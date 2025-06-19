package com.project.demo001.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.demo001.domain.Seoul;

public interface SeoulRepository extends JpaRepository<Seoul, Long> {
	List<Seoul> findAll();
}
