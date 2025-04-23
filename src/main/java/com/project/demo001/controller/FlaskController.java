package com.project.demo001.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import com.project.demo001.domain.Flask;
import com.project.demo001.service.FlaskService;


@Controller
public class FlaskController {

	@Value("${flask.api.url}")
	private String flaskApiUrl;
	
	@Autowired
	private FlaskService flaskService;
	
	@GetMapping("/show-traffic-data")
	public String showTrafficData(int year, String region, Model model) {
//		Flask API로부터 데이터 받기
		Flask flask = flaskService.getTrafficData(year, region);
		
//		받은 데이터를 Thymeleaf 모델에 추가
		model.addAttribute("flask", flask);
		
//		ThymeLeaf 템플릿 이름 반환
		return "home";
	}
	
	@GetMapping("/monthly")
	public String showMonthlyPage(Model model) {
		model.addAttribute("title","월별 교통사고 통계");
		return "API/monthly";
	}
	
	@GetMapping("/data/{year}/{region}")
	@ResponseBody
	public Map<String, Object> getData(@PathVariable String year, @PathVariable String region) {
	    Map<String, Object> data = new HashMap<>();
	    data.put("labels", List.of("1월", "2월", "3월", "4월", "5월", "6월", "7월", "8월", "9월", "10월", "11월", "12월"));
	    data.put("사고건수", List.of(100, 110, 95, 130, 120, 140, 135, 125, 115, 105, 100, 90));
	    data.put("사망자수", List.of(1, 2, 0, 1, 1, 2, 1, 1, 0, 1, 1, 0));
	    data.put("부상자수", List.of(50, 55, 60, 70, 65, 75, 72, 68, 60, 58, 55, 50));
	    return data;
	}
	
}
