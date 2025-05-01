package com.project.demo001.controller;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
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

import com.project.demo001.domain.Location;
import com.project.demo001.domain.User;
import com.project.demo001.repository.LocationRepository;
import com.project.demo001.repository.UserRepository;
import com.project.demo001.service.UserService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AllRestController {

	private final UserRepository userRepository;
	private final UserService userService;
	private final LocationRepository locationRepository;
	
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
	
	@GetMapping("/api/notice")
    public String getTrafficEvents() throws Exception {
		String apiUrl = "";
		String apiKey = "";
		String fullUrl = "";
		
		URL url = new URL(fullUrl);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("GET");
		
		BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
		StringBuilder result = new StringBuilder();
		String line;
		while ((line=rd.readLine())!=null){
			result.append(line);
		}
		rd.close();
		conn.disconnect();
		
		return result.toString(); // XML 그대로 반환 (또는 필요한 값만 추출해도 OK)
    }
	
	@PostMapping("/api/save-location")
	public ResponseEntity<String> saveLocation(@RequestBody Map<String, Object> locationData){
		
	    try {
	    	double latitude = Double.parseDouble(locationData.get("latitude").toString());
	    	double longitude = Double.parseDouble(locationData.get("longitude").toString());
	    	
	    	Location loc = new Location();
	    	loc.setLatitude(latitude);
	    	loc.setLongitude(longitude);
	    	
	    	locationRepository.save(loc);
	        // 위치 저장 처리
	        return ResponseEntity.ok("위치 저장 성공");
	    } catch (Exception e) {
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류 발생");
	    }
	}
	

	
	
}
