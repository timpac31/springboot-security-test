package io.timpac.user;

import java.util.Set;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import io.timpac.domain.user.Authority;
import io.timpac.domain.user.User;

public class UserTestHelper {
	
	public static User createUser() {
		return User.builder()
			.username("jyd")
			.password(new BCryptPasswordEncoder().encode("1234"))
			.authorities(Set.of(new Authority("ROLE_USER")))
			.build();
	}
	
	public static User createAdmin() {
		return User.builder()
		.username("jyd")
		.password(new BCryptPasswordEncoder().encode("1234"))
		.authorities(Set.of(new Authority("ROLE_USER"), new Authority("ROLE_ADMIN")))
		.build();
	}

}
