package io.timpac.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
public class SecurityAccessTest {
	@Autowired private MockMvc mockMvc;
	private static PasswordEncoder encoder = new BCryptPasswordEncoder();
	private static UserDetails USER;
	private static UserDetails ADMIN;

	@BeforeAll
	public static void initUserDetails() {
		USER = User.builder().username("user").password(encoder.encode("1234")).authorities("ROLE_USER").build();
		ADMIN = User.builder().username("admin").password(encoder.encode("1234")).authorities("ROLE_ADMIN").build();
	}
	
	@Test
	@DisplayName("USER 권한이 있는 유저는 /test/user 에 접근할 수 있다.")
	public void accessUserPage() throws Exception {
		
		String resp = mockMvc.perform(get("/test/user").with(user(USER)))
			.andExpect(status().isOk())
			.andReturn().getResponse().getContentAsString();
		
		assertEquals("Hello User", resp);
	}
	
	@Test
	@DisplayName("USER 권한이 없는 유저는 /test/user 에 접근할 수 없다.")
	public void cannot_access_userPage() throws Exception {
		mockMvc.perform(get("/test/user").with(user(ADMIN)))
			.andExpect(status().is4xxClientError());
		
		mockMvc.perform(get("/test/user").with(anonymous()))
			.andExpect(status().is3xxRedirection());
	}
	
	@Test
	@DisplayName("ADMIN 권한이 있는 유저는 /test/admin 에 접근할 수 있다.")
	public void access_adminPage() throws Exception {
		String resp = mockMvc.perform(get("/test/admin").with(user(ADMIN)))
			.andExpect(status().isOk())
			.andReturn().getResponse().getContentAsString();
		
		assertEquals("Hello Admin", resp);
	}
	
	@Test
	@DisplayName("ADMIN 권한이 없는 유저는 /test/admin 에 접근할 수 없다.")
	public void cannot_access_adminPage() throws Exception {
		mockMvc.perform(get("/test/admin").with(user(USER)))
			.andExpect(status().is4xxClientError());
		
		mockMvc.perform(get("/test/admin").with(anonymous()))
			.andExpect(status().is3xxRedirection());
	}
	
	@Test
	@DisplayName("/login 페이지는 누구나 접근 가능하다.")
	public void anyone_access_loginPage() throws Exception {
		mockMvc.perform(get("/login").with(anonymous()))
			.andExpect(status().isOk());
	}
}
