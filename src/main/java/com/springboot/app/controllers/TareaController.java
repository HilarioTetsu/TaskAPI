package com.springboot.app.controllers;

import java.time.LocalDate;
import java.util.List;

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
import com.springboot.app.models.services.CatalogoService;
import com.springboot.app.models.services.ITareaService;
import com.springboot.app.models.services.ITareaTagsService;
import com.springboot.app.utils.Constants;
import com.springboot.app.utils.CustomUserDetails;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping(Constants.URL_BASE_API_V1 + "/tareas")
@Tag(name = "Tareas", description = "Gestión completa de tareas: creación, actualización, asignación, búsqueda y eliminación.")
public class TareaController {

	private final ITareaService tareaService;
	
	private final ITareaTagsService tareaTagsService;
	
	private final CatalogoService catalogoService;



	public TareaController(ITareaService tareaService, ITareaTagsService tareaTagsService,
			CatalogoService catalogoService) {
		super();
		this.tareaService = tareaService;
		this.tareaTagsService = tareaTagsService;
		this.catalogoService = catalogoService;
	}

	@Operation(summary = "Buscar tareas del usuario con filtros avanzados", description = """
			Retorna una lista paginada de tareas del usuario autenticado,
			utilizando filtros opcionales:

			- Estado(s) de tarea (tareaStatusIds)
			- Prioridad(es) (prioridadIds)
			- Rango de fecha límite (fechaLimiteDesde / fechaLimiteHasta)
			- Búsqueda por descripción o título
			- Ordenamiento personalizado ("campo,dir;")

			Siempre devuelve únicamente tareas ACTIVAS y propiedad del usuario.
			""")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Resultados obtenidos correctamente.", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = TareaDto.class)))) })
	@GetMapping()
	public ResponseEntity<Page<TareaDto>> getAllTareas(
			@Parameter(description = "Número de página (0-based).", example = "0") @RequestParam(defaultValue = "0") Integer pagina,
			@Parameter(description = "Tamaño de página.", example = "5") @RequestParam(defaultValue = "5") Integer tamanio,
			@Parameter(description = "IDs de estado de tarea.") @RequestParam(required = false) List<Short> tareaStatusIds,
			@Parameter(description = "IDs de prioridad de tarea.") @RequestParam(required = false) List<Short> prioridadIds,
			@Parameter(description = "Fecha límite desde (yyyy-MM-dd).") @RequestParam(required = false) LocalDate fechaLimiteDesde,
			@Parameter(description = "Fecha límite hasta (yyyy-MM-dd).") @RequestParam(required = false) LocalDate fechaLimiteHasta,
			@Parameter(description = "Búsqueda por descripción.") @RequestParam(required = false) String busquedaDesc,
			@Parameter(description = "Búsqueda por título.") @RequestParam(required = false) String busquedaTitulo,
			@Parameter(description = "Ordenamiento. Formato: campo,dir; Ej: fecha_limite,desc;", example = "fecha_limite,desc;") @RequestParam(defaultValue = "fecha_limite,desc;") String sorts,
			@AuthenticationPrincipal CustomUserDetails authUser) {

		return ResponseEntity.ok().body(tareaService.getAllActives(pagina, tamanio, tareaStatusIds, prioridadIds,
				fechaLimiteDesde, fechaLimiteHasta, busquedaDesc, busquedaTitulo, sorts, authUser.getUserId()));

	}

