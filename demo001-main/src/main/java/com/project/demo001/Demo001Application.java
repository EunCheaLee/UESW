package com.project.demo001;

import java.io.IOException;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.PostConstruct;

@SpringBootApplication
public class Demo001Application {

	public static void main(String[] args) {
		SpringApplication.run(Demo001Application.class, args);
	}
	
    @Configuration
    public class AppConfig {

        @Bean
        public RestTemplate restTemplate() {
            return new RestTemplate();
        }
    }
    
    @PostConstruct
    public void runFlask() {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("python", "경로/your_flask_file.py");
            processBuilder.redirectErrorStream(true);
            processBuilder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
