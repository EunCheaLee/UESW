package com.project.demo001.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.project.demo001.domain.User;
import com.project.demo001.service.UserService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class UserController {

	private final UserService service;
	
	@GetMapping("/")
	public String homePage() {
		
		return "home";
	}
	
	@PostMapping("/register")
	public String register(User user, Model model) {
		service.registerUser(user);
		
        model.addAttribute("activeTab", "sign-up"); // 회원가입 탭 열기
		
//		alert 메시지를 alert.html로 전달
		model.addAttribute("message", "회원가입 완료!");
		model.addAttribute("redirectUrl", "/");
		
		return "alert";	// alert.html 뷰로 이동
	}
	
	@GetMapping("/register")
	public String showRegisterForm() {
		return "login";
	}
	
	@GetMapping("/login")
	public String login(@RequestParam(value = "tab", required = false) String tab, Model model) {
		model.addAttribute("activeTab", tab);
		return "login";
	}
	
	@GetMapping("/login/findId")
	public String findId() {
		return "login_find_id";
	}
	
	@GetMapping("/login/findPw")
	public String findPw() {
		return "login_find_pw";
	}
}
