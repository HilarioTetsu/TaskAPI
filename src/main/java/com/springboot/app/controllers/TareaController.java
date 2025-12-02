package com.springboot.app.controllers;

import java.time.LocalDate;

import java.util.List;
import com.springboot.app.utils.Constants;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.springboot.app.models.dtos.PrioridadTareaDto;
import com.springboot.app.models.dtos.TareaDto;
import com.springboot.app.models.dtos.TareaStatusDto;
import com.springboot.app.models.services.ITareaService;
import com.springboot.app.utils.CustomUserDetails;

import jakarta.validation.Valid;

@RestController
@RequestMapping(Constants.URL_BASE_API_V1+"/tareas")
public class TareaController {

	private final ITareaService tareaService;

	
	public TareaController(ITareaService tareaService) {
		this.tareaService = tareaService;	
	}

	@GetMapping()
	public ResponseEntity<Page<TareaDto>> getAllTareas(
			@RequestParam(required = false, defaultValue = "0") Integer pagina,
			@RequestParam(required = false, defaultValue = "5") Integer tamanio,
			@RequestParam(required = false) List<Short> tareaStatusIds,
			@RequestParam(required = false) List<Short> prioridadIds,
			@RequestParam(required = false) LocalDate fechaLimiteDesde,
			@RequestParam(required = false) LocalDate fechaLimiteHasta,
			@RequestParam(required = false) String busquedaDesc, @RequestParam(required = false) String busquedaTitulo,
			@RequestParam(required = false, defaultValue = "fecha_limite,desc;") String sorts,
			@AuthenticationPrincipal CustomUserDetails authUser) {

		return ResponseEntity.ok().body(tareaService.getAllActives(pagina, tamanio, tareaStatusIds, prioridadIds,
				fechaLimiteDesde, fechaLimiteHasta, busquedaDesc, busquedaTitulo, sorts, authUser.getUserId()));

	}

	@GetMapping("/{id}")
	public ResponseEntity<TareaDto> getByIdTask(@PathVariable(name = "id", required = true) String idTask,
			@AuthenticationPrincipal CustomUserDetails authUser) {

		return ResponseEntity.ok().body(tareaService.findByIdGuidAndUserId(idTask, authUser.getUserId()));

	}
	
	@GetMapping("/prioridades")
	public ResponseEntity<List<PrioridadTareaDto>> getPrioridadesTarea() {

		return ResponseEntity.ok().body(tareaService.findAllPrioridadesTarea());

	}
	
	@GetMapping("/tarea-status")
	public ResponseEntity<List<TareaStatusDto>> getTareaStatus() {

		return ResponseEntity.ok().body(tareaService.findAllTareaStatus());

	}

	@PatchMapping
	public ResponseEntity<TareaDto> actualizarTarea(@Valid @RequestBody TareaDto tareaDto,
			@AuthenticationPrincipal CustomUserDetails authUser) {


		return ResponseEntity.ok().body(tareaService.save(tareaDto, authUser.getUserId()));

	}

	@PostMapping("/{idTarea}/tags/{idTag}")
	public ResponseEntity<Void> asignarTag(@PathVariable(required = true) String idTarea,
			@PathVariable(required = true) Integer idTag,
			@AuthenticationPrincipal CustomUserDetails authUser) {

		
		tareaService.asignarTag(idTarea, idTag, authUser.getUserId());
		
		return ResponseEntity.noContent().build();		

	}
	
	@DeleteMapping("/{idTarea}/tags/{idTag}")
	public ResponseEntity<?> removerTag(@PathVariable(required = true) String idTarea,
			@PathVariable(required = true) Integer idTag,
			@AuthenticationPrincipal CustomUserDetails authUser) {

		
		tareaService.quitarTag(idTarea, idTag, authUser.getUserId());
		
		return ResponseEntity.noContent().build();	

	}


	@PostMapping
	public ResponseEntity<TareaDto> crearTarea(@Valid @RequestBody TareaDto tareaDto,
			@AuthenticationPrincipal CustomUserDetails authUser) {

		return new ResponseEntity<TareaDto>(tareaService.save(tareaDto, authUser.getUserId()), HttpStatus.CREATED);

	}

	@GetMapping("/project/{projectId}/assigned")
	public ResponseEntity<List<TareaDto>> getTareasAsignadasByProjectId(@PathVariable String projectId,
			@AuthenticationPrincipal CustomUserDetails authUser) {

		return ResponseEntity.ok().body(tareaService.getTareasAsignadasByProjectId(projectId, authUser.getUserId()));

	}

	@PostMapping("/{tareaId}/assign")
	public ResponseEntity<String> asignarTarea(@RequestBody List<Long> userIds, @PathVariable String tareaId,
			@AuthenticationPrincipal CustomUserDetails authUser) {

		tareaService.asignarTarea(userIds, tareaId, authUser.getUserId());

		return ResponseEntity.ok().body("Asignacion realizada");

	}

	@DeleteMapping("/{tareaId}")
	public ResponseEntity<Void> eliminarTarea(@PathVariable String tareaId,
			@AuthenticationPrincipal CustomUserDetails authUser) {

		tareaService.deleteTarea(tareaId, authUser.getUserId());

		return ResponseEntity.noContent().build();

	}

}
