package com.springboot.app.controllers;

import java.util.List;

import org.springframework.data.domain.Page;
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

import com.springboot.app.models.dtos.TagDto;
import com.springboot.app.models.services.ITagService;
import com.springboot.app.utils.Constants;
import com.springboot.app.utils.CustomUserDetails;

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
	public ResponseEntity<Page<TagDto>> getAll(@RequestParam(required = false, defaultValue = "10") int tamanio,
			@RequestParam(required = false, defaultValue = "0") int pagina) {


		return ResponseEntity.ok(tagService.getAll(pagina, tamanio));
		
		
				

	}

	@GetMapping("/name/{nameTag}")
	public ResponseEntity<List<TagDto>> getByName(@PathVariable(required = true) String nameTag) {

		
		return ResponseEntity.ok(tagService.findByNameContaining(nameTag));
								

	}

	@GetMapping("/{id}")
	public ResponseEntity<TagDto> getByIdOrName(@PathVariable(required = false) Integer id,
			@RequestParam(required = false) String nameTag) {


			return ResponseEntity.ok(tagService.getTagActiveByIdOrName(id, nameTag));
			

	}

	@PostMapping
	public ResponseEntity<TagDto> crearTag(@Valid @RequestBody TagDto dto,
			@AuthenticationPrincipal CustomUserDetails authUser) {

		
		
		return ResponseEntity.ok(tagService.save(dto,authUser.getUserId()));
		

	}
	

	
	@PatchMapping
	public ResponseEntity<TagDto> actualizarTag(@Valid @RequestBody TagDto dto,
			@AuthenticationPrincipal CustomUserDetails authUser) {

		return ResponseEntity.ok(tagService.save(dto,authUser.getUserId()));					


	}
	
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteTag(@PathVariable Integer id,
			@AuthenticationPrincipal CustomUserDetails authUser) {

		tagService.deleteTag(id,authUser.getUserId());
		
		return ResponseEntity.noContent().build();					


	}

}
