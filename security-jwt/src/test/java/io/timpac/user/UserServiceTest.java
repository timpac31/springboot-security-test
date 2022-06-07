package io.timpac.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Transactional;

import io.timpac.domain.user.Authority;
import io.timpac.domain.user.User;
import io.timpac.domain.user.dto.UserDto;
import io.timpac.service.user.UserService;

@SpringBootTest
@Transactional
public class UserServiceTest {
	@Autowired
	private UserService userService;
	
	@Test
	@DisplayName("유저회원가입 정상")
	public void signUp() {
		UserDto userDto = new UserDto("timpac", "1234");

		User savedUser = userService.signUp(userDto);
		User findedUser = userService.getUser(savedUser.getUsername());
		
		assertEquals(savedUser.getId(), findedUser.getId());
	}
	
	@Test
	@DisplayName("이미 username이 존재하면 에러발생")
	public void dupulicateUsername() {
		userService.signUp(new UserDto("timpac", "1234"));
		
		assertThrows(UsernameNotFoundException.class, () -> {
			userService.signUp(new UserDto("timpac", "1111"));
		});
	}
	
	@Test
	@DisplayName("권한 추가")
	public void addAuthority() {
		User user = userService.signUp(new UserDto("timpac", "1234"));
		userService.addAuthority(user.getId(), "ROLE_ADMIN");
		
		User findedUser = userService.getUser(user.getUsername());
		Set<Authority> authorities = findedUser.getAuthorities();
		
		assertEquals(2, authorities.size());
		assertTrue(authorities.contains(new Authority("ROLE_ADMIN")));
		assertTrue(authorities.contains(new Authority("ROLE_USER")));
	}
	
	@Test
	@DisplayName("권한 삭제")
	public void removeAuthority() {
		User user = userService.signUp(new UserDto("timpac", "1234"));
		userService.removeAuthority(user.getId(), "ROLE_USER");
		
		User findedUser = userService.getUser(user.getUsername());
		Set<Authority> authorities = findedUser.getAuthorities();
		
		assertEquals(0, authorities.size());
	}
	
	
	
	
	
	
	
}
