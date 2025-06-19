package com.project.demo001.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.project.demo001.domain.User;
import com.project.demo001.repository.UserRepository;

@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

	private final UserRepository userRepository;
	
	public CustomOAuth2UserService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}
	
	@Override
	public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
		
		OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
	    OAuth2User oAuth2User;
	    
	    try {
	        oAuth2User = delegate.loadUser(userRequest); // 여기서 오류 발생 가능성 가장 높음
	    } catch (Exception e) {
	        e.printStackTrace(); // 콘솔에 전체 예외 출력
	        throw new OAuth2AuthenticationException(new OAuth2Error("oauth2_load_failed"), "OAuth 사용자 정보 로딩 실패: " + e.getMessage());
	    }
		
		Map<String, Object> attributes = oAuth2User.getAttributes();
		
		String registrationId = userRequest.getClientRegistration().getRegistrationId(); // google, kakao, naver
		String provider = userRequest.getClientRegistration().getRegistrationId();

	    System.out.println("===== provider = " + provider + " =====");
	    System.out.println("===== attributes = " + attributes + " =====");
		
		String providerId = null;
		String email = null;
		String name = null;
		
	    
		if ("google".equals(provider)) {
			providerId = (String) attributes.get("sub");
			email = (String) attributes.get("email");
			name = (String) attributes.get("name");
		} else if ("kakao".equalsIgnoreCase(provider) || "kakao-login".equals(provider)) {
		    providerId = String.valueOf(attributes.get("id"));

		    Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
		    if (kakaoAccount != null) {
		        email = (String) kakaoAccount.get("email"); // ✅ 수정됨

		        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
		        if (profile != null) {
		            name = (String) profile.get("nickname"); // ✅ 수정됨
		        }

		        if (email == null || email.isBlank()) {
		            throw new OAuth2AuthenticationException(
		                new OAuth2Error("missing_email"),
		                "카카오 이메일을 가져올 수 없습니다. 이메일 항목 동의를 활성화하세요."
		            );
		        }

		        System.out.println("✅ [카카오 이메일 추출 결과] email = " + email);
		        System.out.println("attributes keys = " + attributes.keySet());
		        System.out.println("attributes[email] = " + attributes.get("email"));
		    }
		} else if ("naver".equals(provider)) {
			Map<String, Object> response = (Map<String, Object>) attributes.get("response");
			if (response != null) {
				providerId = (String) response.get("id");
				email = (String) response.get("email");
				name = (String) response.get("name");
			}
		} else {
			throw new OAuth2AuthenticationException(new OAuth2Error("unsupported_provider"), "지원하지 않는 소셜 로그인입니다.");
		}

		if (email == null || email.isBlank()) {
			throw new OAuth2AuthenticationException(new OAuth2Error("missing_email"), "이메일 정보를 가져올 수 없습니다.");
		}
		if (name == null || name.isBlank()) {
			name = "Unknown User";
		}

		String tokenId = userRequest.getAccessToken().getTokenValue();

		User user = userRepository.findByProviderAndProviderId(provider, providerId);

		if (user == null) {
		    user = saveNewUser(email, name, provider, providerId, tokenId);
		    if (user == null) {
		        throw new OAuth2AuthenticationException(
		            new OAuth2Error("user_save_failed"),
		            "사용자 저장에 실패했습니다."
		        );
		    }
		}
		// 이미 존재하는 사용자일 경우 토큰을 최신화할 수도 있음
		user.setAccount(tokenId);
		userRepository.save(user);

		List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));


		Map<String, Object> enrichedAttributes = new HashMap<>(attributes);
		enrichedAttributes.put("email", email); // 수동으로 삽입
		
		return new DefaultOAuth2User(authorities, enrichedAttributes, "email");
	}

	private User saveNewUser(String email, String name, String provider, String providerId, String tokenId) {
	    User user = new User();
	    user.setEmail(email);
	    user.setUserName(name);
	    user.setProvider(provider);
	    user.setProviderId(providerId);
	    user.setAccount(tokenId);
	    user.setPassword(null);
	    
	    return userRepository.save(user); // 예외 발생하면 여기서 멈춤
	}
	
}
