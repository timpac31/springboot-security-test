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

@Component
public class JwtUtil {
	private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);
	public static final String AUTHORIZATION_HEADER = "Authorization";
	public static final String AUTHORIZATION_PREFIX = "Bearer ";
	
	private final JwtProperties jwtProperties;
	private final String secret;
	private final int expiredSeconds;
	private SignatureAlgorithm alg;
	private Key key;
	
	public JwtUtil(JwtProperties jwtProperties) {
		this.jwtProperties = jwtProperties;
		secret = this.jwtProperties.getSecretKey();
		expiredSeconds = this.jwtProperties.getExpiredMilliSeconds();
		alg = SignatureAlgorithm.HS256;
		key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
	}
	
	public String createToken(User user) {
		String auths = user.getAuthorities().stream()
			.map(Authority::getAuthority)
			.collect(Collectors.joining(","));
		
		String jwt = Jwts.builder()
			.setSubject("JWT")
			.claim("username", user.getUsername())
			.claim("authority", auths)
			.setExpiration(new Date(System.currentTimeMillis() + expiredSeconds))
			.signWith(key, alg)
			.compact();
		
		return jwt;
	}

	public boolean verify(String jwt) {
		try {
			Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(jwt);
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
}
