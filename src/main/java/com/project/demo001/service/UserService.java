package com.project.demo001.service;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.project.demo001.domain.Address;
import com.project.demo001.domain.User;
import com.project.demo001.repository.AddressRepository;
import com.project.demo001.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

	@Autowired
	private final UserRepository userRepository;
	private final AddressRepository addressRepository;
	private final PasswordEncoder passwordEncoder;

	public void updateLastConnect(User user) { // 로그인 성공 시 갱신
		user.setLastConnect(LocalDateTime.now());
		userRepository.save(user);
	}

	// User를 ID로 조회하는 메서드
	public User findUserById(Long userId) {
		return userRepository.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("사용자가 존재하지 않습니다. ID: " + userId));
	}

//    회원가입 이메일 중복 체크
	public void registerUser(User user) {
		validateDuplicateEmail(user.getEmail());

		// ⚠️ 이거 빠졌으면 절대 안 됨!
		String encoded = passwordEncoder.encode(user.getPassword());
		user.setPassword(encoded);
		user.setProvider("site");

		// 1. 중간 도로명 추출 후 user에 설정
		String streetName = extractStreetName(user.getFullAddress());
		user.setStreetName(streetName);

		userRepository.save(user);

		// 3. Address 테이블에 동일 streetName이 없을 때만 저장
		Optional<Address> exist = addressRepository.findByStreetName(streetName);
		if (exist.isEmpty()) {
			Address address = new Address();
			address.setFullAddress(user.getFullAddress());
			address.setStreetName(streetName);
			addressRepository.save(address);
		}
	}

	public void validateDuplicateEmail(String email) {
		if (userRepository.findByEmail(email).isPresent()) {
			throw new IllegalStateException("이미 존재하는 이메일입니다.");
		}
	}

	public boolean isValidUser(String account, String password) {
		User user = userRepository.findByAccount(account);
		return user != null && passwordEncoder.matches(password, user.getPassword());
	}

	public User getUserByAccount(String account) {
		return userRepository.findByAccount(account);
	}

	@Override
	public UserDetails loadUserByUsername(String account) throws UsernameNotFoundException {
		User user = userRepository.findByAccount(account);
		if (user == null) {
			throw new UsernameNotFoundException("사용자 없음");
		}

		return new org.springframework.security.core.userdetails.User(user.getAccount(), user.getPassword(), // 🔐 이미
																												// 암호화된
																												// 비밀번호
				List.of(new SimpleGrantedAuthority("ROLE_USER")));
	}

	public User getUserByEmail(String email) {
		return userRepository.findByEmail(email).orElse(null); // 또는 예외를 던질 수도 있음
	}

	public User findUserByUserName(String userName) {
		return userRepository.findByUserName(userName);
	}

	public String extractStreetName(String fullAddress) {
		Pattern pattern = Pattern.compile("(\\S+\\s\\d+)");
		Matcher matcher = pattern.matcher(fullAddress);
		if (matcher.find()) {
			return matcher.group(1); // 예: "종로 69"
		}
		return null;
	}

	@Value("${kakao.rest-api-key}")
	private String kakaoRestApiKey;
	private final RestTemplate restTemplate = new RestTemplate();

	public ResponseEntity<?> fetchRoute(String origin, String destination, String departureTimeStr) {
		if (origin == null || destination == null) {
			return ResponseEntity.badRequest().body(Map.of("error", "출발지 또는 도착지가 누락되었습니다."));
		}

		double[] originCoords = getCoords(origin);
		double[] destCoords = getCoords(destination);

		if (originCoords == null || destCoords == null) {
			return ResponseEntity.badRequest().body(Map.of("error", "주소 검색 실패"));
		}

		long departureTimestamp = 0L;
		if (departureTimeStr != null && departureTimeStr.matches("\\d{2}:\\d{2}")) {
			String[] parts = departureTimeStr.split(":");
			LocalDateTime dt = LocalDateTime.of(LocalDate.now(),
					LocalTime.of(Integer.parseInt(parts[0]), Integer.parseInt(parts[1])));
			if (dt.isBefore(LocalDateTime.now())) {
				dt = dt.plusDays(1);
			}
			departureTimestamp = dt.toEpochSecond(ZoneOffset.of("+9"));
		}

		String url = String.format(
				"https://apis-navi.kakaomobility.com/v1/directions?origin=%.6f,%.6f&destination=%.6f,%.6f&priority=RECOMMEND",
				originCoords[0], originCoords[1], destCoords[0], destCoords[1]);

		if (departureTimestamp > 0) {
			url += "&departure_time=" + departureTimestamp;
		}

		HttpHeaders headers = new HttpHeaders();
		headers.set("Authorization", "KakaoAK " + kakaoRestApiKey);

		HttpEntity<Void> entity = new HttpEntity<>(headers);
		ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);

		if (!response.getStatusCode().is2xxSuccessful()) {
			return ResponseEntity.status(500).body(Map.of("error", "경로 요청 실패"));
		}

		Map<String, Object> route = (Map<String, Object>) ((List<?>) response.getBody().get("routes")).get(0);
		Map<String, Object> summary = (Map<String, Object>) route.get("summary");
		List<Map<String, Object>> roads = (List<Map<String, Object>>) ((Map<String, Object>) ((List<?>) route
				.get("sections")).get(0)).get("roads");

		List<Map<String, Double>> pathCoords = new ArrayList<>();
		for (Map<String, Object> road : roads) {
			List<Double> vertices = (List<Double>) road.get("vertexes");
			for (int i = 0; i < vertices.size(); i += 2) {
				pathCoords.add(Map.of("x", vertices.get(i), "y", vertices.get(i + 1)));
			}
		}

		return ResponseEntity.ok(Map.of("summary", summary, "path", pathCoords, "start",
				Map.of("x", originCoords[0], "y", originCoords[1]), "end",
				Map.of("x", destCoords[0], "y", destCoords[1])));
	}

	private double[] getCoords(String query) {
		String url = "https://dapi.kakao.com/v2/local/search/keyword.json?query=" + query;
		HttpHeaders headers = new HttpHeaders();
		headers.set("Authorization", "KakaoAK " + kakaoRestApiKey);

		HttpEntity<Void> entity = new HttpEntity<>(headers);
		ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);

		List<Map<String, Object>> docs = (List<Map<String, Object>>) response.getBody().get("documents");
		if (docs != null && !docs.isEmpty()) {
			double x = Double.parseDouble((String) docs.get(0).get("x"));
			double y = Double.parseDouble((String) docs.get(0).get("y"));
			return new double[] { x, y };
		}
		return null;
	}

	// 이메일, 주소 업데이트 메서드
	public void updateUserInfo(String account, String email, String address) {
		User user = userRepository.findByAccount(account);
		if (user != null) {
			user.setEmail(email);
			user.setFullAddress(address);
			userRepository.save(user);
		}
	}

	public boolean updateField(String account, String field, String value) {
		User user = userRepository.findByAccount(account);
		if (user == null)
			return false;

		switch (field) {
		case "email":
			user.setEmail(value);
			break;
		case "address":
			user.setFullAddress(value);
			break;
		default:
			return false;
		}
		userRepository.save(user);
		return true;
	}
}