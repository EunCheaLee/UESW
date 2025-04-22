package com.project.demo001.service;

import java.io.IOException;

import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.project.demo001.domain.Flask;

import jakarta.annotation.PostConstruct;


@Service
public class FlaskService {

	private final RestTemplate restTemplate;
	
	public FlaskService() {
		this.restTemplate = new RestTemplate();
	}
	
	public Flask getTrafficData(int year, String region) {
		
        // Flask 서버의 URL을 동적으로 생성
        String apiUrl = "http://localhost:5000/data/" + year + "/" + region;

        try {
        	// RestTemplate으로 GET 요청
        	ResponseEntity<Flask> response = restTemplate.exchange(apiUrl, HttpMethod.GET, null, Flask.class);
			
//        	성공적으로 데이터를 받았다면 반환
        	return response.getBody();
		} catch (HttpClientErrorException|HttpServerErrorException e) {
			e.printStackTrace();
			return null;
		}
		
//		Flask traffic = restTemplate.getForObject(apiUrl, Flask.class);
		
//		return traffic;
	}
	
	@PostConstruct
	public void init() {
		new Thread(this::startFlaskServer).start();
	}
	
//	Flask 서버 실행
	public void startFlaskServer()  {
		try {
			// resources/python/month/app.py 파일 경로 가져오기
            String resourcePath = "src/main/resources/python/month/app";
			
//			Flask 서버를 실행할 명령어
			ProcessBuilder processBuilder = new ProcessBuilder();
			
			processBuilder.environment().put("FLASK_APP", resourcePath);  // FLASK_APP 환경 변수 설정
			processBuilder.command("flask","run");	// flask run 명령어
			
			// Flask 실행 전에 FLASK_APP 환경 변수를 설정
			processBuilder.inheritIO();	// 콘솔 출력을 Spring Boot 어플리케이션의 콘솔로 출력하도록 설정
			Process process = processBuilder.start();
			
			// Flask 서버의 실행 상태를 확인
	        process.waitFor();  // 서버가 종료될 때까지 기다림
		} catch (IOException |InterruptedException e) {
			e.printStackTrace();
		}
	}
	
}
