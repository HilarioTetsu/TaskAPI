package com.springboot.app.controllers;

import java.util.NoSuchElementException;

import org.springframework.http.HttpStatus;
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
	public ResponseEntity<?> getAllUsers() {

		try {

			
			return new ResponseEntity<Object>(usuarioService.findByStatusIs(Constants.STATUS_ACTIVE), HttpStatus.OK);

		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<Object>("Error al realizar la operacion", HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	@GetMapping("/{data}")
	public ResponseEntity<?> getByEmailOrUsername(@PathVariable(required = true) String data) {

		try {

			UsuarioDto userDto = usuarioService.findByEmailOrUsernameAndStatusIs(data, data)
					.orElseThrow(() -> new NoSuchElementException("Usuario no encontrado"));

			return new ResponseEntity<Object>(userDto, HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<Object>("Error al realizar la operacion", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}



}
