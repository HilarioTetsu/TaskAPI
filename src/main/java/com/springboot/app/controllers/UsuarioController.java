package com.springboot.app.controllers;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.springboot.app.models.dtos.UsuarioDto;
import com.springboot.app.models.services.IUsuarioService;
import com.springboot.app.utils.Constants;

@RestController
@RequestMapping(Constants.URL_BASE_API_V1+"/usuario")
public class UsuarioController {

	private final IUsuarioService usuarioService;

	public UsuarioController(IUsuarioService usuarioService) {
		this.usuarioService = usuarioService;
	}

	@GetMapping()
	public ResponseEntity<List<UsuarioDto>> getAllUsers() {

		
			return ResponseEntity.ok(usuarioService.findByStatusIs(Constants.STATUS_ACTIVE));

	}

	@GetMapping("/{data}")
	public ResponseEntity<UsuarioDto> getByEmailOrUsername(@PathVariable(required = true) String data) {


			UsuarioDto userDto = usuarioService.findByEmailOrUsernameAndStatusIs(data, data)
					.orElseThrow(() -> new NoSuchElementException("Usuario no encontrado"));

			return ResponseEntity.ok(userDto);
						
		
	}



}
