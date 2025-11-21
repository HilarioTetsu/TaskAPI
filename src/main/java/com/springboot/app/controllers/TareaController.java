package com.springboot.app.controllers;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

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

import com.springboot.app.models.dtos.TareaDto;
import com.springboot.app.models.entities.Tag;
import com.springboot.app.models.entities.Tarea;
import com.springboot.app.models.services.ITagService;
import com.springboot.app.models.services.ITareaService;
import com.springboot.app.utils.CustomUserDetails;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/ExampleAPI/v1/tareas")
public class TareaController {

	private final ITareaService tareaService;

	private final ITagService tagService;

	public TareaController(ITareaService tareaService, ITagService tagService) {
		this.tareaService = tareaService;
		this.tagService = tagService;
	}

	@GetMapping()	
	public ResponseEntity<?> getAllTareas(@RequestParam(required = false, defaultValue = "0") Integer pagina,
			@RequestParam(required = false, defaultValue = "5") Integer tamanio,
			@RequestParam(required = false) List<Short> tareaStatusIds,
			@RequestParam(required = false) List<Short> prioridadIds,
			@RequestParam(required = false) LocalDate fechaLimiteDesde,
			@RequestParam(required = false) LocalDate fechaLimiteHasta,
			@RequestParam(required = false) String busquedaDesc,
			@RequestParam(required = false) String busquedaTitulo,
			@RequestParam(required = false, defaultValue = "fecha_limite,desc;") String sorts,			
			@AuthenticationPrincipal CustomUserDetails authUser) {

		try {
			


			return new ResponseEntity<Object>(tareaService.getAllActives(pagina, tamanio, tareaStatusIds, prioridadIds,
					fechaLimiteDesde, fechaLimiteHasta, busquedaDesc, busquedaTitulo, sorts,authUser.getUserId()), HttpStatus.OK);

		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<Object>("Error al obtener la informacion", HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	@GetMapping("/{id}")
	public ResponseEntity<?> getByIdTask(@PathVariable(name = "id", required = true) String idTask,
			@AuthenticationPrincipal CustomUserDetails authUser) {

		try {

			Optional<TareaDto> tarea = tareaService.findByIdGuid(idTask,authUser.getUserId());

			if (tarea.isEmpty())
				return new ResponseEntity<Object>("Tarea no encontrada", HttpStatus.NOT_FOUND);

			return new ResponseEntity<Object>(tarea.get(), HttpStatus.OK);

		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<Object>("Error al obtener la informacion", HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	@PatchMapping
	public ResponseEntity<?> actualizarTarea(@Valid @RequestBody TareaDto tareaDto,
			@AuthenticationPrincipal CustomUserDetails authUser) {

		try {

			Optional<TareaDto> tarea = tareaService.findByIdGuid(tareaDto.getIdGuid(),authUser.getUserId());

			if (tarea.isEmpty())
				return new ResponseEntity<Object>("Informacion no encontrada", HttpStatus.NOT_FOUND);

			return new ResponseEntity<Object>(tareaService.save(tareaDto,authUser.getUserId()), HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<Object>("Error al obtener la informacion", HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	@PostMapping("/{idTarea}/tags/{idTag}")
	public ResponseEntity<?> asignarTag(@PathVariable(required = true) String idTarea,
			@PathVariable(required = true) Integer idTag,
			@AuthenticationPrincipal CustomUserDetails authUser) {

		try {

			Optional<Tarea> tarea = tareaService.findTareaByIdGuid(idTarea,authUser.getUserId());

			if (tarea.isEmpty())
				return new ResponseEntity<Object>("Tarea no encontrada", HttpStatus.NOT_FOUND);

			Optional<Tag> tag = tagService.getTagActiveById(idTag);

			if (tag.isEmpty())
				return new ResponseEntity<Object>("Tag no encontrado", HttpStatus.NOT_FOUND);

			return new ResponseEntity<Object>(tareaService.asignarTag(tarea.get(), tag.get()), HttpStatus.OK);

		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<Object>("Error al obtener la informacion", HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	@PostMapping
	public ResponseEntity<?> crearTarea(@Valid @RequestBody TareaDto tareaDto,
			@AuthenticationPrincipal CustomUserDetails authUser) {

		try {

			return new ResponseEntity<Object>(tareaService.save(tareaDto,authUser.getUserId()), HttpStatus.CREATED);

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
	
	
	@GetMapping("/project/{projectId}/assigned")
	public ResponseEntity<?> getTareasAsignadasByProjectId(@PathVariable String projectId,
			@AuthenticationPrincipal CustomUserDetails authUser) {

		try {

			return ResponseEntity.ok().body(tareaService.getTareasAsignadasByProjectId(projectId,authUser.getUserId()));

		} catch (NoSuchElementException e) {
	        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
		} catch (SecurityException e) {
	        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<Object>("Error al obtener la informacion", HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}
	
	
	
	@PostMapping("/{tareaId}/assign")
	public ResponseEntity<?> asignarTarea(@RequestBody List<Long> userIds,
			@PathVariable String tareaId,
			@AuthenticationPrincipal CustomUserDetails authUser){
		
		try {

			tareaService.asignarTarea(userIds,tareaId,authUser.getUserId());
			
			return ResponseEntity.ok().body("Asignacion realizada");

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
	
	@DeleteMapping("/{tareaId}")
	public ResponseEntity<?> eliminarTarea(
			@PathVariable String tareaId,
			@AuthenticationPrincipal CustomUserDetails authUser){
		
		try {

			tareaService.deleteTarea(tareaId,authUser.getUserId());
			
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
