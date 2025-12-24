package com.springboot.app.models.services;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;

import com.springboot.app.models.dtos.TareaDto;
import com.springboot.app.models.entities.Tag;
import com.springboot.app.models.entities.Tarea;


public interface ITareaService {

	TareaDto findByIdGuidAndUserId(String id, Long userId);
	
	Tarea findTareaByIdGuidAndUserId(String id, Long userId);
	
	Page<TareaDto> getAllActives(int pagina,int tamanio, List<Short> tareaStatusIds, List<Short> prioridadIds, LocalDate fechaLimiteDesde, LocalDate fechaLimiteHasta, String busquedaDesc, String busquedaTitulo, String sorts, Long ownerId);
	
	TareaDto save(TareaDto dto, Long userId);
	
	boolean isAsignedToThisTask(String tareaId,Long userId);
		

	void asignarTarea(List<Long> userIds, String tareaId, Long userId);

	List<TareaDto> getTareasAsignadasByProjectId(String projectId, Long userId);

	void deleteTarea(String tareaId, Long authUserId);

	Optional<Tarea> findByIdGuid(String tareaId);
	
	
	List<Tag> getTagsFromTarea(String idGuid);
	
	
	
}
