package io.timpac.repository.user;

import org.springframework.data.jpa.repository.JpaRepository;

import io.timpac.domain.user.Authority;

public interface AuthorityRepository extends JpaRepository<Authority, String> {

}
