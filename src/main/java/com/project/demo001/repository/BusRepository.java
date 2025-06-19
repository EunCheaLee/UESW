package com.project.demo001.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.demo001.domain.Bus;

public interface BusRepository extends JpaRepository<Bus, String> {
    List<Bus> findByArsId(String arsId); // 정류소 ARS ID로 검색
}
