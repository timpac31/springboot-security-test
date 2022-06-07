package io.timpac.controller.user;

import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.timpac.domain.common.RestResponsePage;
import io.timpac.domain.user.User;
import io.timpac.domain.user.dto.UserDto;
import io.timpac.service.user.UserService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
	private final UserService userService;
	
	@PostMapping("/signup")
	public User singUp(@RequestBody UserDto userDto) {
		return userService.signUp(userDto);
	}
	
	@GetMapping("/list")
	@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER')")
	public RestResponsePage<User> userList(@RequestParam(defaultValue = "1") Integer page, @RequestParam(defaultValue = "10") Integer pageSize) {
		Page<User> users = userService.findAll(page, pageSize);
		return new RestResponsePage<>(users.getContent(), users.getPageable(), users.getTotalElements());
	}
	
	@GetMapping("/{userId}")
	public User getUser(@PathVariable String userId) {
		return userService.findUser(Long.valueOf(userId));
	}

	@PutMapping("/authority/add")
	public User addAuthority(@RequestParam String userId, @RequestParam String authorityName) {
		return userService.addAuthority(Long.valueOf(userId), authorityName);
	}
	
	@PutMapping("/authority/remove")
	public User removeAuthority(@RequestParam String userId, @RequestParam String authorityName) {
		return userService.removeAuthority(Long.valueOf(userId), authorityName);
	}
}
