package io.timpac.jwt;

import java.security.Key;
import java.util.Arrays;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.timpac.domain.user.Authority;
import io.timpac.domain.user.User;
import io.timpac.domain.user.dto.UserDto;

@Component
public class JwtUtil {
	private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);
	public static final String AUTHORIZATION_HEADER = "Authorization";
	public static final String REFRESH_TOKEN_HEADER = "Refresh-token";
	public static final String AUTHORIZATION_PREFIX = "Bearer ";
	
	private final JwtProperties jwtProperties;
	private final String secret;
	private int tokenLifeTimeMs;
	private int refreshTokenLifeTimeMs;
	private SignatureAlgorithm alg;
	private Key key;
	
	public JwtUtil(JwtProperties jwtProperties) {
		this.jwtProperties = jwtProperties;
		secret = this.jwtProperties.getSecretKey();
		tokenLifeTimeMs = this.jwtProperties.getTokenLifeTimeMs();
		refreshTokenLifeTimeMs = this.jwtProperties.getRefreshTokenLifeTimeMs();
		alg = SignatureAlgorithm.HS256;
		key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
	}
	
	public String createToken(User user, int expiredSeconds, UserDto.TokenType tokenType) {
		String auths = user.getAuthorities().stream()
				.map(Authority::getAuthority)
				.collect(Collectors.joining(","));
			
			String jwt = Jwts.builder()
				.setSubject("JWT")
				.claim("username", user.getUsername())
				.claim("authority", auths)
				.claim("tokenType", tokenType.name())
				.setExpiration(new Date(System.currentTimeMillis() + expiredSeconds))
				.signWith(key, alg)
				.compact();
			
			return jwt;
	}
	
	public String createAccessToken(User user) {
		return createToken(user, this.tokenLifeTimeMs, UserDto.TokenType.ACCESS);
	}
	
	public String createRefreshToken(User user) {
		return createToken(user, this.refreshTokenLifeTimeMs, UserDto.TokenType.REFRESH);
	}

	public boolean verify(String jwt) {
		try {
			Claims body = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(jwt).getBody();
			
			if(body.get("tokenType").toString().equals(UserDto.TokenType.REFRESH.name())) {
				throw new SecurityException("리프레시 토큰으로는 접근 불가능");
			}
			
			return true;
		} catch(SecurityException | io.jsonwebtoken.security.SignatureException | MalformedJwtException | IllegalArgumentException  e) {
			logger.info("잘못된 JWT 서명입니다.");
		} catch(UnsupportedJwtException e) {
			logger.info("지원되지않는 JWT 서명입니다.");
		} catch(ExpiredJwtException e) {
			logger.info("만료된 토큰입니다.");
		}
		
		return false;
	}
	
	public String getUsernameFromToken(String token) throws Exception {
		Claims body = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
		return body.get("username").toString();
	}

	public Authentication getAuthentication(String token) {
		Claims body = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
		String username = body.get("username").toString();
		Set<Authority> authList = Arrays.stream(body.get("authority").toString().split(","))
					.map(Authority::new)
					.collect(Collectors.toSet());
		
		User user = User.builder()
			.username(username)
			.authorities(authList)
			.password("")
			.build();
		
		return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
	}

	
	public void setTokenLifeTimeMs(int tokenLifeTimeMs) {
		this.tokenLifeTimeMs = tokenLifeTimeMs;
	}

}
