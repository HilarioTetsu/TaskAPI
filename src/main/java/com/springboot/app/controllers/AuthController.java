package com.springboot.app.controllers;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
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
@RequestMapping(Constants.URL_BASE_API_V1+"/auth")
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
	public ResponseEntity<?> login (@RequestBody AuthRequestDto request){
		
		try {
			
			Authentication auth = authManager.authenticate(
					new UsernamePasswordAuthenticationToken(request.getData(),request.getPassword())
					);
			
			User principal= (User) auth.getPrincipal();
			
			
			
			String token = jwtUtil.generarToken(principal.getUsername(),principal.getAuthorities());
			
			return ResponseEntity.ok(Map.of("jwt", token,"expires",jwtUtil.getExpirationToken(token)));
			
		} catch (AuthenticationException e) {
			return ResponseEntity.status(401).body(Map.of("error","Credenciales inválidas"));
		}
		
		
	}
	
	@PostMapping("/signup")
	public ResponseEntity<?> save(@RequestBody @Valid UsuarioDto userDto) {

		try {

			return new ResponseEntity<Object>(usuarioService.save(userDto), HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<Object>("Error al realizar la operacion", HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}
	
}
