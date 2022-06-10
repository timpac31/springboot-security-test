package io.timpac.jwt;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.security.Key;
import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;

public class JwtTest {
	private int expiredSeconds = 2 * 1000;
	private String secret = "timpac1234timpac1234timpac1234timpac1234timpac1234";
	private Key key;
	private Key unMatchKey;
	private SignatureAlgorithm alg;
	
	@BeforeEach
	public void setup() {
		key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
		unMatchKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode("1111111111111111111111111111111111111111111111111111111111111111111111"));
		alg = SignatureAlgorithm.HS256;
	}
	
	@Test
	public void create() {
		String jwt = Jwts.builder()
			.setSubject("JWT")
			.claim("username", "jyd")
			.setExpiration(new Date(System.currentTimeMillis() + expiredSeconds))
			.signWith(key, alg)
			.compact();
		
		System.out.println(jwt);
	}
	
	@Test
	public void verify_success() {
		String jwt = Jwts.builder()
			.setSubject("JWT")
			.claim("username", "jyd")
			.setExpiration(new Date(System.currentTimeMillis() + expiredSeconds))
			.signWith(key, alg)
			.compact();
		
		Jws<Claims> jws = Jwts.parserBuilder()
			.setSigningKey(key).build()
			.parseClaimsJws(jwt);
		
		System.out.println("body: " + jws.getBody());
		System.out.println("header: " + jws.getHeader());
	}
	
	@Test
	@Disabled
	public void when_expiredTime_verify_fail() throws InterruptedException {
		String jwt = Jwts.builder()
			.setSubject("JWT")
			.claim("username", "jyd")
			.setExpiration(new Date(System.currentTimeMillis() + expiredSeconds))
			.signWith(key, alg)
			.compact();
		
		Thread.sleep(2500);
		
		assertThrows(ExpiredJwtException.class, () -> {
			Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(jwt);
		});
	}
	
	@Test
	public void when_notMatchKey_verify_fail() throws InterruptedException {
		String jwt = Jwts.builder()
			.setSubject("JWT")
			.claim("username", "jyd")
			.setExpiration(new Date(System.currentTimeMillis() + expiredSeconds))
			.signWith(key, alg)
			.compact();
		
		Thread.sleep(2500);
		
		assertThrows(ExpiredJwtException.class, () -> {
			Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(jwt);
		});
		
		assertThrows(SignatureException.class, () -> {
			Jwts.parserBuilder().setSigningKey(unMatchKey).build().parseClaimsJws(jwt);
		});
	}
	

}
