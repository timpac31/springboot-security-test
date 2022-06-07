package io.timpac.service.user;

import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.timpac.domain.user.Authority;
import io.timpac.repository.user.AuthorityRepository;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthorityService {
	private final AuthorityRepository authorityRepository;

	public Authority retriveAuthority(String authorityName) {
		return authorityRepository.findById(authorityName)
				.orElseGet(() -> authorityRepository.save(new Authority(authorityName)));
	}
	
	public Set<Authority> retriveAuthoritySet(String authorityName) {
		Set<Authority> auths = new HashSet<>();
		auths.add(retriveAuthority(authorityName));
		
		return auths;
	}
	
}
