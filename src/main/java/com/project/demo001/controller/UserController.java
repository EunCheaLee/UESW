package com.project.demo001.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.project.demo001.domain.User;
import com.project.demo001.repository.UserRepository;
import com.project.demo001.service.FlaskService;
import com.project.demo001.service.UserService;

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
		if (session == null || session.getAttribute("loggedInUser") == null) { // 고쳤어!
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Session expired");
		}

		session.setAttribute("refreshed", System.currentTimeMillis());
		return ResponseEntity.ok("OK");
	}

	@GetMapping({ "/", "/home", "/main" })
	public String homePage(HttpSession session, Model model) {

//		ThymeLeaf 템플릿 이름 반환
		return "home";
	}

	@GetMapping("/weather")
	public String weatherPage() {
		return "home/weather"; // resources/templates/weather.html을 렌더링합니다.
	}

	@PostMapping("/register")
	public String register(User user, Model model) {
		userservice.registerUser(user);

		model.addAttribute("activeTab", "sign-up"); // 회원가입 탭 열기

//		alert 메시지를 alert.html로 전달
		model.addAttribute("message", "회원가입 완료!");
		model.addAttribute("redirectUrl", "/");

		return "alert"; // alert.html 뷰로 이동
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

	@GetMapping("/logout")
	public String logout(HttpSession session) {
		if (session != null) {
			session.invalidate(); // 세션 전체 무효화
		}
		return "redirect:/login"; // 로그인 페이지로 리다이렉션
	}

	@GetMapping("/login_find")
	public String loginFind() {

		return "login/login_find";
	}

	@GetMapping("/info")
	public String myPage(HttpSession session, Model model) {
		User loggedInUser = (User) session.getAttribute("loggedInUser");

		if (loggedInUser != null) {
			// DB에서 유저 정보를 다시 가져옴 (안전하게 최신 정보 조회)
			User user = userRepository.findById(loggedInUser.getId()).orElse(null);

			if (user != null) {
				// fullAddress 추출
				String address = user.getFullAddress();

				// null 체크부터 항상 안전하게
				if (address != null) {
					// 1. 맨 앞의 (숫자) 패턴 제거
					address = address.replaceFirst("^\\(\\d{5}\\)\\s*", "");

					// 2. 괄호 뒤에 줄바꿈 적용
					if (address.contains("(")) {
						address = address.replaceAll("(\\(.*?\\))", "$1<br>");
					}
				}

				// 모델에 담기
				model.addAttribute("address", address);
				model.addAttribute("user", user);
				model.addAttribute("now", System.currentTimeMillis());
			}

			return "home/info";
		} else {
			return "redirect:/login";
		}
	}

	@PostMapping("/user/profile/upload")
	public String uploadProfileImage(@RequestParam("file") MultipartFile file, HttpSession session) {

		User loggedInUser = (User) session.getAttribute("loggedInUser");

		if (loggedInUser == null || file.isEmpty()) {
			return "redirect:/login";
		}

		if (file.isEmpty()) {
			throw new IllegalArgumentException("파일이 비어있습니다.");
		}

		String contentType = file.getContentType();
		if (contentType == null || !contentType.startsWith("image/")) {
			throw new IllegalArgumentException("이미지 파일만 업로드 가능합니다.");
		}

		String originalFilename = file.getOriginalFilename();
		String extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
		List<String> allowedExt = Arrays.asList("jpg", "jpeg", "png", "gif", "bmp");

		if (!allowedExt.contains(extension)) {
			throw new IllegalArgumentException("허용되지 않는 파일 확장자입니다.");
		}

		try {
			String uploadDir = System.getProperty("user.dir") + "/uploads/profile/";
			File dir = new File(uploadDir);
			if (!dir.exists())
				dir.mkdirs();

			String filename = UUID.randomUUID() + "_"
					+ file.getOriginalFilename().replaceAll("[^a-zA-Z0-9\\.\\-]", "_");
			Path filePath = Paths.get(uploadDir, filename);
			Files.write(filePath, file.getBytes());

			// 기존 이미지 삭제 (선택사항)
			if (loggedInUser.getProfileImageUrl() != null) {
				Path oldPath = Paths.get(uploadDir, new File(loggedInUser.getProfileImageUrl()).getName());
				Files.deleteIfExists(oldPath);
			}

			// DB에 저장되는 경로는 이렇게
			loggedInUser.setProfileImageUrl("/uploads/profile/" + filename);
			userRepository.save(loggedInUser);
			session.setAttribute("loggedInUser", loggedInUser);

		} catch (IOException e) {
			e.printStackTrace();
		}

		return "redirect:/info";
	}

	@PostMapping("/user/profile/delete")
	public String deleteProfileImage(HttpSession session) {
		User loggedInUser = (User) session.getAttribute("loggedInUser");

		if (loggedInUser == null) {
			return "redirect:/login";
		}

		try {
			if (loggedInUser.getProfileImageUrl() != null) {
				String uploadDir = System.getProperty("user.dir") + "/uploads/profile/";
				Path imagePath = Paths.get(uploadDir, new File(loggedInUser.getProfileImageUrl()).getName());
				Files.deleteIfExists(imagePath);

				loggedInUser.setProfileImageUrl(null);
				userRepository.save(loggedInUser);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return "redirect:/info";
	}

	@PostMapping("/user/profile/update")
	public String updateProfile(@RequestParam String email, @RequestParam String address, Principal principal) {
		String account = principal.getName(); // 현재 로그인한 사용자의 계정
		userservice.updateUserInfo(account, email, address);
		return "redirect:/info"; // 수정 후 다시 마이페이지로 이동
	}

	@PostMapping("/user/profile/update-field")
	@ResponseBody
	public ResponseEntity<?> updateField(@RequestBody Map<String, String> data, Principal principal) {
		String account = principal.getName();
		String field = data.get("field");
		String value = data.get("value");

		boolean updated = userservice.updateField(account, field, value);
		if (updated) {
			return ResponseEntity.ok().build();
		} else {
			return ResponseEntity.badRequest().build();
		}
	}

}
