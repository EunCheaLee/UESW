package com.project.demo001.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.project.demo001.domain.Subway;

public interface SubwayRepository extends JpaRepository<Subway, Long> {
	void deleteByStationName(String stationName);

	// stationName으로 like 검색
	List<Subway> findByStationNameContaining(String keyword);

	// chosung으로 like 검색
	List<Subway> findByChosungContaining(String keyword);

	// 두 가지를 모두 검색 (조금 더 발전형)
	@Query("SELECT s FROM Subway s WHERE s.stationName LIKE %:keyword% OR s.chosung LIKE %:keyword%")
	List<Subway> searchByStationNameOrChosung(@Param("keyword") String keyword);

	@Query("SELECT s.stationName FROM Subway s")
    List<String> findAllStationNames();
}
