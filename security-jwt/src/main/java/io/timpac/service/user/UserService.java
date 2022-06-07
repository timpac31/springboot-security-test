package io.timpac.service.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.timpac.domain.user.User;
import io.timpac.domain.user.dto.UserDto;
import io.timpac.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {
	private final UserRepository userRepository;
	private final AuthorityService authorityService;
	
	public User signUp(UserDto userDto) {
		userRepository.findByUsername(userDto.getUsername()).ifPresent(user -> {
			throw new UsernameNotFoundException(user.getUsername() + " 유저가 이미 존재합니다.");
		});

		User user = User.builder()
				.username(userDto.getUsername())
				.password(userDto.getPassword())
				.enabled(true)
				.authorities(authorityService.retriveAuthoritySet("ROLE_USER"))
				.build();
		
		return userRepository.save(user);
	}
	
	public User getUser(String username) {
		return userRepository.findByUsername(username).orElse(null);
	}	
	
	public User findUser(Long userId) {
		return userRepository.findById(userId)
				.orElseThrow(() -> new UsernameNotFoundException("아이디 [" +userId+ "] 를 찾을 수 없습니다."));
	}
	
	public User addAuthority(Long userId, String authorityName) {
		User user = findUser(userId);
		user.addAuthority(authorityService.retriveAuthority(authorityName));
		return user;
	}

	public User removeAuthority(Long userId, String authorityName) {
		User user = findUser(userId);
		user.removeAuthority(authorityService.retriveAuthority(authorityName));
		return user;
	}

	public Page<User> findAll(Integer page, Integer pageSize) {
		return userRepository.findAll(PageRequest.of(page, pageSize));
	}
	
}