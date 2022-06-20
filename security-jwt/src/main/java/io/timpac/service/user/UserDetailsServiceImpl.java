package io.timpac.service.user;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.timpac.domain.user.User;
import io.timpac.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
	private final UserRepository userRepository;

	@Override
	public User loadUserByUsername(String username) throws UsernameNotFoundException {
		return userRepository.findOneWithAuthoritiesByUsername(username)
				.orElseThrow(() -> new UsernameNotFoundException(username + "을 찾을 수 없습니다."));
	}

}
