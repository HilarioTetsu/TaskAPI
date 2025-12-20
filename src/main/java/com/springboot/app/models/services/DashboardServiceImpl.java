package com.springboot.app.models.services;

import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.springboot.app.models.dtos.DashboardDto;
import com.springboot.app.models.entities.Usuario;

@Service
public class DashboardServiceImpl implements IDashboardService {

	private final StatsService statsService;
	
	private final IUsuarioService usuarioService;
	


	public DashboardServiceImpl(StatsService statsService, IUsuarioService usuarioService) {
		super();
		this.statsService = statsService;
		this.usuarioService = usuarioService;
	}


	@Override
	public DashboardDto getDashboardInfo(Long userId) {
	
		Usuario user = usuarioService.findByUserId(userId);
		
		
		Map<String,Integer> mapCountersTareaStatus = statsService.countTareasByTareaStatus(userId).stream()
	            .collect(Collectors.toMap(
	                n -> (String) n[0], 
	                n -> ((Number) n[1]).intValue() 
	            ));

	   
	    Map<String,Integer> mapCountersTareaPrioridad = statsService.countTareasByPrioridad(userId).stream()
	            .collect(Collectors.toMap(
	                n -> (String) n[0], 
	                n -> ((Number) n[1]).intValue()
	            ));
		
	
		DashboardDto dto = DashboardDto.builder()
				.username(user.getUsername())
				.usuarioId(userId)
				.proyectosComoOwner(statsService.getProjectCountRoleOwner(userId))
				.proyectosTotalActivos(statsService.getProjectCountActive(userId))
				.tareasParaHoy(statsService.getTareasHoyCountByUserId(userId))
				.tareasPendientes(statsService.getTareasPendientesCountByUserId(userId))
				.tareasVencidas(statsService.getTareasVencidasCountByUserId(userId))
				.tareasPorEstatus(mapCountersTareaStatus)
				.tareasPorPrioridad(mapCountersTareaPrioridad)
				.build();
		
		return dto;
	}



}
