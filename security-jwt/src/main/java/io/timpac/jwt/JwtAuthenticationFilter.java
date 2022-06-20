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
import org.springframework.util.ObjectUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.timpac.domain.user.User;
import io.timpac.domain.user.dto.UserDto;
import io.timpac.service.user.UserDetailsServiceImpl;
import lombok.SneakyThrows;

public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
	private AuthenticationManager manager;
	private ObjectMapper mapper;
	private JwtUtil jwtUtil;
	private UserDetailsServiceImpl userDetailsService;
	
	public JwtAuthenticationFilter(AuthenticationManager manager, ObjectMapper mapper, JwtUtil jwtUtil, UserDetailsServiceImpl userDetailsService) {
		setFilterProcessesUrl("/login");
		this.manager = manager;
		this.mapper = mapper;
		this.jwtUtil = jwtUtil;
		this.userDetailsService = userDetailsService;
	}
		
	@Override
	@SneakyThrows
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
			throws AuthenticationException {
		System.out.println("jwtAuthetication Filter start");
		
		UserDto loginUser = mapper.readValue(request.getInputStream(), UserDto.class);
		
		if(loginUser.getTokenType() == UserDto.TokenType.ACCESS) {
			UsernamePasswordAuthenticationToken usernamePasswordToken = new UsernamePasswordAuthenticationToken(
					loginUser.getUsername(), loginUser.getPassword());
			
			return manager.authenticate(usernamePasswordToken);
		} else if(loginUser.getTokenType() == UserDto.TokenType.REFRESH) {
			if(ObjectUtils.isEmpty(loginUser.getRefreshToken())) {
				throw new IllegalArgumentException("리프레시 토큰이 필요합니다.");
			}
			
			String username = jwtUtil.getUsernameFromToken(loginUser.getRefreshToken());
			User user = userDetailsService.loadUserByUsername(username);
			
			return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
		} else {
			throw new IllegalArgumentException("알 수 없는 로그인 타입: " + loginUser.getTokenType());
		}
		
		
	}
	
	@Override
	protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
			Authentication authResult) throws IOException, ServletException {
		User user = (User) authResult.getPrincipal();
		
		response.addHeader(JwtUtil.AUTHORIZATION_HEADER, JwtUtil.AUTHORIZATION_PREFIX + jwtUtil.createAccessToken(user));
		response.addHeader(JwtUtil.REFRESH_TOKEN_HEADER, jwtUtil.createRefreshToken(user));
	}
	
	@Override
	protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException failed) throws IOException, ServletException {
		
		System.out.println(failed.getMessage());
		failed.printStackTrace();
		super.unsuccessfulAuthentication(request, response, failed);
	}

}
