package io.timpac.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
@Builder
public class UserDto {
	public enum TokenType {
		ACCESS, REFRESH
	}
	
	public UserDto(String username, String password) {
		this.username = username;
		this.password = password;
	}
	
	public UserDto(String username, String password, TokenType tokenType) {
		this.username = username;
		this.password = password;
		this.tokenType = tokenType;
	}
	
	private String username;
	private String password;
	private TokenType tokenType;
	private String refreshToken;
}
