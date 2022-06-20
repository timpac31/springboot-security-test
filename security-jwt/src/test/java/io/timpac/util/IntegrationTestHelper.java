package io.timpac.util;

import java.net.URI;
import java.net.URISyntaxException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import io.timpac.domain.user.Authority;
import io.timpac.domain.user.User;
import io.timpac.domain.user.dto.UserDto;
import io.timpac.jwt.JwtUtil;
import io.timpac.jwt.Tokens;
import io.timpac.service.user.UserService;

public class IntegrationTestHelper {
	@Autowired
	protected UserService userService;
	
	@LocalServerPort 
	int port;
	
	protected RestTemplate restTemplate = new RestTemplate();
	
	/**
	 * 유저권한을 계정 2명과 관리자권한을 가진 계정하나를 회원가입시킨다 
	 * */
	protected void saveTwoUserAndAdmin() {
		UserDto user1 = new UserDto("user1", "1111");
		UserDto user2 = new UserDto("user2", "2222");
		UserDto admin = new UserDto("admin", "3333");

		userService.signUp(user1);
		userService.signUp(user2);
		User signed = userService.signUp(admin);
		userService.addAuthority(signed.getId(), Authority.ADMIN);
	}
	
	protected URI uri(String path) throws URISyntaxException {
		return new URI(String.format("http://localhost:%d%s", port, path));
	}
	
	/**
	 * 아이디 패스워드로 로그인 요청하여 토큰을 발급받는다
	 * */
	protected Tokens getTokens(UserDto user) throws URISyntaxException {
		HttpEntity<UserDto> body = new HttpEntity<UserDto>(user);
		ResponseEntity<String> response = restTemplate.exchange(uri("/login"), HttpMethod.POST, body, String.class);
		
		
		return new Tokens(
				response.getHeaders().get(JwtUtil.AUTHORIZATION_HEADER).get(0).substring(JwtUtil.AUTHORIZATION_PREFIX.length()),
				response.getHeaders().get(JwtUtil.REFRESH_TOKEN_HEADER).get(0)
		);
	}
	
	protected String getAccessTokenFromHeaders(HttpHeaders headers) {
		return headers.get(JwtUtil.AUTHORIZATION_HEADER).get(0).substring(JwtUtil.AUTHORIZATION_PREFIX.length());
	}
	
	/**
	 * Header set {"Authorization" : "Bearer token"}
	 * @param token Authorization 헤더키에 들어갈 토큰값
	 * */
	protected HttpHeaders headerWithToken(String token) {
		HttpHeaders headers = new HttpHeaders();
		headers.add(JwtUtil.AUTHORIZATION_HEADER, JwtUtil.AUTHORIZATION_PREFIX + token);
		return headers;
	}

}
