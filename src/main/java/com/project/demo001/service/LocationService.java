package com.project.demo001.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class LocationService {

    private final RestTemplate restTemplate;

    LocationService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

	public String getWeatherInfo(double lat, double lon) {
		
		String apikey = "8d7e97191ae9cf92a3a4e3b1e90b8651";
		String url = "https://api.openweathermap.org/data/2.5/weather?lat=" + lat + "&lon=" + lon + "&appid=" + apikey + "&units=metric&lang=kr";
		
		RestTemplate restTemplate = new RestTemplate();
		return restTemplate.getForObject(url, String.class);
	}
	
}
