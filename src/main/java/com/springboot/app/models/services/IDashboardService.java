package com.springboot.app.models.services;



import com.springboot.app.models.dtos.DashboardDto;

public interface IDashboardService {

	DashboardDto getDashboardInfo(Long userId);
	
}
