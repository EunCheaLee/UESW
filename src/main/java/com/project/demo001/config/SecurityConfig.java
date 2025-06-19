package com.project.demo001.config;

import com.project.demo001.domain.User;
import com.project.demo001.repository.UserRepository;
import com.project.demo001.service.CustomOAuth2UserService;
import com.project.demo001.service.UserService;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

	private final UserService userService; // ✅ UserService를 주입
    private final CustomOAuth2UserService customOAuth2UserService;
    private final PasswordEncoder passwordEncoder;

    public SecurityConfig(UserService userService,
            CustomOAuth2UserService customOAuth2UserService,
            PasswordEncoder passwordEncoder) {
			this.userService = userService;
			this.customOAuth2UserService = customOAuth2UserService;
			this.passwordEncoder = passwordEncoder; // ✅ 이걸 사용하면 됨
			}

	@Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
	        .cors()
	        .and()
        	.csrf(csrf -> csrf.disable()) // ✅ CSRF 끄기 (테스트 단계에서는 필요)
            .headers(headers -> headers 
            	.frameOptions().disable() // X-Frame-Options 해제
	            .contentSecurityPolicy(csp -> 
	            	csp.policyDirectives(
	            			"default-src 'self';" +
        					"media-src 'self' blob: data:; " +
        					"style-src 'self' 'unsafe-inline' https://uicdn.toast.com https://cdn.jsdelivr.net https://unpkg.com https://cdnjs.cloudflare.com; " +
        		            "script-src 'self' 'unsafe-inline' 'unsafe-eval' https://uicdn.toast.com https://www.utic.go.kr/* https://cdn.jsdelivr.net https://cdn.jsdelivr.net/npm/vue@2/dist/vue.js https://unpkg.com https://dapi.kakao.com http://t1.daumcdn.net https://t1.daumcdn.net http://ws.bus.go.kr; " +
        		            "frame-src * 'self' https://www.utic.go.kr/* https://*.kakao.com https://*.daumcdn.net https://t1.daumcdn.net https://postcode.map.daum.net http://postcode.map.daum.net; " + // iframe 혹은 카카오 지도용
        		            "font-src 'self' https://fastly.jsdelivr.net https://cdn.jsdelivr.net https://cdnjs.cloudflare.com; " +
        		            "img-src * data: blob:; " +
        		            "connect-src *; " +
        		            "worker-src 'self' blob:;"+
	                        "object-src 'none';"
	                        ) // 🔥 추가!
	            		)
	                )
            .exceptionHandling(exception -> 
	            exception.accessDeniedPage("/access-denied")
	        )
            .authorizeHttpRequests(auth -> auth
        		.requestMatchers(
        		        "/api/register",
        		        "/api/check-email",     // ← 이거 꼭 추가하세요!
        		        "/login.html", 
        		        "/css/**", 
        		        "/js/**",
        		        "/img/**",
        		        "/oauth2/**"
        		    ).permitAll()
        			.requestMatchers("/chatting").authenticated() // 🔐 로그인 필요 경로
        		    .anyRequest().permitAll()	// ✅ 항상 마지막
        		)
            .formLogin(form -> form			// 일반 로그인 설정 (ID/PW)
            	    .loginPage("/login")
            	    .loginProcessingUrl("/login")
            	    .usernameParameter("account")
            	    .passwordParameter("password")
            	    .successHandler((request, response, authentication) -> {
            	        org.springframework.security.core.userdetails.User principal =
            	            (org.springframework.security.core.userdetails.User) authentication.getPrincipal();
            	        User user = userService.getUserByAccount(principal.getUsername()); // ← null일 수도 있음!
            	        request.getSession().setAttribute("loggedInUser", user);
            	        response.sendRedirect("/home");
            	    })
            	    .failureUrl("/login.html?error=true") // 🔥 실패 시 쿼리 파라미터
            	    .permitAll()
            )
            .userDetailsService(userService) // 사용자 조회 서비스 등록
            .oauth2Login(oauth2 -> oauth2	// 소셜 로그인 설정 (Google, Kakao 등)
                .loginPage("/login")
                .defaultSuccessUrl("/home", true)
                .failureUrl("/login?error=true") // ✅ 실패 시 로그인 페이지로 이동 + 에러 표시
                .userInfoEndpoint(userInfo -> userInfo
                		.userService(customOAuth2UserService)
                )
                .successHandler((request, response, authentication) -> {
                    // OAuth2User에서 이메일(고유키) 추출
                    OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
                    String email = (String) oAuth2User.getAttribute("email");

                    // DB에서 사용자 조회
                    User user = userService.getUserByEmail(email); // ✅ 직접 구현한 서비스
                    request.getSession().setAttribute("loggedInUser", user); // ✅ 세션에 저장

                    response.sendRedirect("/home");
                })
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/login.html")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .sessionManagement(session -> session
        	    .maximumSessions(1)
        	    .maxSessionsPreventsLogin(false)
        	);

        return http.build();
    }
	
	@Bean
	public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
	    return http.getSharedObject(AuthenticationManagerBuilder.class)
	            .userDetailsService(userService)
	            .passwordEncoder(passwordEncoder) // ✅ 여기!
	            .and()
	            .build();
	}

}
