package io.timpac.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.timpac.jwt.JwtAccessDeniedHandler;
import io.timpac.jwt.JwtAuthenticationEntryPoint;
import io.timpac.jwt.JwtUtil;
import io.timpac.service.user.UserDetailsServiceImpl;

@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
	private final ObjectMapper objectMapper;
	private final JwtUtil jwtUtil;
	private final JwtAuthenticationEntryPoint authenticationEntryPoint;
	private final JwtAccessDeniedHandler accessDeniedHandler;
	private final UserDetailsServiceImpl userDetailsService;
	
	public SecurityConfig(ObjectMapper objectMapper, JwtUtil jwtUtil,
			JwtAuthenticationEntryPoint authenticationEntryPoint,
			JwtAccessDeniedHandler accessDeniedHandler,
			UserDetailsServiceImpl userDetailsService) {
		this.objectMapper = objectMapper;
		this.jwtUtil = jwtUtil;
		this.authenticationEntryPoint = authenticationEntryPoint;
		this.accessDeniedHandler = accessDeniedHandler;
		this.userDetailsService = userDetailsService;
	}
	
	@Bean
	public SecurityFilterChain configure(HttpSecurity http) throws Exception {
		http
			.csrf().disable()
			.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
			
			.and()
			.authorizeHttpRequests()
			.antMatchers("/user/signup", "/login").permitAll()
			.anyRequest().authenticated()
			
			.and()
			.exceptionHandling()
			.accessDeniedHandler(accessDeniedHandler)
			.authenticationEntryPoint(authenticationEntryPoint)
			
			.and()
			.apply(new JwtAuthenticationDsl(objectMapper, jwtUtil, userDetailsService));
		
		return http.build();
	}

	@Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().antMatchers("/css/**", "/js/**", "/h2-console/**");
    }
	
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
	
}
