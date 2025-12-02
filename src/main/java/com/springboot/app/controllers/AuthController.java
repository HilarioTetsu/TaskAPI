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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping(Constants.URL_BASE_API_V1 + "/auth")
@Tag(
	    name = "Autenticación",
	    description = "Endpoints para autenticación de usuarios y obtención de tokens JWT."
	)
public class AuthController {

	private AuthenticationManager authManager;

	private JwtUtil jwtUtil;

	private final IUsuarioService usuarioService;

	public AuthController(AuthenticationManager authManager, JwtUtil jwtUtil, IUsuarioService usuarioService) {
		this.authManager = authManager;
		this.jwtUtil = jwtUtil;
		this.usuarioService = usuarioService;
	}

    @Operation(
            summary = "Iniciar sesión",
            description = """
                    Autentica a un usuario por *email* o *username* y devuelve un token JWT.
                    El campo **data** puede ser el correo electrónico o el nombre de usuario registrado.
                    """
        )
        @ApiResponses({
            @ApiResponse(
                responseCode = "200",
                description = "Autenticación correcta. Se devuelve el JWT y su fecha de expiración.",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(
                        example = """
                        {
                          "jwt": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                          "expires": "2025-12-01T17:00:00Z"
                        }
                        """
                    )
                )
            ),
            @ApiResponse(
                responseCode = "401",
                description = "Credenciales inválidas (usuario o contraseña incorrectos).",
                content = @Content
            )
        })
	@PostMapping("/login")
	public ResponseEntity<Map<String, Serializable>> login(@RequestBody @Valid AuthRequestDto request) {

		Authentication auth = authManager
				.authenticate(new UsernamePasswordAuthenticationToken(request.getData(), request.getPassword()));

		User principal = (User) auth.getPrincipal();

		String token = jwtUtil.generarToken(principal.getUsername(), principal.getAuthorities());

		return ResponseEntity.ok(Map.of("jwt", token, "expires", jwtUtil.getExpirationToken(token)));

	}

    
    
    
    
    @Operation(
            summary = "Registrar un nuevo usuario",
            description = """
                    Crea un nuevo usuario en el sistema.
                    - Valida que el email y el username no estén en uso.
                    - Permite especificar los roles por su ID.
                    El password se almacena encriptado (BCrypt) y no se devuelve en la respuesta.
                    """
        )
        @ApiResponses({
            @ApiResponse(
                responseCode = "200",
                description = "Usuario registrado correctamente.",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = UsuarioDto.class)
                )
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Datos inválidos en el cuerpo de la petición (validaciones de campos).",
                content = @Content
            ),
            @ApiResponse(
                responseCode = "409",
                description = "Conflicto: el email o el username ya existen en el sistema.",
                content = @Content
            )
        })
	@PostMapping("/signup")
	public ResponseEntity<UsuarioDto> save(@RequestBody @Valid UsuarioDto userDto) {

		return ResponseEntity.ok(usuarioService.save(userDto));

	}

}
