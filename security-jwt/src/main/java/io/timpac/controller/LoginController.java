package io.timpac.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {

	@RequestMapping("/login")
	public String login(@RequestParam(defaultValue = "false") boolean error, Model model) {
		if(error) {
			model.addAttribute("errorMessage", "아이디와 패스워드가 일치하지 않습니다.");
		}
		
		return "/login";
	}
}
