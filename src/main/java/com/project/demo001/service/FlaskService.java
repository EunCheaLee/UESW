package com.project.demo001.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.demo001.domain.Flask;
import com.project.demo001.domain.Flask.StationData;
import com.project.demo001.domain.Subway;
import com.project.demo001.repository.SubwayRepository;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class FlaskService {

	private final SubwayRepository subwayRepository;

	private final RestTemplate restTemplate = new RestTemplate();
	private static final String API_KEY = (키);
	private static final List<String> DEFAULT_STATIONS = List.of("서울");

	private static final Map<String, String> SUBWAY_LINE_MAP = Map.ofEntries(Map.entry("1001", "1호선"),
			Map.entry("1002", "2호선"), Map.entry("1003", "3호선"), Map.entry("1004", "4호선"), Map.entry("1005", "5호선"),
			Map.entry("1006", "6호선"), Map.entry("1007", "7호선"), Map.entry("1008", "8호선"), Map.entry("1009", "9호선"),
			Map.entry("1063", "경의중앙선"), Map.entry("1075", "분당선"), Map.entry("1077", "신분당선"), Map.entry("1092", "우이신설선"),
			Map.entry("1093", "서해선"), Map.entry("1094", "김포골드라인"), Map.entry("1095", "수인선"), Map.entry("1096", "에버라인"),
			Map.entry("1097", "의정부경전철"), Map.entry("1098", "인천1호선"), Map.entry("1099", "인천2호선"),
			Map.entry("1041", "공항철도"), Map.entry("1032", "공항철도"));

	@PostConstruct
	public void init() {
		new Thread(this::startFlaskServer).start();
	}

	public void startFlaskServer() {
		try {
			ProcessBuilder processBuilder = new ProcessBuilder("flask", "run");
			processBuilder.directory(new File("src/main/resources/python/py"));
			processBuilder.environment().put("FLASK_APP", "app.py");
			processBuilder.inheritIO();
			processBuilder.start(); // 비동기 실행이므로 waitFor는 제거
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void initSession(HttpSession session) {
		if (session.getAttribute("stations") == null)
			session.setAttribute("stations", new ArrayList<>(DEFAULT_STATIONS));
		if (session.getAttribute("favorites") == null)
			session.setAttribute("favorites", new ArrayList<>());
		if (session.getAttribute("station_cache") == null)
			session.setAttribute("station_cache", new HashMap<>());
	}

	public void handleAction(HttpSession session, String station, String action) {
		List<String> stations = (List<String>) session.getAttribute("stations");
		List<String> favorites = (List<String>) session.getAttribute("favorites");
		Map<String, List<Flask>> cache = (Map<String, List<Flask>>) session.getAttribute("station_cache");

		switch (action) {
		case "add" -> {
			if (!stations.contains(station)) {
				stations.add(station);
				session.setAttribute("message", "✅ \"" + station + "\" 역이 추가되었습니다.");
			} else {
				session.setAttribute("message", "ℹ️ \"" + station + "\" 역은 이미 있습니다.");
			}
		}
		case "remove" -> {
			stations.remove(station);
			cache.remove(station);
			session.setAttribute("message", "🗑️ \"" + station + "\" 역이 삭제되었습니다.");
		}
		case "favorite" -> {
			if (favorites.contains(station)) {
				favorites.remove(station);
				session.setAttribute("message", "⭐ \"" + station + "\" 즐겨찾기에서 제거됨.");
			} else {
				favorites.add(station);
				session.setAttribute("message", "⭐ \"" + station + "\" 즐겨찾기로 등록됨.");
			}
		}
		}
	}

	public List<StationData> getStationData(HttpSession session, boolean favonly, String refreshStation) {
		List<String> stations = (List<String>) session.getAttribute("stations");
		List<String> favorites = (List<String>) session.getAttribute("favorites");
		Map<String, List<Flask>> cache = (Map<String, List<Flask>>) session.getAttribute("station_cache");

		List<String> stationsToShow = favonly ? stations.stream().filter(favorites::contains).toList() : stations;

		List<StationData> result = new ArrayList<>();

		for (String station : stationsToShow) {
			List<Flask> arrivals;
			if (station.equals(refreshStation) || !cache.containsKey(station)) {
				arrivals = getArrivalsFromApi(station);
				cache.put(station, arrivals);
			} else {
				arrivals = cache.get(station);
			}
			result.add(new StationData(station, arrivals));
		}
		return result;
	}

	public List<Flask> getArrivalsFromApi(String station) {
		try {
			String url = "http://swopenapi.seoul.go.kr/api/subway/" + API_KEY + "/json/realtimeStationArrival/0/10/"
					+ station;
			String body = restTemplate.getForObject(url, String.class);

			ObjectMapper mapper = new ObjectMapper();
			JsonNode root = mapper.readTree(body);

			JsonNode list = root.get("realtimeArrivalList");

			// ❗ 오류 응답 처리
			if (list == null || !list.isArray()) {
				System.err.println("⛔ API 응답 이상: " + body); // 또는 log.warn(...)
				return new ArrayList<>();
			}

			List<Flask> result = new ArrayList<>();
			for (JsonNode node : list) {
				int seconds = node.path("barvlDt").asInt(0);
				String subwayId = node.path("subwayId").asText();

				Flask f = new Flask();
				f.setTrainLineNm(node.path("trainLineNm").asText());
				f.setUpdnLine(node.path("updnLine").asText());
				f.setArvlMsg2(node.path("arvlMsg2").asText());
				f.setRemain_seconds(seconds);
				f.setRemain_time(seconds > 0 ? (seconds / 60) + "분 " + (seconds % 60) + "초" : "도착");
				f.setLine_name(SUBWAY_LINE_MAP.getOrDefault(subwayId, subwayId + "호선"));

				result.add(f);
			}
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return List.of();
		}
	}

	@Transactional
	public boolean deleteStationByName(String stationName) {
		try {
			subwayRepository.deleteByStationName(stationName);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public List<Subway> searchStations(String keyword) {
		return subwayRepository.searchByStationNameOrChosung(keyword);
	}

	public List<String> getAllStations() {
		List<String> stations = subwayRepository.findAllStationNames();

		// ✅ 불량 데이터 정제
		List<String> cleanedStations = stations.stream().filter(s -> s != null && !s.trim().isEmpty())
				.collect(Collectors.toList());

		return cleanedStations;
	}

}
