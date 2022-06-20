package io.timpac.jwt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.URISyntaxException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;

import io.timpac.domain.board.Board;
import io.timpac.domain.user.dto.UserDto;
import io.timpac.domain.user.dto.UserDto.TokenType;
import io.timpac.util.IntegrationTestHelper;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class RefreshTokenTest extends IntegrationTestHelper {
	@Autowired JwtUtil jwtUtil;
	
	private UserDto user1 = new UserDto("user1", "1111", UserDto.TokenType.ACCESS);
	
	@BeforeEach
	void setUp() {
		saveTwoUserAndAdmin();
	}
	
	@AfterEach
	void after() {
		userService.deleteAll();
	}
	
	@DisplayName("1.token이 만료되면 인증이 안된다")
	@Test
	Tokens test1() throws URISyntaxException, InterruptedException {
		jwtUtil.setTokenLifeTimeMs(1000);
		
		Tokens tokens = getTokens(user1);
		HttpHeaders headers = headerWithToken(tokens.getAccessToken());
		HttpEntity<?> entity = new HttpEntity<>(null, headers);

		Thread.sleep(2000);
		
		HttpClientErrorException ex = assertThrows(HttpClientErrorException.class, () -> {
			restTemplate.exchange(uri("/test/user"), HttpMethod.GET, entity, String.class);
		});
		assertEquals(403, ex.getRawStatusCode());
		
		return tokens;
	}
	
	@DisplayName("2.refresh token으로 access token을 발급받는다")
	@Test
	void test2() throws URISyntaxException {
		String refreshToken = getTokens(user1).getRefreshToken();
		HttpEntity<UserDto> entity = new HttpEntity<>(UserDto.builder().tokenType(TokenType.REFRESH).refreshToken(refreshToken).build());
		
		ResponseEntity<String> response = restTemplate.exchange(uri("/login"), HttpMethod.POST, entity, String.class);
		
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(getAccessTokenFromHeaders(response.getHeaders()));
	}
	
	@DisplayName("3.토큰이 만료되면 refresh token으로 access token을 다시 발급받아서 인증에 성공한다")
	@Test
	void test3() throws URISyntaxException, InterruptedException {
		Tokens tokens = test1();
		
		UserDto refreshReq = UserDto.builder().tokenType(TokenType.REFRESH).refreshToken(tokens.getRefreshToken()).build();
		ResponseEntity<String> response = restTemplate.exchange(uri("/login"), HttpMethod.POST, new HttpEntity<>(refreshReq), String.class);

		String refreshedAccessToken = getAccessTokenFromHeaders(response.getHeaders());
		HttpHeaders headers = headerWithToken(refreshedAccessToken);
		ResponseEntity<String> response2 = restTemplate.exchange(uri("/test/user"), HttpMethod.GET, new HttpEntity<>(null, headers), String.class);
		
		assertEquals(HttpStatus.OK, response2.getStatusCode());
	}
	
	@DisplayName("4.refresh token으로는 api resource에 접근할 수 없다")
	@Test
	void test4() throws URISyntaxException {
		String refreshToken = getTokens(user1).getRefreshToken();
		HttpEntity<?> entity = new HttpEntity<Board>(null, headerWithToken(refreshToken));
		
		HttpClientErrorException ex = assertThrows(HttpClientErrorException.class, () -> {
			restTemplate.exchange(uri("/test/user"), HttpMethod.GET, entity, String.class);
		});
		assertEquals(403, ex.getRawStatusCode());
	}
	
}
