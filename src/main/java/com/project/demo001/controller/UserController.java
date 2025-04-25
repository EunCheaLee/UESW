package com.project.demo001.controller;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.project.demo001.domain.Flask;
import com.project.demo001.domain.User;
import com.project.demo001.repository.UserRepository;
import com.project.demo001.service.FlaskService;
import com.project.demo001.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class UserController {

	private final UserService userservice;
	private final UserRepository userRepository;
	
	@Autowired
	private FlaskService flaskService;
	
	@GetMapping("/session/refresh")
	@ResponseBody
	public ResponseEntity<String> refreshSession(HttpSession session) {
	    if (session == null || session.getAttribute("user") == null) {
	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Session expired");
	    }

	    session.setAttribute("refreshed", System.currentTimeMillis());
	    return ResponseEntity.ok("OK");
	}
	
	@GetMapping({"/","/home","/main"})
	public String homePage(HttpSession session,
			@RequestParam(value = "year", required = false) Integer year, 
            @RequestParam(value = "region", required = false) String region, 
            Model model) {
		
		/* 데이터 분석 화면에 출력하기 */
		
	    // year와 region에 기본값 설정
	    year = (year != null) ? year : 2025;  // 기본값 2025 설정
	    region = (region != null) ? region : "defaultRegion";  // 기본값 "defaultRegion" 설정
	    
	    // Flask API로부터 데이터 받기
	    Flask flask = flaskService.getTrafficData(year, region);
	    
	    // flask 객체가 null이면 기본값 설정
	    if (flask == null) {
	        flask = new Flask();  // 기본값 설정
	        flask.setLabels(new ArrayList<>());
	        flask.set사고건수(new ArrayList<>());
	        flask.set사망자수(new ArrayList<>());
	        flask.set부상자수(new ArrayList<>());
	    }
	    
	    // 받은 데이터를 Thymeleaf 모델에 추가
	    model.addAttribute("flask", flask);
	    
//		ThymeLeaf 템플릿 이름 반환
		return "home";
	}
	
	@PostMapping("/register")
	public String register(User user, Model model) {
		userservice.registerUser(user);
		
        model.addAttribute("activeTab", "sign-up"); // 회원가입 탭 열기
		
//		alert 메시지를 alert.html로 전달
		model.addAttribute("message", "회원가입 완료!");
		model.addAttribute("redirectUrl", "/");
		
		return "alert";	// alert.html 뷰로 이동
	}
	
	@GetMapping("/sidebar")
	public String sideBar() {
		
		return "/home/sidebar";
	}
	
    @GetMapping("/login")
    public String loginPage(HttpSession session) {
        if (session != null && session.getAttribute("loggedInUser") != null) {
            session.invalidate();
            return "redirect:/logout";
        }
        return "login/login";
    }
	
    @PostMapping("/login")
    public String login(HttpSession session, @RequestParam String account, @RequestParam String password) {
        // 사용자 인증 로직 (아이디, 비밀번호 체크)
        if (userservice.isValidUser(account, password)) {
            // 인증이 성공하면 세션에 사용자 정보 저장
        	User loggedInUser = userservice.getUserByAccount(account);
            session.setAttribute("loggedInUser", loggedInUser);
            return "redirect:/home";  // 로그인 후 홈으로 리디렉션
        }
        return "login/login";  // 인증 실패 시 로그인 페이지로 돌아옴
    }
    
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate(); // 세션 무효화
        return "redirect:/login"; // ✅ 템플릿 말고 바로 리디렉션
    }

	@GetMapping("/login/findId")
	public String findId() {
		return "login_find_id";
	}
	
	@GetMapping("/login/findPw")
	public String findPw() {
		return "login_find_pw";
	}
	
	
	@GetMapping("/info")
	public String myPage(HttpSession session, Model model) {
	    // 세션에서 로그인한 사용자 정보를 가져오기
	    User loggedInUser = (User) session.getAttribute("loggedInUser");
	    
	    if (loggedInUser != null) {
	        model.addAttribute("user", loggedInUser);  // 로그인한 사용자 정보를 모델에 추가
	        return "home/info";  // info.html로 이동
	    } else {
	        return "redirect:/login";  // 로그인하지 않은 경우 로그인 페이지로 리다이렉트
	    }
	}
	
}
