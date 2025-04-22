package com.project.demo001.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

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
	
}
