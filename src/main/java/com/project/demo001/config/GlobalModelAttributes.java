package com.project.demo001.config;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.project.demo001.domain.User;

import jakarta.servlet.http.HttpSession;

@ControllerAdvice
public class GlobalModelAttributes {

	@ModelAttribute("isLoggedIn")
	public boolean isLoggedIn(HttpSession session) {
		return session.getAttribute("loggedInUser")!=null;
	}
	
	@ModelAttribute("loggedInUser")
	public User loggedInUser(HttpSession session) {
		return (User) session.getAttribute("loggedInUser");
	}
	
}
