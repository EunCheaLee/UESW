package com.project.demo001.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.client.RestTemplate;

@Controller
public class FlaskController {

	@Autowired
	private RestTemplate restTemplate;
	
	@GetMapping("/call-carAccident")
	public void callCarAccident() {

		StringBuilder result = new StringBuilder();
		String urlstr="http://127.0.0.1:5000/carAccident";
		
		URL url = null;
		try {
			url = new URL(urlstr);
			 // HttpURLConnection을 통해 Flask 서버에 연결
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // GET 방식 요청 설정
            connection.setRequestMethod("GET");

            // 응답 코드 확인
            int responseCode = connection.getResponseCode();
            System.out.println("GET 요청에 대한 응답 코드: " + responseCode);

            // 응답이 정상일 경우 응답 데이터 읽기
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                // 서버로부터 받은 응답 출력
                System.out.println("서버 응답: " + response.toString());
            } else {
                System.out.println("요청 실패: 응답 코드 " + responseCode);
            }
			
			connection.disconnect();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
	}
	
}
