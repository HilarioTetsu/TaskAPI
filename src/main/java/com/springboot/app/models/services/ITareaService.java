package com.springboot.app.models.services;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;

import com.springboot.app.models.dtos.PrioridadTareaDto;
import com.springboot.app.models.dtos.TagDto;
import com.springboot.app.models.dtos.TareaDto;
import com.springboot.app.models.dtos.TareaStatusDto;
import com.springboot.app.models.entities.Tarea;
import com.springboot.app.models.entities.TareaStatus;


public interface ITareaService {

	TareaDto findByIdGuidAndUserId(String id, Long userId);
	
	Tarea findTareaByIdGuidAndUserId(String id, Long userId);
	
	Page<TareaDto> getAllActives(int pagina,int tamanio, List<Short> tareaStatusIds, List<Short> prioridadIds, LocalDate fechaLimiteDesde, LocalDate fechaLimiteHasta, String busquedaDesc, String busquedaTitulo, String sorts, Long ownerId);
	
	TareaDto save(TareaDto dto, Long userId);
	
	boolean isAsignedToThisTask(String tareaId,Long userId);
	
	List<TagDto> asignarTag(String idTarea,Integer idTag, Long authUserId);

	void asignarTarea(List<Long> userIds, String tareaId, Long userId);

	List<TareaDto> getTareasAsignadasByProjectId(String projectId, Long userId);

	void deleteTarea(String tareaId, Long authUserId);

	Optional<Tarea> findByIdGuid(String tareaId);

	List<PrioridadTareaDto> findAllPrioridadesTarea();

	void quitarTag(String idTarea, Integer idTag, Long userId);
	
	TareaDto save(Tarea tarea);
	
	List<Object[]> countTareasByPrioridad(Long userId);
	
	List<Object[]> countTareasByTareaStatus(Long userId);
	
	int getTareasHoyCountByUserId(Long userId);
	
	int getTareasVencidasCountByUserId(Long userId);
	
	int getTareasPendientesCountByUserId(Long userId);

	List<TareaStatusDto> findAllTareaStatus();
	
	
	
}
