package com.springboot.app.utils;

import org.springframework.stereotype.Component;

import com.springboot.app.models.dtos.TareaDto;
import com.springboot.app.models.entities.PrioridadTarea;
import com.springboot.app.models.entities.Project;
import com.springboot.app.models.entities.Tarea;
import com.springboot.app.models.entities.TareaStatus;
import com.springboot.app.models.entities.Usuario;
import com.springboot.app.models.services.IProjectService;

@Component
public class TareaChangeLogHelper {

	private final IProjectService projectService;
	
	
	
	public TareaChangeLogHelper(IProjectService projectService) {		
		this.projectService = projectService;
	}



	public String generarMensajeCambiosTarea(Tarea tarea, TareaDto dto, Usuario user, PrioridadTarea prioridad, TareaStatus tareaStatus, Project project) {
		
		StringBuffer buffer = new StringBuffer();
		
		String msg="%s ha cambiado a \"%s\" por el usuario %s \n\n";
		
		if (!tarea.getTitulo().equals(dto.getTitulo())) {
			buffer.append(String.format(msg, "Titulo",dto.getTitulo(),user.getUsername()));
		}
		
		
		if (!tarea.getDescripcion().equals(dto.getDescripcion())) {
			buffer.append(String.format(msg, "Descripcion",dto.getDescripcion(),user.getUsername()));
		}
		
		if (!tarea.getFechaLimite().equals(dto.getFechaLimite())) {
			buffer.append(String.format(msg, "Fecha limite",dto.getFechaLimite().toString(),user.getUsername()));
		}
		
		if (!tarea.getStatus().equals(dto.getStatus())) {
			buffer.append(String.format(msg, "Status",projectService.getStatusByKey(dto.getStatus()),user.getUsername()));
		}
		
		if (!tarea.getPrioridad().getId().equals(dto.getId_prioridad())) {
			buffer.append(String.format(msg, "Prioridad",prioridad.getPrioridadTipo(),user.getUsername()));
		}
		
		if (!tarea.getTareaStatus().getId().equals(dto.getId_tarea_status())) {
			buffer.append(String.format(msg, "Status de la tarea",tareaStatus.getStatus(),user.getUsername()));
		}
		
		if (project!=null && !tarea.getProject().getIdGuid().equals(dto.getProject_id())) {
			buffer.append(String.format(msg, "El proyecto de la tarea",project.getName(),user.getUsername()));
		}
		
		
		return buffer.toString();
	}

	
}
