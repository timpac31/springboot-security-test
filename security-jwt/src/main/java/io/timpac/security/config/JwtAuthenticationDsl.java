package io.timpac.security.config;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.timpac.jwt.JwtAuthenticationFilter;
import io.timpac.jwt.JwtAuthorityFilter;
import io.timpac.jwt.JwtUtil;
import io.timpac.service.user.UserDetailsServiceImpl;

public class JwtAuthenticationDsl extends AbstractHttpConfigurer<JwtAuthenticationDsl, HttpSecurity> {
	
	private ObjectMapper objectMapper;
	private JwtUtil jwtUtil;
	private UserDetailsServiceImpl userDetailsService;
	
	public JwtAuthenticationDsl(ObjectMapper objectMapper, JwtUtil jwtUtil, UserDetailsServiceImpl userDetailsService) {
		this.objectMapper = objectMapper;
		this.jwtUtil = jwtUtil;
		this.userDetailsService = userDetailsService;
	}
	
	@Override
	public void configure(HttpSecurity http) throws Exception {
		AuthenticationManager manager = http.getSharedObject(AuthenticationManager.class);
		http.addFilter(new JwtAuthenticationFilter(manager, objectMapper, jwtUtil, userDetailsService));
		http.addFilter(new JwtAuthorityFilter(manager, jwtUtil));
	}

}
