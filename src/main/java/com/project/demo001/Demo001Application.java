package com.project.demo001;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import com.project.demo001.service.FlaskService;

import jakarta.annotation.PostConstruct;

@SpringBootApplication
public class Demo001Application implements CommandLineRunner {

	@Autowired
	private FlaskService flaskService;
	
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
    
    @Override
    public void run(String... args) throws Exception {
    	flaskService.startFlaskServer();
    }
    
    

}
