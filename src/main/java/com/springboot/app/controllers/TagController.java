package com.springboot.app.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.springboot.app.models.dtos.TagDto;
import com.springboot.app.models.services.ITagService;
import com.springboot.app.utils.Constants;

import jakarta.validation.Valid;

@RestController
@RequestMapping(Constants.URL_BASE_API_V1+"/tags")
public class TagController {

	private final ITagService tagService;

	public TagController(ITagService tagService) {
		super();
		this.tagService = tagService;
	}

	@GetMapping
	public ResponseEntity<?> getAll(@RequestParam(required = false, defaultValue = "10") int tamanio,
			@RequestParam(required = false, defaultValue = "0") int pagina) {

		try {

			return new ResponseEntity<Object>(tagService.getAll(pagina, tamanio), HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<Object>("Error al obtener la informacion", HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	@GetMapping("/name/{nameTag}")
	public ResponseEntity<?> getByName(@PathVariable(required = true) String nameTag) {

		try {

			return new ResponseEntity<Object>(tagService.findByNameContaining(nameTag), HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<Object>("Error al obtener la informacion", HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	@GetMapping("/{id}")
	public ResponseEntity<?> getByIdOrName(@PathVariable(required = false) Integer id,
			@RequestParam(required = false) String nameTag) {

		try {

			return new ResponseEntity<Object>(tagService.getTagActiveByIdOrName(id, nameTag), HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<Object>("Error al obtener la informacion", HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	@PostMapping
	public ResponseEntity<?> crearTag(@Valid @RequestBody TagDto dto) {

		try {

			return new ResponseEntity<Object>(tagService.save(dto), HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<Object>("Error al obtener la informacion", HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}
	

	
	@PatchMapping
	public ResponseEntity<?> actualizarTag(@Valid @RequestBody TagDto dto) {

		try {

			if (tagService.getTagActiveByIdOrName(dto.getId(), null).isEmpty()) {
				return new ResponseEntity<Object>("Tag no encontrado", HttpStatus.NOT_FOUND);
			}
			
			
			return new ResponseEntity<Object>(tagService.save(dto), HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<Object>("Error al obtener la informacion", HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

}
