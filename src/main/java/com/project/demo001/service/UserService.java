package com.project.demo001.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.project.demo001.domain.User;
import com.project.demo001.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

	@Autowired
	private final UserRepository userRepository;
	
	public void registerUser(User user) {
		userRepository.save(user);
	}
	
	public void updateLastConnect(User user) {	// 로그인 성공 시 갱신
		user.setLastConnect(LocalDateTime.now());
		userRepository.save(user);
	}

    // User를 ID로 조회하는 메서드
    public User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자가 존재하지 않습니다. ID: " + userId));
    }
	
}