	@Operation(summary = "Obtener una tarea por su GUID", description = """
			Devuelve una tarea específica, siempre que pertenezca al usuario autenticado.
			""")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Tarea encontrada.", content = @Content(schema = @Schema(implementation = TareaDto.class))),
			@ApiResponse(responseCode = "404", description = "Tarea no encontrada o sin permisos.", content = @Content) })
	@GetMapping("/{id}")
	public ResponseEntity<TareaDto> getByIdTask(
			@Parameter(description = "GUID de la tarea") @PathVariable String idTask,
			@AuthenticationPrincipal CustomUserDetails authUser) {

		return ResponseEntity.ok().body(tareaService.findByIdGuidAndUserId(idTask, authUser.getUserId()));

	}

	@Operation(summary = "Obtener catálogo de prioridades", description = "Devuelve el catálogo completo de prioridades disponibles para las tareas.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Catálogo devuelto correctamente.", content = @Content(array = @ArraySchema(schema = @Schema(implementation = PrioridadTareaDto.class)))) })
	@GetMapping("/prioridades")
	public ResponseEntity<List<PrioridadTareaDto>> getPrioridadesTarea() {

		return ResponseEntity.ok().body(catalogoService.findAllPrioridadesTarea());

	}

	@Operation(summary = "Obtener catálogo de estatus de tarea", description = "Devuelve el catálogo de estado de tarea (tarea_status).")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Catálogo devuelto correctamente.", content = @Content(array = @ArraySchema(schema = @Schema(implementation = TareaStatusDto.class)))) })
	@GetMapping("/tarea-status")
	public ResponseEntity<List<TareaStatusDto>> getTareaStatus() {

		return ResponseEntity.ok().body(catalogoService.findAllTareaStatus());

	}

	@Operation(summary = "Actualizar una tarea", description = """
			Actualiza información de una tarea existente:
			- Título
			- Descripción
			- Prioridad
			- Estado
			- Fecha límite
			- Proyecto asociado

			El usuario debe ser owner de la tarea.
			""")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Tarea actualizada correctamente.", content = @Content(schema = @Schema(implementation = TareaDto.class))),
			@ApiResponse(responseCode = "403", description = "El usuario no tiene permisos para actualizar esta tarea.", content = @Content) })
	@PatchMapping
	public ResponseEntity<TareaDto> actualizarTarea(@Valid @RequestBody TareaDto tareaDto,
			@AuthenticationPrincipal CustomUserDetails authUser) {

		return ResponseEntity.ok().body(tareaService.save(tareaDto, authUser.getUserId()));

	}

	@Operation(summary = "Asignar un tag a una tarea", description = """
			Asigna una etiqueta (tag) a una tarea.

			Reglas:
			- Si la tarea NO pertenece a un proyecto:
			    Solo el owner de la tarea puede asignar un tag.
			- Si la tarea pertenece a un proyecto:
			    El usuario debe ser owner o miembro con permisos del proyecto.
			- No se puede asignar el mismo tag dos veces.
			""")
	@ApiResponses({ @ApiResponse(responseCode = "204", description = "Tag asignado correctamente.", content = @Content),
			@ApiResponse(responseCode = "403", description = "Sin permisos para asignar el tag.", content = @Content),
			@ApiResponse(responseCode = "404", description = "Tarea o Tag no encontrado.", content = @Content) })
	@PostMapping("/{idTarea}/tags/{idTag}")
	public ResponseEntity<Void> asignarTag(@PathVariable(required = true) String idTarea,
			@PathVariable(required = true) Integer idTag, @AuthenticationPrincipal CustomUserDetails authUser) {

		tareaTagsService.asignarTag(idTarea, idTag, authUser.getUserId());

		return ResponseEntity.noContent().build();

	}

	@Operation(summary = "Eliminar un tag de una tarea", description = "Remueve una etiqueta previamente asignada a una tarea.")
	@ApiResponses({ @ApiResponse(responseCode = "204", description = "Tag removido correctamente.", content = @Content),
			@ApiResponse(responseCode = "403", description = "Sin permisos.", content = @Content),
			@ApiResponse(responseCode = "404", description = "Tarea o Tag no encontrado.", content = @Content) })
	@DeleteMapping("/{idTarea}/tags/{idTag}")
	public ResponseEntity<?> removerTag(@PathVariable(required = true) String idTarea,
			@PathVariable(required = true) Integer idTag, @AuthenticationPrincipal CustomUserDetails authUser) {

		tareaTagsService.quitarTag(idTarea, idTag, authUser.getUserId());

		return ResponseEntity.noContent().build();

	}

	@Operation(summary = "Crear una nueva tarea", description = """
			Crea una tarea nueva (independiente o dentro de un proyecto).

			Reglas:
			- Si `project_id` es null → tarea independiente.
			- Si `project_id` tiene valor → debe existir el proyecto y estar activo.
			- El usuario que crea la tarea se convierte en owner.
			""")
	@ApiResponses({
			@ApiResponse(responseCode = "201", description = "Tarea creada correctamente.", content = @Content(schema = @Schema(implementation = TareaDto.class))),
			@ApiResponse(responseCode = "404", description = "Proyecto no encontrado o inactivo.", content = @Content) })
	@PostMapping
	public ResponseEntity<TareaDto> crearTarea(@Valid @RequestBody TareaDto tareaDto,
			@AuthenticationPrincipal CustomUserDetails authUser) {

		return new ResponseEntity<TareaDto>(tareaService.save(tareaDto, authUser.getUserId()), HttpStatus.CREATED);

	}

	@Operation(summary = "Obtener tareas asignadas al usuario dentro de un proyecto", description = """
			Lista las tareas donde el usuario autenticado está asignado,
			pero únicamente dentro del proyecto indicado.
			""")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Tareas obtenidas correctamente.", content = @Content(array = @ArraySchema(schema = @Schema(implementation = TareaDto.class)))),
			@ApiResponse(responseCode = "403", description = "El usuario no tiene permisos sobre este proyecto.", content = @Content) })
	@GetMapping("/project/{projectId}/assigned")
	public ResponseEntity<List<TareaDto>> getTareasAsignadasByProjectId(@PathVariable String projectId,
			@AuthenticationPrincipal CustomUserDetails authUser) {

		return ResponseEntity.ok().body(tareaService.getTareasAsignadasByProjectId(projectId, authUser.getUserId()));

	}

	@Operation(summary = "Asignar usuarios a una tarea", description = """
			Asigna uno o varios usuarios a una tarea.

			Reglas:
			- Si la tarea NO tiene proyecto:
			    Solo se puede asignar a **un** usuario.
			    Solo el owner puede asignar.

			- Si la tarea SÍ tiene proyecto:
			    Solo OWNER del proyecto puede asignar.
			    Los usuarios deben ser miembros del proyecto.
			    La asignación se acumula (no reemplaza los ya asignados).
			""")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Asignación realizada correctamente.", content = @Content(schema = @Schema(example = "\"Asignacion realizada\""))),
			@ApiResponse(responseCode = "403", description = "Sin permisos.", content = @Content),
			@ApiResponse(responseCode = "404", description = "Tarea o Usuario no encontrado.", content = @Content) })
	@PostMapping("/{tareaId}/assign")
	public ResponseEntity<String> asignarTarea(@RequestBody List<Long> userIds, @PathVariable String tareaId,
			@AuthenticationPrincipal CustomUserDetails authUser) {

		tareaService.asignarTarea(userIds, tareaId, authUser.getUserId());

		return ResponseEntity.ok().body("Asignacion realizada");

	}

	@Operation(summary = "Eliminar una tarea", description = """
			Elimina lógicamente una tarea.

			Reglas:
			- Si la tarea NO pertenece a un proyecto:
			    Solo el owner puede eliminarla.

			- Si la tarea pertenece a un proyecto:
			    Solo usuarios con permisos (OWNER/EDITOR) pueden eliminarla.

			Efectos:
			- Tarea se marca como INACTIVE.
			- Se limpia la lista de usuarios asignados.
			""")
	@ApiResponses({
			@ApiResponse(responseCode = "204", description = "Tarea eliminada correctamente.", content = @Content),
			@ApiResponse(responseCode = "403", description = "Sin permisos.", content = @Content),
			@ApiResponse(responseCode = "404", description = "Tarea no encontrada.", content = @Content) })
	@DeleteMapping("/{tareaId}")
	public ResponseEntity<Void> eliminarTarea(@PathVariable String tareaId,
			@AuthenticationPrincipal CustomUserDetails authUser) {

		tareaService.deleteTarea(tareaId, authUser.getUserId());

		return ResponseEntity.noContent().build();

	}

}
