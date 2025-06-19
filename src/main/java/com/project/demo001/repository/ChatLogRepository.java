package com.project.demo001.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.demo001.domain.ChatMessage;

public interface ChatLogRepository extends JpaRepository<ChatMessage, Long> {
	List<ChatMessage> findTop30ByOrderByTimestampAsc(); // 최근 30개
}
