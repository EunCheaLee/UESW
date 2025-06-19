package com.project.demo001.controller;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.project.demo001.domain.Bus;
import com.project.demo001.domain.Location;
import com.project.demo001.domain.Seoul;
import com.project.demo001.domain.Subway;
import com.project.demo001.domain.User;
import com.project.demo001.repository.LocationRepository;
import com.project.demo001.repository.SeoulRepository;
import com.project.demo001.repository.UserRepository;
import com.project.demo001.service.BusService;
import com.project.demo001.service.FlaskService;
import com.project.demo001.service.UserService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AllRestController {

	private final UserService userService;
	private final BusService busService;
	private final FlaskService flaskService;
	private final UserRepository userRepository;
	private final LocationRepository locationRepository;
	private final SeoulRepository seoulRepository;
	private final PasswordEncoder passwordEncoder;

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
	public ResponseEntity<Map<String, String>> registerUser(@RequestBody User user) {
		if (userRepository.existsByEmail(user.getEmail())) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "이미 존재하는 이메일입니다."));
		}

		// ✅ 비밀번호 암호화
		user.setPassword(passwordEncoder.encode(user.getPassword()));

		userRepository.save(user);
		return ResponseEntity.ok(Map.of("message", "회원가입 성공!"));
	}

	@GetMapping("/api/menu-login")
	public String checkLoginStatus(HttpSession session) {
		if (session.getAttribute("loggedInUser") != null) { // 고쳤어
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
			session.setAttribute("loggedInUser", user); // 세션에 사용자 정보 저장
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
		while ((line = rd.readLine()) != null) {
			result.append(line);
		}
		rd.close();
		conn.disconnect();

		return result.toString(); // XML 그대로 반환 (또는 필요한 값만 추출해도 OK)
	}

	@PostMapping("/api/save-location")
	public ResponseEntity<String> saveLocation(@RequestBody Map<String, Object> locationData) {

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

	@GetMapping("/api/bus/markers")
	@ResponseBody
	public List<Map<String, Object>> getAllStations() {
		return busService.getAllStations();
	}

	@PostMapping("/api/navigation/routes")
	public ResponseEntity<?> getRoutes(@RequestBody Map<String, String> body) {
		String origin = body.get("origin");
		String destination = body.get("destination");
		String departureTime = body.get("departureTime");

		System.out.println("origin: " + origin);
		System.out.println("destination: " + destination);
		System.out.println("departureTime: " + departureTime);

		if (origin == null || destination == null) {
			return ResponseEntity.badRequest().body("출발지 또는 도착지가 없습니다.");
		}

		return userService.fetchRoute(origin, destination, departureTime);
	}

	@GetMapping("/api/station/autocomplete")
	public List<String> autocomplete(@RequestParam String keyword) {
		List<Subway> stations = flaskService.searchStations(keyword);
		return stations.stream().map(Subway::getStationName).collect(Collectors.toList());
	}

	@GetMapping("/api/seoul/all")
	public List<Seoul> getAllGu() {
		return seoulRepository.findAll();
	}

	@GetMapping("/api/news.json")
	public ResponseEntity<List<Map<String, Object>>> getNews() {
		List<Map<String, Object>> extractedNewsData = new ArrayList<>();

		String searchQuery = "교통";
		String searchUrl = "https://search.daum.net/search?w=news&q=" + searchQuery;

		try {
			// 1단계: 검색 결과 페이지 요청
			Document doc = Jsoup.connect(searchUrl).userAgent(
					"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.212 Safari/537.36")
					.timeout((int) TimeUnit.SECONDS.toMillis(10)).get();

			Elements items = doc.select("div.item-bundle-mid");

			int count = 0;
			for (Element item : items) {
				if (count++ >= 20)
					break;

				String titleText = "";
				String articleUrl = "";
				String thumbnailUrl = "https://placehold.co/100x70/E0E0E0/555555?text=No+Image";
				String description = "";

				Element titleElement = item.selectFirst("strong.tit-g a, a.link_txt");
				if (titleElement != null) {
					titleText = titleElement.text();
					articleUrl = titleElement.attr("href");
				}

				Element descElement = item.selectFirst("p.conts-desc, p.desc_g");
				if (descElement != null) {
					description = descElement.text();
				}

				// 2단계: 기사 본문에서 이미지 크롤링 시도
				try {
					Document articleDoc = Jsoup.connect(articleUrl).userAgent(
							"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.212 Safari/537.36")
							.timeout((int) TimeUnit.SECONDS.toMillis(10)).get();

					Element imgElement = articleDoc.selectFirst("figure.figure_frm.origin_fig img");
					if (imgElement != null) {
						String potentialUrl = imgElement.hasAttr("data-org-src") ? imgElement.attr("data-org-src")
								: imgElement.hasAttr("src") ? imgElement.attr("src")
										: imgElement.hasAttr("data-original-loaded")
												? imgElement.attr("data-original-loaded")
												: null;

						if (potentialUrl != null) {
							if (potentialUrl.startsWith("//"))
								potentialUrl = "https:" + potentialUrl;
							if (potentialUrl.length() >= 10
									&& (potentialUrl.startsWith("http") || potentialUrl.startsWith("https"))) {
								thumbnailUrl = potentialUrl;
							}
						}
					}
				} catch (Exception e) {
					// 썸네일 파싱 실패시 무시
				}

				// 최종 데이터 추가
				Map<String, Object> newsItem = new HashMap<>();
				newsItem.put("title", titleText);
				newsItem.put("link", articleUrl);
				newsItem.put("thumbnail", thumbnailUrl);
				newsItem.put("description", description);

				extractedNewsData.add(newsItem);
			}

			return ResponseEntity.ok(extractedNewsData);

		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.internalServerError().build();
		}
	}
}
