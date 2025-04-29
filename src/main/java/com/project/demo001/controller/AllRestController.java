package com.project.demo001.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.project.demo001.domain.User;
import com.project.demo001.repository.UserRepository;
import com.project.demo001.service.UserService;

import jakarta.servlet.http.HttpSession;
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
    public ResponseEntity<String> registerUser(@RequestBody User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("이미 존재하는 이메일입니다.");
        }

        userRepository.save(user);
        return ResponseEntity.ok("회원가입 성공!");
    }
	
    @GetMapping("/api/menu-login")
    public String checkLoginStatus(HttpSession session) {
        if (session.getAttribute("loggedInUser") != null) {  // 고쳤어
            return "already-logged-in";
        } else {
            return "not-logged-in";
        }
    }
    
    @PostMapping("/api/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> loginData, HttpSession session) {
        String account = loginData.get("account");
        String password = loginData.get("password");

        User user = userService.getUserByAccount(account); // DB에서 유저 조회

        Map<String, Object> response = new HashMap<>();

        if (user != null && user.getPassword().equals(password)) {
            session.setAttribute("loggedInUser", user);  // 세션에 사용자 정보 저장
            response.put("success", true);
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }
    
    @PostMapping("/api/logout")
    public ResponseEntity<String> apiLogout(HttpSession session) {
        if (session != null) {
            session.invalidate();
        }
        return ResponseEntity.ok("logged-out");
    }
	
	@GetMapping("/traffic-events")
    public ResponseEntity<Object> getTrafficEvents() {
        String apiKey = "dd094983b6bb4bd99860e397c52eae38";
        String url = "https://openapi.its.go.kr:9443/eventInfo?apiKey=" + apiKey + "&type=all&getType=json";

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Object> response = restTemplate.exchange(url, HttpMethod.GET, null, Object.class);

        return response;
    }
	
	
	
}
