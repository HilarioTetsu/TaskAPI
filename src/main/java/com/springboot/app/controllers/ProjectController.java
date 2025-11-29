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

import jakarta.validation.Valid;

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

	@GetMapping("/roles")
	public ResponseEntity<ProjectRole[]> getProjectRoles() {

		return new ResponseEntity<ProjectRole[]>(ProjectRole.values(), HttpStatus.OK);

	}

	@GetMapping("/{id}")
	public ResponseEntity<ProjectDto> getByProjectId(@AuthenticationPrincipal CustomUserDetails authUser,
			@PathVariable String id) {

		return ResponseEntity.ok().body((projectService.findByProjectIdAndUserId(id, authUser.getUserId())));

	}
	
	@GetMapping("/summary")
	public ResponseEntity<DashboardDto> getDashboardInfo(@AuthenticationPrincipal CustomUserDetails authUser) {

		return ResponseEntity.ok().body(dashboardService.getDashboardInfo(authUser.getUserId()));

	}

	@GetMapping
	public ResponseEntity<List<ProjectDto>> getAllProjects(@AuthenticationPrincipal CustomUserDetails authUser) {

		return ResponseEntity.ok().body(projectService.findByOwnerId(authUser.getUserId()));

	}

	@PostMapping
	public ResponseEntity<ProjectDto> saveProject(@RequestBody @Valid ProjectDto dto,
			@AuthenticationPrincipal CustomUserDetails authUser) {

		return new ResponseEntity<ProjectDto>(projectService.save(dto, authUser.getUserId()), HttpStatus.CREATED);

	}

	@PostMapping("/{id}/members")
	public ResponseEntity<ProjectMemberDto> addProjectMember(@RequestBody @Valid ProjectMemberDto dto,
			@AuthenticationPrincipal CustomUserDetails authUser, @PathVariable("id") String projectId) {

		return new ResponseEntity<ProjectMemberDto>(projectMemberService.save(projectId, dto, authUser),
				HttpStatus.CREATED);

	}

	@PostMapping("/{id}/task")
	public ResponseEntity<TareaDto> saveTaskInProject(@RequestBody @Valid TareaDto dto,
			@AuthenticationPrincipal CustomUserDetails authUser, @PathVariable(name = "id") String projectId) {

		if (!projectService.existsProjectActive(projectId)) {
			throw new NoSuchElementException("Elementos no encontrados");
		}

		dto.setProject_id(projectId);

		return new ResponseEntity<TareaDto>(tareaService.save(dto, authUser.getUserId()), HttpStatus.CREATED);

	}

	@PatchMapping
	public ResponseEntity<ProjectDto> updateProject(@RequestBody @Valid ProjectDto dto,
			@AuthenticationPrincipal CustomUserDetails authUser) {

		return ResponseEntity.ok().body(projectService.save(dto, authUser.getUserId()));

	}

	@PatchMapping("/{projectId}/members/{userId}")
	public ResponseEntity<ProjectMemberDto> updateProjectMember(@RequestBody @Valid ProjectMemberDto dto,
			@AuthenticationPrincipal CustomUserDetails authUser, @PathVariable String projectId,
			@PathVariable Long userId) {

		projectMemberService.validationOwnerAndMemberProject(dto, userId, authUser.getUserId(), projectId);

		dto.setId(projectMemberService.findByUsuarioIdAndProjectIdGuid(userId, projectId).getId());
		dto.setUsuarioId(userId);
		dto.setProjectId(projectId);

		return new ResponseEntity<ProjectMemberDto>(projectMemberService.save(projectId, dto, authUser),
				HttpStatus.CREATED);

	}
	
	@DeleteMapping("/{projectId}/members/{userId}")
	public ResponseEntity<Void> deleteProjectMember(@AuthenticationPrincipal CustomUserDetails authUser,
			@PathVariable(required = true) String projectId,
			@PathVariable(required = true) Long userId) {

				
		projectMemberService.deleteProjectMember(authUser.getUserId(),projectId,userId);


		return ResponseEntity.noContent().build();
	}

	@GetMapping("/{projectId}/members")
	public ResponseEntity<List<ProjectMemberDto>> getProjectMembers(@AuthenticationPrincipal CustomUserDetails authUser,
			@PathVariable String projectId) {

		return ResponseEntity.ok(projectMemberService.findProjectMembersByProjectId(authUser.getUserId(), projectId));

	}

	@GetMapping("/{id}/tasks")
	public ResponseEntity<?> getTasksByProjectId(@PathVariable String id,
			@AuthenticationPrincipal CustomUserDetails authUser) {

		return ResponseEntity.ok(projectService.findTasksByProjectId(id, authUser.getUserId()));

	}

	@DeleteMapping("/{projectId}")
	public ResponseEntity<Void> eliminarProjecto(@PathVariable String projectId,
			@AuthenticationPrincipal CustomUserDetails authUser) {

		projectService.deleteProject(projectId, authUser.getUserId());

		return ResponseEntity.noContent().build();

	}

}
