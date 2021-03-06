package io.timpac.board;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static io.timpac.domain.user.dto.UserDto.TokenType.*;

import java.net.URISyntaxException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;

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
import io.timpac.service.board.BoardService;
import io.timpac.util.IntegrationTestHelper;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class BoardControllerIntegrationTest extends IntegrationTestHelper {
	@Autowired private BoardService boardService;
	@Autowired private ObjectMapper objectMapper;
    
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
		user1 = new UserDto("user1", "1111", ACCESS);
		user2 = new UserDto("user2", "2222", ACCESS);
		admin = new UserDto("admin", "3333", ACCESS);

		userService.signUp(user1);
		userService.signUp(user2);
		User signed = userService.signUp(admin);
		userService.addAuthority(signed.getId(), Authority.ADMIN);
	}
	
	@DisplayName("1.user1??? ????????? ????????????")
	@Test
	void test1() throws URISyntaxException {
		String token = getTokens(user1).getAccessToken();
		assertNotNull(token);
	}
	
	@DisplayName("2-1.???????????? ????????? ????????? ????????? ????????? ??? ??????.")
	@Test
	void test2() throws URISyntaxException {
		HttpEntity<Board> entity = new HttpEntity<Board>(makeBoard(), headerWithToken(getTokens(user1).getAccessToken()));
		ResponseEntity<Board> response = restTemplate.exchange(uri("/board/save"), HttpMethod.POST, entity, Board.class);
		this.board = response.getBody();
		
		assertEquals(200, response.getStatusCodeValue());
		assertEquals(makeBoard().getTitle(), response.getBody().getTitle());
		assertEquals(makeBoard().getContent(), response.getBody().getContent());
		assertEquals("user1", response.getBody().getWriter().getUsername());
	}
	
	@DisplayName("2-2.???????????? ????????? ????????? ????????? ??? ??????.")
	@Test
	void test3() throws URISyntaxException {
		HttpEntity<Board> entity = new HttpEntity<Board>(makeBoard(), headerWithToken("invalidToken.invalidToken.invalidToken"));
		
		assertThrows(HttpClientErrorException.class, () -> {
			restTemplate.exchange(uri("/board/save"), HttpMethod.POST, entity, Board.class);
		});
	}
	
	@DisplayName("3-1.???????????? ???????????? ????????? ??? ??????.")
	@Test
	void test4() throws URISyntaxException {
		saveBoard();
		
		HttpEntity<Board> entity = new HttpEntity<Board>(
				Board.builder().boardId(this.board.getBoardId()).title("????????????").content("????????????").build(),
				headerWithToken(getTokens(user1).getAccessToken()));
		ResponseEntity<Board> response = restTemplate.exchange(uri("/board/save"), HttpMethod.POST, entity, Board.class);
		
		assertEquals(200, response.getStatusCodeValue());
		assertEquals("????????????", response.getBody().getTitle());
		assertEquals("????????????", response.getBody().getContent());
	}
	
	@DisplayName("3-2.???????????? ???????????? ????????? ????????? ??? ??????.")
	@Test
	void test5() throws URISyntaxException {
		saveBoard();
		
		HttpEntity<Board> entity = new HttpEntity<Board>(
				Board.builder().boardId(this.board.getBoardId()).title("????????????").content("????????????").build(),
				headerWithToken(getTokens(user2).getAccessToken()));
		
		HttpClientErrorException exception = assertThrows(HttpClientErrorException.class, () -> {
			restTemplate.exchange(uri("/board/save"), HttpMethod.POST, entity, Board.class);
		});
		assertEquals(403, exception.getRawStatusCode());
	}
	
	@DisplayName("3-3.???????????? ???????????? ???????????? ?????? ????????? ??? ??????.")
	@Test
	void test6() throws URISyntaxException {
		saveBoard();
		
		HttpEntity<Board> entity = new HttpEntity<Board>(
				Board.builder().boardId(this.board.getBoardId()).title("????????????").content("????????????").build(),
				headerWithToken(getTokens(admin).getAccessToken()));
		
		ResponseEntity<Board> response = restTemplate.exchange(uri("/board/save"), HttpMethod.POST, entity, Board.class);
		
		assertEquals(200, response.getStatusCodeValue());
		assertEquals("????????????", response.getBody().getTitle());
		assertEquals("????????????", response.getBody().getContent());
	}
	
	@DisplayName("4-1.???????????? ???????????? ????????? ????????? ??? ??????.")
	@Test
	void test7() throws URISyntaxException {
		saveBoard();
		
		HttpEntity<Board> entity = new HttpEntity<Board>(makeBoard(), headerWithToken(getTokens(user2).getAccessToken()));
		
		HttpClientErrorException exception = assertThrows(HttpClientErrorException.class, () -> {
			restTemplate.exchange(uri("/board/" + this.board.getBoardId()), HttpMethod.DELETE, entity, Board.class);
		});
		assertEquals(403, exception.getRawStatusCode());
	}
	
	@DisplayName("4-2.???????????? ???????????? ????????? ??? ??????.")
	@Test
	void test8() throws URISyntaxException, JsonMappingException, JsonProcessingException {
		saveBoard();
		
		HttpEntity<Board> entity = new HttpEntity<Board>(makeBoard(), headerWithToken(getTokens(user1).getAccessToken()));
		
		ResponseEntity<Board> response = restTemplate.exchange(uri("/board/" + this.board.getBoardId()), HttpMethod.DELETE, entity, Board.class);
		assertEquals(200, response.getStatusCodeValue());
		
		ResponseEntity<String> response2 = restTemplate.exchange(uri("/board/list"), HttpMethod.GET, entity, String.class);
		RestResponsePage<BoardListDto> page = objectMapper.readValue(response2.getBody(), new TypeReference<RestResponsePage<BoardListDto>>() {});
		assertEquals(0, page.getTotalElements());
	}
	
	
	private void saveBoard() throws URISyntaxException {
		HttpEntity<Board> entity = new HttpEntity<Board>(makeBoard(), headerWithToken(getTokens(user1).getAccessToken()));
		
		ResponseEntity<Board> response = restTemplate.exchange(uri("/board/save"), HttpMethod.POST, entity, Board.class);
		this.board = response.getBody();
	}
	
	private Board makeBoard() {
		return Board.builder()
			.title("??????1")
			.content("??????1")
			.build();
	}
	
	
}
