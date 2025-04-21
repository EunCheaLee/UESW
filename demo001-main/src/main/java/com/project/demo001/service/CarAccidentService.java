package com.project.demo001.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class CarAccidentService {

    public String getCarAccidentData() {
        RestTemplate restTemplate = new RestTemplate();
        String url = "http://127.0.0.1:5000/";  // Flask 서버 URL
        return restTemplate.getForObject(url, String.class);
    }
	
}
