package io.timpac.jwt;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

public class JwtAuthorityFilter extends BasicAuthenticationFilter {
	private JwtUtil jwtUtil;

	public JwtAuthorityFilter(AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
		super(authenticationManager);
		this.jwtUtil = jwtUtil;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		System.out.println("JwtAuthorityFilter start==");
		
		String token = request.getHeader(JwtUtil.AUTHORIZATION_HEADER);
		if(token == null || !token.startsWith(JwtUtil.AUTHORIZATION_PREFIX)) {
			super.doFilterInternal(request, response, chain);
			return;
		}

		String jwt = token.substring(JwtUtil.AUTHORIZATION_PREFIX.length());
		boolean valid = jwtUtil.verify(jwt);
		if(valid) {
			Authentication authentication = jwtUtil.getAuthentication(jwt);
			SecurityContextHolder.getContext().setAuthentication(authentication);
		}
		
		super.doFilterInternal(request, response, chain);
	}

	

}
