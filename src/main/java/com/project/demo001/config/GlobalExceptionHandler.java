package com.project.demo001.config;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.NoHandlerFoundException;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public String handleSpringAccessDeniedException(org.springframework.security.access.AccessDeniedException ex, Model model) {
        model.addAttribute("status", 403);
        model.addAttribute("message", "권한이 없습니다.");
        return "error/access-denied";
    }

    @ExceptionHandler(java.nio.file.AccessDeniedException.class)
    public String handleJavaAccessDeniedException(java.nio.file.AccessDeniedException ex, Model model) {
        model.addAttribute("status", 403);
        model.addAttribute("message", "파일 접근이 거부되었습니다.");
        return "error/access-denied";
    }

    @ExceptionHandler(Exception.class)
    public String handleException(HttpServletRequest request, Exception ex, Model model) {
        log.error("❗ 전역 예외 발생", ex);

        Object status = request.getAttribute("jakarta.servlet.error.status_code");
        int statusCode = (status != null) ? Integer.parseInt(status.toString()) : 500;

        model.addAttribute("status", statusCode);
        model.addAttribute("message", ex.getMessage() != null ? ex.getMessage() : "예상치 못한 오류가 발생했습니다.");
        return "error/access-denied";
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public String handleNotFound(NoHandlerFoundException ex, Model model) {
        model.addAttribute("status", 404);
        model.addAttribute("message", "요청하신 페이지를 찾을 수 없습니다.");
        return "error/access-denied";
    }
	
}
