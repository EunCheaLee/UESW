package com.project.demo001.controller;

//JSON 파싱 관련
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;

//Java 유틸
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestTemplate;


@Controller
public class CarAccidentController {

	
	@Autowired
	private RestTemplate restTemplate;
	@Autowired
    private FlaskController flaskController;
	
	@GetMapping("/carAccident")
	public String getCarAccident(Model model) throws JsonProcessingException {
        try {
            // 1. Flask 서버에서 데이터 가져오기 (JSON 형태 문자열)
            String flaskResponse = restTemplate.getForObject("http://127.0.0.1:5000/carAccident", String.class);

            // 2. JSON 문자열을 Map으로 변환
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> resultMap = objectMapper.readValue(
                    flaskResponse,
                    new TypeReference<Map<String, Object>>() {}
            );

            // 3. Thymeleaf로 데이터 전달
            model.addAttribute("years", resultMap.get("years"));
            model.addAttribute("사고건수", resultMap.get("사고건수"));
            model.addAttribute("사망자수", resultMap.get("사망자수"));
            model.addAttribute("중상자수", resultMap.get("중상자수"));
            model.addAttribute("부상자수", resultMap.get("부상자수"));

        } catch (Exception e) {
            e.printStackTrace();
        }

        return "carAccident"; // src/main/resources/templates/carAccident.html
    }
	
}
