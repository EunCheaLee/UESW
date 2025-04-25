package com.project.demo001.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.demo001.domain.User;

public interface UserRepository extends JpaRepository<User, Long> {

	User findAllById(Long id);
	User findByAccount(String account);
	User findByUserName(String userName);
	Optional<User> findByEmail(String email);
	User findByAccountAndPassword(String account, String password);

}