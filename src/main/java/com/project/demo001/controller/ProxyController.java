package com.project.demo001.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/proxy")
public class ProxyController {

	private final RestTemplate restTemplate = new RestTemplate();
	private static final String API_KEY = (키);
	private static final String API_KEY_BUS = (키);

	@GetMapping("/subway")
	public ResponseEntity<String> proxySubway(@RequestParam String station) {
		try {
			String url = "http://swopenapi.seoul.go.kr/api/subway/" + API_KEY + "/json/realtimeStationArrival/0/10/"
					+ station;
			String result = restTemplate.getForObject(url, String.class);
			return ResponseEntity.ok(result);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("API 호출 실패");
		}
	}

	@GetMapping("/bus")
	public ResponseEntity<String> proxyBus(@RequestParam String route) {
		try {
			// 1️⃣ API 키 준비
			String serviceKey = API_KEY_BUS;

			// 2️⃣ 외부 요청 URL 구성
			String urlStr = String.format(
					"http://ws.bus.go.kr/api/rest/arrive/getArrInfoByRouteAll" + "?serviceKey=%s&busRouteId=%s",
					serviceKey, route);

			// 3️⃣ 요청 객체 생성 및 연결 설정
			URL url = new URL(urlStr);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");

			// 4️⃣ 응답 스트림 읽기
			BufferedReader in = new BufferedReader(
					new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));

			StringBuilder result = new StringBuilder();
			String inputLine;
			while ((inputLine = in.readLine()) != null) {
				result.append(inputLine);
			}
			in.close();

			// 5️⃣ 응답 반환
			return ResponseEntity.ok(result.toString());

		} catch (MalformedURLException e) {
			System.err.println("📛 URL 형식 오류: " + e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("잘못된 URL 요청 형식입니다.");

		} catch (IOException e) {
			System.err.println("🌐 통신 오류: " + e.getMessage());
			return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT).body("버스 도착 정보를 가져오는 데 실패했습니다.");

		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("❌ 알 수 없는 오류가 발생했습니다.");
		}
	}

}
