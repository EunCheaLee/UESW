package com.project.demo001.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.project.demo001.domain.Bus;
import com.project.demo001.repository.BusRepository;

@Service
public class BusService {

	private final BusRepository busRepository;
	
	public BusService(BusRepository busRepository) {
        this.busRepository = busRepository;
    }

	public List<Map<String, Object>> getAllStations() {
	    List<Bus> busList = busRepository.findAll();

	    return busList.stream()
	    	.filter(Objects::nonNull) // null 객체 방지
	        .filter(bus -> bus.getStNm() != null && bus.getLat() != null && bus.getLot() != null) // 중요 필드 체크
	        .map(bus -> {
	        Map<String, Object> map = new HashMap<>();
	        map.put("busRouteId", bus.getBusRouteId());  // 고유 식별자 추가
	        map.put("name", bus.getStNm());
	        map.put("lat", bus.getLat());
	        map.put("lon", bus.getLot());
	        map.put("arsId", bus.getArsId());
	        return map;
	    })
	        .collect(Collectors.toList());
	}
	
}
