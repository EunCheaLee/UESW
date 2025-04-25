package com.project.demo001.service;

import java.time.LocalDateTime;
import java.util.Optional;

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
	
	public void updateLastConnect(User user) {	// 로그인 성공 시 갱신
		user.setLastConnect(LocalDateTime.now());
		userRepository.save(user);
	}

    // User를 ID로 조회하는 메서드
    public User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자가 존재하지 않습니다. ID: " + userId));
    }
    
//    회원가입 이메일 중복 체크
    public void registerUser(User user) {
    	validateDuplicateEmail(user.getEmail());
    	userRepository.save(user);
    }
    
    public void validateDuplicateEmail(String email) {
    	if(userRepository.findByEmail(email).isPresent()) {
    		throw new IllegalStateException("이미 존재하는 이메일입니다.");
    	}
    }

    public User login(String account, String password) {
        User user = userRepository.findByAccount(account);
        if (user != null && user.getPassword().equals(password)) {
            return user;
        }
        return null;
    }
	
	public boolean isValidUser(String account, String password) {
	    User user = userRepository.findByAccount(account); // 👈 수정
	    return user != null && user.getPassword().equals(password);
	}

	public User getUserByAccount(String account) {
	    return userRepository.findByAccount(account); // account가 unique 키라고 가정
	}
    
	
}