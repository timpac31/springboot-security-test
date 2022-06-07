package io.timpac.domain.user;

import java.time.LocalDateTime;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "USERS")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class User {
	@Id @GeneratedValue
	@Column(name = "USER_ID")
	private Long id;
	
	private String username;
	private String password;
	private boolean enabled;
	
	@CreatedDate
	private LocalDateTime created;
	@LastModifiedDate
	private LocalDateTime updated;
	
	@ManyToMany
	@JoinTable(name = "USER_AUTHORITY", 
		joinColumns = @JoinColumn(name = "USER_ID"), 
		inverseJoinColumns = @JoinColumn(name = "AUTHORITY_NAME") 
	)
	private Set<Authority> authorities;
	
	public void addAuthority(Authority authority) {
		this.authorities.add(authority);
	}

	public void removeAuthority(Authority authority) {
		this.authorities.remove(authority);
	}
}
