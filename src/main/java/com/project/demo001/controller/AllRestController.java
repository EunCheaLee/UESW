package com.project.demo001.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.project.demo001.domain.User;
import com.project.demo001.repository.UserRepository;
import com.project.demo001.service.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AllRestController {

	private final UserRepository userRepository;
	private final UserService userService;
	
	@GetMapping("/api/check-email")
	public boolean checkEmailDuplicate(@RequestParam String email) {
//		return userRepository.findByEmail(email).isPresent();
	
		try {
			userService.validateDuplicateEmail(email);
			return false;
		} catch (IllegalStateException e) {
			return true;
		}
	}
	
	@PostMapping("/api/register")
	public ResponseEntity<String> registerUser(@RequestBody User user){
		try {
			userService.registerUser(user);
			return ResponseEntity.ok("회원가입 성공");
		} catch (IllegalStateException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
	}
	
}
