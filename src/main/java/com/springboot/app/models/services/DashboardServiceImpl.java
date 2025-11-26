package com.springboot.app.models.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.springboot.app.models.dtos.DashboardDto;
import com.springboot.app.models.entities.Usuario;

@Service
public class DashboardServiceImpl implements IDashboardService {

	private final ITareaService tareaService;
	
	private final IProjectService projectService;
	
	private final IUsuarioService usuarioService;
	
	




	public DashboardServiceImpl(ITareaService tareaService, IProjectService projectService,
			IUsuarioService usuarioService) {
		super();
		this.tareaService = tareaService;
		this.projectService = projectService;
		this.usuarioService = usuarioService;
	}






	@Override
	public DashboardDto getDashboardInfo(Long userId) {
	
		Usuario user = usuarioService.findByUserId(userId);
		
		
		Map<String,Integer> mapCountersTareaStatus = tareaService.countTareasByTareaStatus(userId).stream()
	            .collect(Collectors.toMap(
	                n -> (String) n[0], 
	                n -> ((Number) n[1]).intValue() 
	            ));

	   
	    Map<String,Integer> mapCountersTareaPrioridad = tareaService.countTareasByPrioridad(userId).stream()
	            .collect(Collectors.toMap(
	                n -> (String) n[0], 
	                n -> ((Number) n[1]).intValue()
	            ));
		
	
		DashboardDto dto = DashboardDto.builder()
				.username(user.getUsername())
				.usuarioId(userId)
				.proyectosComoOwner(projectService.getProjectCountRoleOwner(userId))
				.proyectosTotalActivos(projectService.getProjectCountActive(userId))
				.tareasParaHoy(tareaService.getTareasHoyCountByUserId(userId))
				.tareasPendientes(tareaService.getTareasPendientesCountByUserId(userId))
				.tareasVencidas(tareaService.getTareasVencidasCountByUserId(userId))
				.tareasPorEstatus(mapCountersTareaStatus)
				.tareasPorPrioridad(mapCountersTareaPrioridad)
				.build();
		
		return dto;
	}



}
