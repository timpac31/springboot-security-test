package io.timpac.user;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import io.timpac.service.user.UserService;

@WebMvcTest
public class UserControllerTest {
	@Autowired
	private MockMvc mockMvc;
	
	@MockBean
	private UserService userService;
	
	private static PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
	
	private static UserDetails USER;
	private static UserDetails ADMIN;

	@BeforeAll
	public static void initUserDetails() {
		USER = User.builder().username("user").password("1234").authorities("ROLE_USER").build();
		ADMIN = User.builder().username("admin").password(passwordEncoder.encode("1234")).authorities("ROLE_ADMIN").build();
	}
	
	@Test
	@DisplayName("리스트는 접근권한이 없으면 300에러")
	public void list_access_denied() throws Exception {
		mockMvc.perform(get("/user/list"))
			.andExpect(status().is4xxClientError());
	}
	
	@Test
	@DisplayName("관리자는 리스트에 접근할 수 있다.")
	public void list_acessable() throws Exception { 
		io.timpac.domain.user.User user = new io.timpac.domain.user.User();
		user.setUsername("jyd");
		when(userService.findAll(1, 10)).thenReturn(new PageImpl<>(List.of(user)));
		
		String resp = mockMvc.perform(get("/user/list").with(user(ADMIN)))
			.andExpect(status().isOk())
			.andReturn().getResponse().getContentAsString();
		
		System.out.println("response::");
		System.out.println(resp);
	}
	
	@Test
	@DisplayName("관리자는 권한을 줄수 있다.")
	public void addAuthority() throws Exception {
		when(userService.addAuthority(1L, "ROLE_ADMIN")).thenReturn(UserTestHelper.createAdmin());
		
		mockMvc.perform(put("/user/authority/add").param("userId", "1").param("authorityName", "ROLE_ADMIN").with(user(ADMIN)))
			.andExpect(status().isOk());
	}
	
	@Test
	@DisplayName("USER는 개인정보 접근 가능")
	public void getUser() throws Exception {
		when(userService.getUser("1")).thenReturn(UserTestHelper.createUser());
		
		mockMvc.perform(get("/user/1").with(user(USER))).andExpect(status().isOk());
	}
	
	
	
}
