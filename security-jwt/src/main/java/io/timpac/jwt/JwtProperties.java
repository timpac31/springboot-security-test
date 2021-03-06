package io.timpac.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@Configuration
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
	private String secretKey;
	private int tokenLifeTimeMs;
	private int refreshTokenLifeTimeMs;
}
