package com.project.demo001.Filter;

import java.io.IOException;

import org.springframework.stereotype.Component;

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
		HttpServletRequest httpServletRequest = (HttpServletRequest) request;
		HttpServletResponse httpServletResponse = (HttpServletResponse) response;
		
		HttpSession session = httpServletRequest.getSession(false);
	
        // 세션이 없으면 새로 생성하여 사용자 정보를 세션에 저장
        if (session == null) {
            session = httpServletRequest.getSession(true); // 새로운 세션 생성
            Object user = "someUserObject"; // 사용자 정보 (로그인 후 설정한 정보)
            session.setAttribute("user", user); // 세션에 사용자 정보 저장
        }

        // 세션 정보가 있을 경우, 필요한 로직을 계속 처리
        if (session != null) {
            Object user = session.getAttribute("user"); // 세션에서 사용자 정보 가져오기
            // 세션에 저장된 사용자 정보로 필요한 처리를 진행
        }

        // 다음 필터나 서블릿으로 요청을 전달
        chain.doFilter(request, response);
		
	}
	@Override
	public void destroy() {}
	
}
