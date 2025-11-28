package com.springboot.app.controllers;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.springboot.app.models.dtos.UsuarioAuthInfoDto;
import com.springboot.app.models.dtos.UsuarioDto;
import com.springboot.app.models.dtos.UsuarioUpdateDto;
import com.springboot.app.models.services.IUsuarioService;
import com.springboot.app.utils.Constants;
import com.springboot.app.utils.CustomUserDetails;

import jakarta.validation.Valid;

@RestController
@RequestMapping(Constants.URL_BASE_API_V1+"/usuario")
public class UsuarioController {

	private final IUsuarioService usuarioService;

	public UsuarioController(IUsuarioService usuarioService) {
		this.usuarioService = usuarioService;
	}

	@GetMapping("/search")
	public ResponseEntity<List<UsuarioAuthInfoDto>> getUsersContainingUsername(@RequestParam(required = true) String term,
			@RequestParam(required = true) String projectId,
			@AuthenticationPrincipal CustomUserDetails authUser
			) {

		
			return ResponseEntity.ok(usuarioService.findByUsernameContainingAndProjectId(term,projectId,authUser.getUserId()));

	}
	
	
	@GetMapping()
	public ResponseEntity<List<UsuarioDto>> getAllUsers() {

		
			return ResponseEntity.ok(usuarioService.findByStatusIs(Constants.STATUS_ACTIVE));

	}

	@GetMapping("/{data}")
	public ResponseEntity<UsuarioDto> getByEmailOrUsername(@PathVariable(required = true) String data) {


			return ResponseEntity.ok(usuarioService.findByEmailOrUsernameAndStatusIs(data, data));
						
		
	}
	
	@GetMapping("/me")
	public ResponseEntity<UsuarioAuthInfoDto> getUserAuthInfo(@AuthenticationPrincipal CustomUserDetails authUser) {

			
			return ResponseEntity.ok(usuarioService.findUserById(authUser.getUserId()));
						
		
	}
	
	@PatchMapping("/me")
	public ResponseEntity<UsuarioAuthInfoDto> updateUserAuthInfo(@RequestBody @Valid UsuarioUpdateDto dto,
			@AuthenticationPrincipal CustomUserDetails authUser) {

			
			return ResponseEntity.ok(usuarioService.updateUserInfo(dto,authUser.getUserId()));
						
		
	}

}
