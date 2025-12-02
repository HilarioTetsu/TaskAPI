package com.springboot.app.controllers;

import java.util.List;

import java.util.NoSuchElementException;

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
import org.springframework.web.bind.annotation.RestController;

import com.springboot.app.models.dtos.DashboardDto;
import com.springboot.app.models.dtos.ProjectDto;
import com.springboot.app.models.dtos.ProjectMemberDto;
import com.springboot.app.models.dtos.TareaDto;
import com.springboot.app.models.services.IDashboardService;
import com.springboot.app.models.services.IProjectMemberService;
import com.springboot.app.models.services.IProjectService;
import com.springboot.app.models.services.ITareaService;
import com.springboot.app.utils.Constants;
import com.springboot.app.utils.CustomUserDetails;
import com.springboot.app.utils.ProjectRole;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Proyectos", description = "Gestión de proyectos, miembros de proyecto y tareas asociadas.")
@RequestMapping(Constants.URL_BASE_API_V1 + "/projects")
@RestController
public class ProjectController {

	private final IProjectService projectService;

	private final ITareaService tareaService;

	private final IProjectMemberService projectMemberService;

	private final IDashboardService dashboardService;

	public ProjectController(IProjectService projectService, ITareaService tareaService,
			IProjectMemberService projectMemberService, IDashboardService dashboardService) {
		super();
		this.projectService = projectService;
		this.tareaService = tareaService;
		this.projectMemberService = projectMemberService;
		this.dashboardService = dashboardService;
	}

	@Operation(summary = "Obtener roles disponibles para proyectos", description = "Devuelve la lista de roles posibles dentro de un proyecto (OWNER, EDITOR, VIEWER).")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Lista de roles devuelta correctamente.", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ProjectRole.class)))) })
	@GetMapping("/roles")
	public ResponseEntity<ProjectRole[]> getProjectRoles() {

		return new ResponseEntity<ProjectRole[]>(ProjectRole.values(), HttpStatus.OK);

	}

