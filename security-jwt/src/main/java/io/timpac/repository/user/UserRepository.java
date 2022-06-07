package io.timpac.repository.user;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import io.timpac.domain.user.User;

public interface UserRepository extends JpaRepository<User, Long> {
	Optional<User> findByUsername(String username);
	
}
