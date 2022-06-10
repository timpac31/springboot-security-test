package io.timpac.jwt;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URI;
import java.net.URISyntaxException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import io.timpac.domain.user.dto.UserDto;
import io.timpac.service.user.UserService;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class JwtAuthenticationFilterTest {
	
	private RestTemplate restTemplate = new RestTemplate();
	
	@Autowired
	private UserService userService;
	
	@LocalServerPort
	private int port;
	
	private UserDto user;
	
	@BeforeEach
	public void saveUser() {
		userService.deleteAll();
		
		user = UserDto.builder()
			.username("jyd")
			.password("1234").build();
		
		userService.signUp(user);
	}
	
	private URI loginUri() throws URISyntaxException {
		return new URI(String.format("http://localhost:%d/login", port));
	}
	
	@Test
	public void loginAttempt() throws RestClientException, URISyntaxException {
		HttpEntity<UserDto> body = new HttpEntity<>(user);
		ResponseEntity<String> resp = restTemplate.exchange(loginUri(), HttpMethod.POST, body, String.class);
		
		System.out.println(resp);
		
		assertEquals(200, resp.getStatusCodeValue());
		
	}

}
