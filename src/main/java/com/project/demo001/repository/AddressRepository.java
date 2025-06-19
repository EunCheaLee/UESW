package com.project.demo001.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.demo001.domain.Address;

public interface AddressRepository extends JpaRepository<Address, Long> {

	Optional<Address> findByStreetName(String streetName);
	
}
