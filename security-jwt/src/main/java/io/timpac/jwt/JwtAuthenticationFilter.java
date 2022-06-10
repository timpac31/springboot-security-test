package io.timpac.jwt;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.timpac.domain.user.User;
import io.timpac.domain.user.dto.UserDto;
import lombok.SneakyThrows;

public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
	private AuthenticationManager manager;
	private ObjectMapper mapper;
	private JwtUtil jwtUtil;
	
	public JwtAuthenticationFilter(AuthenticationManager manager, ObjectMapper mapper, JwtUtil jwtUtil) {
		setFilterProcessesUrl("/login");
		this.manager = manager;
		this.mapper = mapper;
		this.jwtUtil = jwtUtil;
	}
		
	@Override
	@SneakyThrows
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
			throws AuthenticationException {
		System.out.println("jwtAuthetication Filter start");
		
		UserDto loginUser = mapper.readValue(request.getInputStream(), UserDto.class);
		System.out.println(loginUser);
		UsernamePasswordAuthenticationToken usernamePasswordToken = new UsernamePasswordAuthenticationToken(loginUser.getUsername(), loginUser.getPassword());
		
		return manager.authenticate(usernamePasswordToken);
	}
	
	@Override
	protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
			Authentication authResult) throws IOException, ServletException {
		User user = (User) authResult.getPrincipal();
		String token = jwtUtil.createToken(user);
		System.out.println("token : " + token);
		
		response.addHeader(JwtUtil.AUTHORIZATION_HEADER, JwtUtil.AUTHORIZATION_PREFIX + token);
	}
	
	@Override
	protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException failed) throws IOException, ServletException {
		
		System.out.println(failed.getMessage());
		failed.printStackTrace();
		super.unsuccessfulAuthentication(request, response, failed);
	}

}
