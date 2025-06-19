package com.project.demo001.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Bus {
	@Id
	private String busRouteId;
	private String arsId;
	private String stNm;
	private Double lot;
	private Double lat;
	
}
