package com.project.demo001.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.demo001.domain.Location;

public interface LocationRepository extends JpaRepository<Location, Long> {

}
