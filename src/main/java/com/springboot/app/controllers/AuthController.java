package com.springboot.app.controllers;

import java.io.Serializable;

import java.util.Map;


import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.springboot.app.models.dtos.AuthRequestDto;
import com.springboot.app.models.dtos.UsuarioDto;
import com.springboot.app.models.services.IUsuarioService;
import com.springboot.app.utils.Constants;
import com.springboot.app.utils.JwtUtil;

import jakarta.validation.Valid;

@RestController
@RequestMapping(Constants.URL_BASE_API_V1 + "/auth")
public class AuthController {

	private AuthenticationManager authManager;

	private JwtUtil jwtUtil;

	private final IUsuarioService usuarioService;

	public AuthController(AuthenticationManager authManager, JwtUtil jwtUtil, IUsuarioService usuarioService) {
		this.authManager = authManager;
		this.jwtUtil = jwtUtil;
		this.usuarioService = usuarioService;
	}

	@PostMapping("/login")
	public ResponseEntity<Map<String, Serializable>> login(@RequestBody AuthRequestDto request) {

		Authentication auth = authManager
				.authenticate(new UsernamePasswordAuthenticationToken(request.getData(), request.getPassword()));

		User principal = (User) auth.getPrincipal();

		String token = jwtUtil.generarToken(principal.getUsername(), principal.getAuthorities());

		return ResponseEntity.ok(Map.of("jwt", token, "expires", jwtUtil.getExpirationToken(token)));

	}

	@PostMapping("/signup")
	public ResponseEntity<UsuarioDto> save(@RequestBody @Valid UsuarioDto userDto) {

		return ResponseEntity.ok(usuarioService.save(userDto));

	}

}