	@Operation(summary = "Obtener un proyecto por su ID", description = """
			Obtiene la información de un proyecto identificado por su GUID,
			siempre y cuando el usuario autenticado sea miembro (normalmente OWNER).
			""")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Proyecto encontrado.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProjectDto.class))),
			@ApiResponse(responseCode = "404", description = "Proyecto no encontrado o el usuario no tiene acceso.", content = @Content) })
	@GetMapping("/{id}")
	public ResponseEntity<ProjectDto> getByProjectId(@AuthenticationPrincipal CustomUserDetails authUser,
			@Parameter(description = "GUID del proyecto", example = "b3b6a1c5-9d8e-4f2c-9013-0b40e9f5f111") @PathVariable String id) {

		return ResponseEntity.ok().body((projectService.findByProjectIdAndUserId(id, authUser.getUserId())));

	}

	@Operation(summary = "Obtener resumen del dashboard", description = """
			Devuelve información consolidada para el dashboard del usuario:
			cantidad de proyectos, tareas pendientes, vencidas, para hoy, agrupadas por prioridad y estatus.
			""")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Resumen obtenido correctamente.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = DashboardDto.class))) })
	@GetMapping("/summary")
	public ResponseEntity<DashboardDto> getDashboardInfo(@AuthenticationPrincipal CustomUserDetails authUser) {

		return ResponseEntity.ok().body(dashboardService.getDashboardInfo(authUser.getUserId()));

	}

	@Operation(summary = "Listar proyectos del usuario", description = "Lista todos los proyectos activos donde el usuario autenticado es OWNER.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Lista de proyectos devuelta correctamente.", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ProjectDto.class)))) })
	@GetMapping
	public ResponseEntity<List<ProjectDto>> getAllProjects(@AuthenticationPrincipal CustomUserDetails authUser) {

		return ResponseEntity.ok().body(projectService.findByOwnerId(authUser.getUserId()));

	}

	@Operation(summary = "Crear o actualizar un proyecto", description = """
			Crea un nuevo proyecto o actualiza uno existente.

			- Si `idGuid` viene vacío o null, se crea un proyecto nuevo y se asigna al usuario como OWNER.
			- Si `idGuid` ya existe, se actualizan sus datos (nombre, descripción, etc.),
			  siempre que el usuario tenga permisos (OWNER).
			""")
	@ApiResponses({
			@ApiResponse(responseCode = "201", description = "Proyecto creado.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProjectDto.class))),
			@ApiResponse(responseCode = "200", description = "Proyecto actualizado.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProjectDto.class))),
			@ApiResponse(responseCode = "400", description = "Datos inválidos en el cuerpo de la petición.", content = @Content),
			@ApiResponse(responseCode = "403", description = "El usuario no tiene permisos para modificar el proyecto.", content = @Content) })
	@PostMapping
	public ResponseEntity<ProjectDto> saveProject(@RequestBody @Valid ProjectDto dto,
			@AuthenticationPrincipal CustomUserDetails authUser) {

		return new ResponseEntity<ProjectDto>(projectService.save(dto, authUser.getUserId()), HttpStatus.CREATED);

	}

	@Operation(summary = "Agregar un miembro al proyecto", description = """
			Agrega un usuario como miembro de un proyecto con un rol específico.

			Solo un OWNER del proyecto puede agregar miembros.
			""")
	@ApiResponses({
			@ApiResponse(responseCode = "201", description = "Miembro agregado al proyecto.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProjectMemberDto.class))),
			@ApiResponse(responseCode = "400", description = "Datos inválidos o usuario/proyecto inexistente.", content = @Content),
			@ApiResponse(responseCode = "403", description = "El usuario autenticado no es OWNER del proyecto.", content = @Content) })
	@PostMapping("/{id}/members")
	public ResponseEntity<ProjectMemberDto> addProjectMember(@RequestBody @Valid ProjectMemberDto dto,
			@AuthenticationPrincipal CustomUserDetails authUser,
			@Parameter(description = "GUID del proyecto", example = "b3b6a1c5-9d8e-4f2c-9013-0b40e9f5f111") @PathVariable("id") String projectId) {

		return new ResponseEntity<ProjectMemberDto>(projectMemberService.save(projectId, dto, authUser),
				HttpStatus.CREATED);

	}

	@Operation(summary = "Crear una tarea dentro de un proyecto", description = """
			Crea una nueva tarea asociada a un proyecto específico.
			El proyecto debe existir y estar activo; en caso contrario se lanza una excepción.
			""")
	@ApiResponses({
			@ApiResponse(responseCode = "201", description = "Tarea creada dentro del proyecto.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = TareaDto.class))),
			@ApiResponse(responseCode = "404", description = "Proyecto no encontrado o inactivo.", content = @Content) })
	@PostMapping("/{id}/task")
	public ResponseEntity<TareaDto> saveTaskInProject(@RequestBody @Valid TareaDto dto,
			@AuthenticationPrincipal CustomUserDetails authUser,
			@Parameter(description = "GUID del proyecto", example = "b3b6a1c5-9d8e-4f2c-9013-0b40e9f5f111") @PathVariable(name = "id") String projectId) {

		if (!projectService.existsProjectActive(projectId)) {
			throw new NoSuchElementException("Elementos no encontrados");
		}

		dto.setProject_id(projectId);

		return new ResponseEntity<TareaDto>(tareaService.save(dto, authUser.getUserId()), HttpStatus.CREATED);

	}

	@Operation(summary = "Actualizar proyecto", description = """
			Actualiza la información de un proyecto existente.
			Es similar a POST, pero se recomienda para updates explícitos.
			""")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Proyecto actualizado correctamente.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProjectDto.class))),
			@ApiResponse(responseCode = "403", description = "El usuario no tiene permisos para modificar el proyecto.", content = @Content) })
	@PatchMapping
	public ResponseEntity<ProjectDto> updateProject(@RequestBody @Valid ProjectDto dto,
			@AuthenticationPrincipal CustomUserDetails authUser) {

		return ResponseEntity.ok().body(projectService.save(dto, authUser.getUserId()));

	}

	@Operation(summary = "Actualizar miembro de proyecto", description = """
			Actualiza el rol o el estado de un miembro de proyecto.

			- Solo un OWNER del proyecto puede modificar miembros.
			- No se permite dejar al proyecto sin ningún OWNER.
			""")
	@ApiResponses({
			@ApiResponse(responseCode = "201", description = "Miembro de proyecto actualizado.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProjectMemberDto.class))),
			@ApiResponse(responseCode = "400", description = "Datos inválidos o reglas de negocio violadas (por ejemplo, último OWNER).", content = @Content),
			@ApiResponse(responseCode = "403", description = "El usuario autenticado no tiene permisos para actualizar miembros.", content = @Content) })
	@PatchMapping("/{projectId}/members/{userId}")
	public ResponseEntity<ProjectMemberDto> updateProjectMember(@RequestBody @Valid ProjectMemberDto dto,
			@AuthenticationPrincipal CustomUserDetails authUser,
			@Parameter(description = "GUID del proyecto") @PathVariable String projectId,
			@Parameter(description = "ID del usuario miembro") @PathVariable Long userId) {

		projectMemberService.validationOwnerAndMemberProject(dto, userId, authUser.getUserId(), projectId);

		dto.setId(projectMemberService.findByUsuarioIdAndProjectIdGuid(userId, projectId).getId());
		dto.setUsuarioId(userId);
		dto.setProjectId(projectId);

		return new ResponseEntity<ProjectMemberDto>(projectMemberService.save(projectId, dto, authUser),
				HttpStatus.CREATED);

	}

	@Operation(summary = "Eliminar miembro de proyecto", description = """
			Elimina (inactiva) un miembro de un proyecto.

			- Solo un OWNER puede eliminar miembros.
			- No se permite eliminar al último OWNER del proyecto.
			""")
	@ApiResponses({
			@ApiResponse(responseCode = "204", description = "Miembro eliminado correctamente.", content = @Content),
			@ApiResponse(responseCode = "400", description = "Regla de negocio violada (ej. intento de eliminar al último OWNER).", content = @Content),
			@ApiResponse(responseCode = "403", description = "El usuario autenticado no tiene permisos para eliminar miembros.", content = @Content) })
	@DeleteMapping("/{projectId}/members/{userId}")
	public ResponseEntity<Void> deleteProjectMember(@AuthenticationPrincipal CustomUserDetails authUser,
			@Parameter(description = "GUID del proyecto") @PathVariable String projectId,
			@Parameter(description = "ID del usuario miembro") @PathVariable Long userId) {

		projectMemberService.deleteProjectMember(authUser.getUserId(), projectId, userId);

		return ResponseEntity.noContent().build();
	}

	@Operation(summary = "Listar miembros de un proyecto", description = """
			Lista los miembros de un proyecto, incluyendo su rol y estado.
			El usuario debe ser miembro del proyecto para ver esta información.
			""")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Lista de miembros devuelta correctamente.", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ProjectMemberDto.class)))),
			@ApiResponse(responseCode = "403", description = "El usuario no es miembro del proyecto.", content = @Content) })
	@GetMapping("/{projectId}/members")
	public ResponseEntity<List<ProjectMemberDto>> getProjectMembers(@AuthenticationPrincipal CustomUserDetails authUser,
			@Parameter(description = "GUID del proyecto") @PathVariable String projectId) {

		return ResponseEntity.ok(projectMemberService.findProjectMembersByProjectId(authUser.getUserId(), projectId));

	}

	@Operation(summary = "Listar tareas de un proyecto", description = """
			Devuelve las tareas asociadas a un proyecto que sean visibles para el usuario autenticado.
			""")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Lista de tareas devuelta correctamente.", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = TareaDto.class)))),
			@ApiResponse(responseCode = "403", description = "El usuario no tiene permisos para ver las tareas del proyecto.", content = @Content) })
	@GetMapping("/{id}/tasks")
	public ResponseEntity<?> getTasksByProjectId(@Parameter(description = "GUID del proyecto") @PathVariable String id,
			@AuthenticationPrincipal CustomUserDetails authUser) {

		return ResponseEntity.ok(projectService.findTasksByProjectId(id, authUser.getUserId()));

	}

	@Operation(summary = "Eliminar proyecto", description = """
			Elimina lógicamente un proyecto y sus relaciones:
			- Tareas del proyecto se marcan como inactivas.
			- Miembros del proyecto se marcan como inactivos.
			Solo un OWNER puede realizar esta operación.
			""")
	@ApiResponses({
			@ApiResponse(responseCode = "204", description = "Proyecto eliminado correctamente.", content = @Content),
			@ApiResponse(responseCode = "403", description = "El usuario no tiene permisos para eliminar el proyecto.", content = @Content),
			@ApiResponse(responseCode = "404", description = "Proyecto no encontrado.", content = @Content) })
	@DeleteMapping("/{projectId}")
	public ResponseEntity<Void> eliminarProjecto(
			@Parameter(description = "GUID del proyecto") @PathVariable String projectId,
			@AuthenticationPrincipal CustomUserDetails authUser) {

		projectService.deleteProject(projectId, authUser.getUserId());

		return ResponseEntity.noContent().build();

	}

}
