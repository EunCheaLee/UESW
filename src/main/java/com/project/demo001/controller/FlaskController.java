package com.project.demo001.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.project.demo001.domain.Flask;
import com.project.demo001.domain.Flask.StationData;
import com.project.demo001.domain.Subway;
import com.project.demo001.service.FlaskService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller
public class FlaskController {

	@Value("${flask.api.url}")
	private String flaskApiUrl;

	private final RestTemplate restTemplate = new RestTemplate();

	private final FlaskService flaskService;

	@GetMapping("/data_index")
	public String index(Model model) {

		return "traffic/data_index";
	}

	@GetMapping("/train_realtime")
	public String train_realtime(
	        @RequestParam(required = false) String favonly,
	        @RequestParam(required = false) String refresh_station,
	        HttpSession session, Model model) {

	    // 세션 초기화
	    flaskService.initSession(session);
	    boolean favOnlyFlag = "1".equals(favonly);

	    // 실시간 도착 정보
	    List<StationData> data = flaskService.getStationData(session, favOnlyFlag, refresh_station);

	    // ✅ 역 리스트 가져오기 (서비스에서 가져옴)
	    List<String> stations = flaskService.getAllStations();

	    // ✅ 여기서 철저하게 필터링 (null, 빈문자열, 공백제거)
	    List<String> cleanedStations = stations.stream()
	            .filter(s -> s != null && !s.trim().isEmpty())
	            .collect(Collectors.toList());

	    // 모델에 세팅
	    model.addAttribute("stations", cleanedStations);
	    model.addAttribute("data", data);
	    model.addAttribute("favorites", session.getAttribute("favorites"));
	    model.addAttribute("favonly", favOnlyFlag);
	    model.addAttribute("message", session.getAttribute("message"));
	    session.removeAttribute("message");

	    return "fragments/train_realtime";
	}

	@PostMapping("/train_realtime")
	@ResponseBody
	public String post_train(
			@RequestParam(required = false) String station,
			@RequestParam(required = false) String action, 
			HttpSession session) {

		if (station == null || action == null) {
			System.out.println(action);
			return "파라미터 누락";
		}

		flaskService.handleAction(session, station, action);
		return "성공";
	}

	@GetMapping("/train_realtime/fragment")
	public String trainRealtimeFragment(
			@RequestParam(required = false) String favonly, 
			HttpSession session, Model model) {
		
		flaskService.initSession(session);
		boolean favonlyFlag = "1".equals(favonly);
		List<StationData> data = flaskService.getStationData(session, favonlyFlag, null);

		model.addAttribute("data", data);
		model.addAttribute("favorites", session.getAttribute("favorites"));
		model.addAttribute("favonly", favonlyFlag);  
		return "fragments/train_realtime :: stationBlock";
	}

	@PostMapping("/train_realtime/delete")
	public ResponseEntity<?> deleteStation(@RequestBody Map<String, String> payload, HttpSession session) {
		String stationName = payload.get("stationName");
		flaskService.handleAction(session, stationName, "remove"); // 세션에서도 삭제
		boolean deleted = flaskService.deleteStationByName(stationName); // DB 삭제
		return deleted ? ResponseEntity.ok().build() : ResponseEntity.badRequest().body("삭제 실패");
	}

	@GetMapping("/subway")
	public String subway() {
		return "traffic/subway";
	}
	
	@GetMapping("/news")
    public String newsPage() {
        return "fragments/news";  // templates/fragments/news.html
    }

}