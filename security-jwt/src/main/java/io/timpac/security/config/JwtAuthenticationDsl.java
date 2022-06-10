package io.timpac.security.config;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.timpac.jwt.JwtAuthenticationFilter;
import io.timpac.jwt.JwtAuthorityFilter;
import io.timpac.jwt.JwtUtil;

public class JwtAuthenticationDsl extends AbstractHttpConfigurer<JwtAuthenticationDsl, HttpSecurity> {
	
	private ObjectMapper objectMapper;
	private JwtUtil jwtUtil;
	
	public JwtAuthenticationDsl(ObjectMapper objectMapper, JwtUtil jwtUtil) {
		this.objectMapper = objectMapper;
		this.jwtUtil = jwtUtil;
	}
	
	@Override
	public void configure(HttpSecurity http) throws Exception {
		AuthenticationManager manager = http.getSharedObject(AuthenticationManager.class);
		http.addFilter(new JwtAuthenticationFilter(manager, objectMapper, jwtUtil));
		http.addFilter(new JwtAuthorityFilter(manager, jwtUtil));
	}

}
