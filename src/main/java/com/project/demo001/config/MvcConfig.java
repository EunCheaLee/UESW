package com.project.demo001.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MvcConfig implements WebMvcConfigurer {

	@Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // /login.html 요청 → templates/login/login.html 렌더링
        registry.addViewController("/login.html").setViewName("login/login");
        // /info.html 요청 → templates/info.html 렌더링
        registry.addViewController("/info.html").setViewName("info");
    }
	
}
