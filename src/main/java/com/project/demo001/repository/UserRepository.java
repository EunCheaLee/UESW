package com.project.demo001.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.demo001.domain.User;

public interface UserRepository extends JpaRepository<User, Long> {

	User findAllById(Long id);
	User findByUserName(String userName);
	User findByAccount(String account);	// <-- 로그인용
	boolean existsByEmail(String email);		 	// <-- 중복체크 빠르게
	Optional<User> findByEmail(String email);		// <-- 이메일 중복체크용
	User findByProviderAndProviderId(String provider, String providerId);

}