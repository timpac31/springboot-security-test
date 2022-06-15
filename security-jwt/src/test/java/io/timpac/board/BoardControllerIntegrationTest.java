package io.timpac.board;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.URI;
import java.net.URISyntaxException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import io.timpac.domain.board.Board;
import io.timpac.domain.board.dto.BoardListDto;
import io.timpac.domain.common.RestResponsePage;
import io.timpac.domain.user.Authority;
import io.timpac.domain.user.User;
import io.timpac.domain.user.dto.UserDto;
import io.timpac.jwt.JwtUtil;
import io.timpac.service.board.BoardService;
import io.timpac.service.user.UserService;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class BoardControllerIntegrationTest {
	@Autowired private BoardService boardService;
	@Autowired private UserService userService;
	@LocalServerPort private int port;
	@Autowired private ObjectMapper objectMapper;
    private RestTemplate restTemplate = new RestTemplate();
    
    private Board board;
    private UserDto user1;
    private UserDto user2;
    private UserDto admin;
	
	@BeforeEach
	void before() {
		saveTwoUser();
	}
	
	@AfterEach
	void after() {
		boardService.deleteAll();
		userService.deleteAll();
	}

	private void saveTwoUser() {
		user1 = new UserDto("user1", "1111");
		user2 = new UserDto("user2", "2222");
		admin = new UserDto("admin", "3333");

		userService.signUp(user1);
		userService.signUp(user2);
		User signed = userService.signUp(admin);
		userService.addAuthority(signed.getId(), Authority.ADMIN);
	}
	
	@DisplayName("1.user1이 토큰을 가져온다")
	@Test
	void test1() throws URISyntaxException {
		String token = getAccessToken(user1);
		assertNotNull(token);
	}
	
	@DisplayName("2-1.게시글은 권한만 있으면 누구나 저장할 수 있다.")
	@Test
	void test2() throws URISyntaxException {
		HttpEntity<Board> entity = new HttpEntity<Board>(makeBoard(), headerWithToken(getAccessToken(user1)));
		ResponseEntity<Board> response = restTemplate.exchange(uri("/board/save"), HttpMethod.POST, entity, Board.class);
		this.board = response.getBody();
		
		assertEquals(200, response.getStatusCodeValue());
		assertEquals(makeBoard().getTitle(), response.getBody().getTitle());
		assertEquals(makeBoard().getContent(), response.getBody().getContent());
		assertEquals("user1", response.getBody().getWriter().getUsername());
	}
	
	@DisplayName("2-2.게시글은 권한이 없으면 저장할 수 없다.")
	@Test
	void test3() throws URISyntaxException {
		HttpEntity<Board> entity = new HttpEntity<Board>(makeBoard(), headerWithToken("invalidToken.invalidToken.invalidToken"));
		
		assertThrows(HttpClientErrorException.class, () -> {
			restTemplate.exchange(uri("/board/save"), HttpMethod.POST, entity, Board.class);
		});
	}
	
	@DisplayName("3-1.게시글은 게시자가 수정할 수 있다.")
	@Test
	void test4() throws URISyntaxException {
		saveBoard();
		
		HttpEntity<Board> entity = new HttpEntity<Board>(Board.builder().boardId(this.board.getBoardId()).title("제목수정").content("내용수정").build(), headerWithToken(getAccessToken(user1)));
		ResponseEntity<Board> response = restTemplate.exchange(uri("/board/save"), HttpMethod.POST, entity, Board.class);
		
		assertEquals(200, response.getStatusCodeValue());
		assertEquals("제목수정", response.getBody().getTitle());
		assertEquals("내용수정", response.getBody().getContent());
	}
	
	@DisplayName("3-2.게시글은 게시자가 아니면 수정할 수 없다.")
	@Test
	void test5() throws URISyntaxException {
		saveBoard();
		
		HttpEntity<Board> entity = new HttpEntity<Board>(Board.builder().boardId(this.board.getBoardId()).title("제목수정").content("내용수정").build(), headerWithToken(getAccessToken(user2)));
		
		HttpClientErrorException exception = assertThrows(HttpClientErrorException.class, () -> {
			restTemplate.exchange(uri("/board/save"), HttpMethod.POST, entity, Board.class);
		});
		assertEquals(403, exception.getRawStatusCode());
	}
	
	@DisplayName("3-3.관리자는 게시자가 아니여도 글을 수정할 수 있다.")
	@Test
	void test6() throws URISyntaxException {
		saveBoard();
		
		HttpEntity<Board> entity = new HttpEntity<Board>(Board.builder().boardId(this.board.getBoardId()).title("제목수정").content("내용수정").build(), headerWithToken(getAccessToken(admin)));
		
		ResponseEntity<Board> response = restTemplate.exchange(uri("/board/save"), HttpMethod.POST, entity, Board.class);
		
		assertEquals(200, response.getStatusCodeValue());
		assertEquals("제목수정", response.getBody().getTitle());
		assertEquals("내용수정", response.getBody().getContent());
	}
	
	@DisplayName("4-1.게시글은 게시자가 아니면 삭제할 수 없다.")
	@Test
	void test7() throws URISyntaxException {
		saveBoard();
		
		HttpEntity<Board> entity = new HttpEntity<Board>(makeBoard(), headerWithToken(getAccessToken(user2)));
		
		HttpClientErrorException exception = assertThrows(HttpClientErrorException.class, () -> {
			restTemplate.exchange(uri("/board/" + this.board.getBoardId()), HttpMethod.DELETE, entity, Board.class);
		});
		assertEquals(403, exception.getRawStatusCode());
	}
	
	@DisplayName("4-2.게시글은 게시자가 삭제할 수 있다.")
	@Test
	void test8() throws URISyntaxException, JsonMappingException, JsonProcessingException {
		saveBoard();
		
		HttpEntity<Board> entity = new HttpEntity<Board>(makeBoard(), headerWithToken(getAccessToken(user1)));
		
		ResponseEntity<Board> response = restTemplate.exchange(uri("/board/" + this.board.getBoardId()), HttpMethod.DELETE, entity, Board.class);
		assertEquals(200, response.getStatusCodeValue());
		
		ResponseEntity<String> response2 = restTemplate.exchange(uri("/board/list"), HttpMethod.GET, entity, String.class);
		RestResponsePage<BoardListDto> page = objectMapper.readValue(response2.getBody(), new TypeReference<RestResponsePage<BoardListDto>>() {});
		assertEquals(0, page.getTotalElements());
	}
	
	
	private void saveBoard() throws URISyntaxException {
		HttpEntity<Board> entity = new HttpEntity<Board>(makeBoard(), headerWithToken(getAccessToken(user1)));
		
		ResponseEntity<Board> response = restTemplate.exchange(uri("/board/save"), HttpMethod.POST, entity, Board.class);
		this.board = response.getBody();
	}
	
	/**아이디 패스워드로 엑세스토큰을 얻는다*/
	private String getAccessToken(UserDto user) throws URISyntaxException {
		HttpEntity<UserDto> body = new HttpEntity<UserDto>(user);
		ResponseEntity<String> response = restTemplate.exchange(uri("/login"), HttpMethod.POST, body, String.class);
		
		return response.getHeaders().get(JwtUtil.AUTHORIZATION_HEADER).get(0).substring(JwtUtil.AUTHORIZATION_PREFIX.length());
	}
	
	private HttpHeaders headerWithToken(String token) {
		HttpHeaders headers = new HttpHeaders();
		headers.add(JwtUtil.AUTHORIZATION_HEADER, JwtUtil.AUTHORIZATION_PREFIX + token);
		return headers;
	}
	
	private URI uri(String path) throws URISyntaxException {
		return new URI(String.format("http://localhost:%d%s", port, path));
	}
	
	private Board makeBoard() {
		return Board.builder()
			.title("제목1")
			.content("내용1")
			.build();
	}
	
	
}
