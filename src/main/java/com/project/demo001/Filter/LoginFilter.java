package com.project.demo001.Filter;

import java.io.IOException;

import org.springframework.stereotype.Component;

import com.project.demo001.domain.User;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Component
public class LoginFilter implements Filter {

	@Override
	public void init(FilterConfig filterConfig) {}
	@Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;

        HttpSession session = httpRequest.getSession(false); // 세션이 없으면 null
        if (session != null) {
            User loggedInUser = (User) session.getAttribute("loggedInUser");
            if (loggedInUser != null) {
                // 로그인한 사용자 정보를 request에 넣어줌 (컨트롤러에서 꺼내쓰기 편하게)
                httpRequest.setAttribute("loginUser", loggedInUser);
            }
        }

        chain.doFilter(request, response); // 계속 진행
    }
	@Override
	public void destroy() {}
	
}
