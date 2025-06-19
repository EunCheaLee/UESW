package com.project.demo001.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class webConfig {

	@Bean
	public WebMvcConfigurer corsConfigurer() {
		return new WebMvcConfigurer() {

			@Override
			public void addCorsMappings(CorsRegistry registry) {
				registry.addMapping("/**")
						.allowedOrigins("http://127.0.0.1:5000", "http://localhost:5000", "http://localhost:9090") // Flask
																													// 서버
						.allowedMethods("*").allowedHeaders("*").allowCredentials(true);
			}

			// webConfig.java
			@Override
			public void addResourceHandlers(ResourceHandlerRegistry registry) {
				// 1. 프로필 이미지 매핑
				String uploadPath = System.getProperty("user.dir") + "/uploads/";
				registry.addResourceHandler("/uploads/**").addResourceLocations("file:///" + uploadPath);

				// 2. .well-known 경로 무시용 또는 대체 리소스 지정
				registry.addResourceHandler("/.well-known/**").addResourceLocations("classpath:/static/empty/"); // 📁 이
																													// 경로는
																													// 만들어두세요

				// 이런 경로는 처리하지 않음
				registry.addResourceHandler("/appspecific/**").addResourceLocations("classpath:/static/empty/");
			}
		};
	}

	@Bean
	public RestTemplate restTemplate() {
		SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
		factory.setConnectTimeout(5000); // 연결 타임아웃 5초
		factory.setReadTimeout(5000); // 응답 대기 타임아웃 5초
		return new RestTemplate(factory);
	}

}
