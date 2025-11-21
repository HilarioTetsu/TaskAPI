package com.springboot.app.controllers;

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

import com.springboot.app.models.dtos.ProjectDto;
import com.springboot.app.models.dtos.ProjectMemberDto;
import com.springboot.app.models.dtos.TareaDto;
import com.springboot.app.models.services.IProjectMemberService;
import com.springboot.app.models.services.IProjectService;
import com.springboot.app.models.services.ITareaService;
import com.springboot.app.models.services.IUsuarioService;
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

	private final IUsuarioService usuarioService;

	public ProjectController(IProjectService projectService, ITareaService tareaService,
			IProjectMemberService projectMemberService, IUsuarioService usuarioService) {
		super();
		this.projectService = projectService;
		this.tareaService = tareaService;
		this.projectMemberService = projectMemberService;
		this.usuarioService = usuarioService;
	}

	@GetMapping("/roles")
	public ResponseEntity<?> getProjectRoles() {

		try {

			return new ResponseEntity<Object>(ProjectRole.values(), HttpStatus.OK);

		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<Object>("Error en el servidor", HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	@GetMapping("/{id}")
	public ResponseEntity<?> getByProjectId(@AuthenticationPrincipal CustomUserDetails authUser,
			@PathVariable String id) {

		try {

			return new ResponseEntity<Object>(projectService.findByProjectIdAndUserId(id, authUser.getUserId()),
					HttpStatus.OK);

		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<Object>("Error en el servidor", HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	@GetMapping
	public ResponseEntity<?> getAllProjects(@AuthenticationPrincipal CustomUserDetails authUser) {

		try {

			return new ResponseEntity<Object>(projectService.findByOwnerId(authUser.getUserId()), HttpStatus.OK);

		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<Object>("Error en el servidor", HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	@PostMapping
	public ResponseEntity<?> saveProject(@RequestBody @Valid ProjectDto dto,
			@AuthenticationPrincipal CustomUserDetails authUser) {

		try {

			return new ResponseEntity<Object>(projectService.save(dto, authUser.getUserId()), HttpStatus.CREATED);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<Object>("Error en el servidor", HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	@PostMapping("/{id}/members")
	public ResponseEntity<?> addProjectMember(@RequestBody @Valid ProjectMemberDto dto,
			@AuthenticationPrincipal CustomUserDetails authUser, @PathVariable("id") String projectId) {

		try {

			

			return new ResponseEntity<Object>(projectMemberService.save(projectId, dto, authUser), HttpStatus.CREATED);

		} catch (IllegalArgumentException e) {
	        return ResponseEntity.badRequest().body(e.getMessage());
	    } catch (SecurityException e) {
	        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
	    } catch (NoSuchElementException e) {
	        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
	    } catch (IllegalStateException e) {
	        return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
	    } catch (Exception e) {
	        e.printStackTrace();
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body("Error en el servidor");
	    }

	}

	@PostMapping("/{id}/task")
	public ResponseEntity<?> saveTaskInProject(@RequestBody @Valid TareaDto dto,
			@AuthenticationPrincipal CustomUserDetails authUser, @PathVariable(name = "id") String projectId) {
		try {

			if (!projectService.existsProjectActive(projectId)) {
				return new ResponseEntity<Object>("Elementos no encontrados", HttpStatus.NOT_FOUND);
			}

			dto.setProject_id(projectId);

			return new ResponseEntity<Object>(tareaService.save(dto, authUser.getUserId()), HttpStatus.CREATED);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<Object>("Error en el servidor", HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	@PatchMapping
	public ResponseEntity<?> updateProject(@RequestBody @Valid ProjectDto dto,
			@AuthenticationPrincipal CustomUserDetails authUser) {

		try {

			return new ResponseEntity<Object>(projectService.save(dto, authUser.getUserId()), HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<Object>("Error en el servidor", HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	@PatchMapping("/{projectId}/members/{userId}")
	public ResponseEntity<?> updateProjectMember(@RequestBody ProjectMemberDto dto,
			@AuthenticationPrincipal CustomUserDetails authUser, @PathVariable String projectId,
			@PathVariable Long userId) {

		try {

			if (projectMemberService.validationOwnerAndMemberProject(dto,userId, authUser.getUserId(), projectId)) {

				
				dto.setId(projectMemberService.findByUsuarioIdAndProjectIdGuid(userId, projectId).getId());
				dto.setUsuarioId(userId);
				dto.setProjectId(projectId);
				
				return new ResponseEntity<Object>(projectMemberService.save(projectId, dto, authUser), HttpStatus.CREATED);
				
			}

			
			
			return null;
		} catch (IllegalArgumentException e) {
	        return ResponseEntity.badRequest().body(e.getMessage());
	    } catch (SecurityException e) {
	        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
	    } catch (NoSuchElementException e) {
	        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
	    } catch (IllegalStateException e) {
	        return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
	    } catch (Exception e) {
	        e.printStackTrace();
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body("Error en el servidor");
	    }
	}
	
	
	@GetMapping("/{projectId}/members")
	public ResponseEntity<?> getProjectMembers(@AuthenticationPrincipal CustomUserDetails authUser,
			@PathVariable String projectId) {

		try {

			return new ResponseEntity<Object>(projectMemberService.findProjectMembersByProjectId(authUser.getUserId(),projectId), HttpStatus.OK);

		} catch (SecurityException e) {
	        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
	    }catch (NoSuchElementException e) {
	        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
	    } catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<Object>("Error en el servidor", HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}


	@GetMapping("/{id}/tasks")
	public ResponseEntity<?> getTasksByProjectId(@PathVariable String id,
			@AuthenticationPrincipal CustomUserDetails authUser) {

		try {

			return new ResponseEntity<Object>(projectService.findTasksByProjectId(id, authUser.getUserId()),
					HttpStatus.OK);

		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<Object>("Error en el servidor", HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}
	
	
	@DeleteMapping("/{projectId}")
	public ResponseEntity<?> eliminarProjecto(
			@PathVariable String projectId,
			@AuthenticationPrincipal CustomUserDetails authUser){
		
		try {

			projectService.deleteTarea(projectId,authUser.getUserId());
			
			return ResponseEntity.noContent().build();

		} catch (SecurityException e) {
	        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
	    } catch (NoSuchElementException e) {
	        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
	    } catch (IllegalStateException e) {
	        return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
	    } catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<Object>("Error al realizar la operacion", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

}
