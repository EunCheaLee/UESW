package com.project.demo001.Filter;

import java.io.IOException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebFilter("/home")
public class LoginFilter implements Filter {

	@Override
	public void init(FilterConfig filterConfig) {}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest httpServletRequest = (HttpServletRequest) request;
		HttpServletResponse httpServletResponse = (HttpServletResponse) response;
	
//		세션에서 로그인 여부 확인
		Object loggedInUser = httpServletRequest.getSession().getAttribute("loggedInUser");
	
		if(loggedInUser == null) {
//			로그인하지 않은 경우 로그인 페이지로 리다이렉트
			httpServletResponse.sendRedirect("/login");
		} else {
//			로그인된 경우, 다음 필터로 요청 전달
			chain.doFilter(httpServletRequest, httpServletResponse);
		}
	}
	
	@Override
	public void destroy() {}
	
}
