package com.springboot.app.controllers;

import java.util.List;

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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping(Constants.URL_BASE_API_V1 + "/usuario")
@Tag(name = "Usuarios", description = "Operaciones relacionadas con usuarios: búsqueda, consulta de perfil y actualización del usuario autenticado.")
public class UsuarioController {

	private final IUsuarioService usuarioService;

	public UsuarioController(IUsuarioService usuarioService) {
		this.usuarioService = usuarioService;
	}

	@Operation(summary = "Buscar usuarios por username dentro de un proyecto", description = """
			Busca usuarios cuyo **username** contenga el término indicado,
			limitado al contexto de un proyecto.

			Reglas:
			- El usuario autenticado debe ser **miembro** del proyecto (`projectId`).
			- Se devuelve información básica para autocompletar (id, email, username, roles).
			""")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Lista de usuarios encontrada.", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = UsuarioAuthInfoDto.class)))),
			@ApiResponse(responseCode = "403", description = "El usuario no es miembro del proyecto.", content = @Content) })
	@GetMapping("/search")
	public ResponseEntity<List<UsuarioAuthInfoDto>> getUsersContainingUsernameInProject(
			@Parameter(description = "Texto parcial del username a buscar.", example = "team") @RequestParam(required = true) String term,
			@Parameter(description = "GUID del proyecto donde se está buscando.", example = "b3b6a1c5-9d8e-4f2c-9013-0b40e9f5f111") @RequestParam(required = true) String projectId,
			@AuthenticationPrincipal CustomUserDetails authUser) {

		return ResponseEntity
				.ok(usuarioService.findByUsernameContainingAndProjectId(term, projectId, authUser.getUserId()));

	}
	
	@GetMapping("/search/by-username-or-email")
	public ResponseEntity<List<UsuarioAuthInfoDto>> getUsersContainingUsernameOrEmail(
			@RequestParam(required = true) String term,
			@RequestParam(required = true) String projectId,
			@AuthenticationPrincipal CustomUserDetails authUser) {

		return ResponseEntity
				.ok(usuarioService.findByUsernameContainingOrEmailContaining(term, projectId, authUser.getUserId()));

	}

	@Operation(summary = "Listar usuarios activos", description = """
			Devuelve la lista de usuarios con estado **ACTIVO**.

			El uso típico sería para vistas administrativas o catálogos internos.
			La seguridad adicional (por rol) se maneja en la capa de seguridad global.
			""")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Lista de usuarios activos.", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = UsuarioDto.class)))) })
	@GetMapping()
	public ResponseEntity<List<UsuarioDto>> getAllUsers() {

		return ResponseEntity.ok(usuarioService.findByStatusIs(Constants.STATUS_ACTIVE));

	}

	@Operation(summary = "Buscar usuario por email o username", description = """
			Busca un usuario por email **o** username (case-insensitive),
			siempre que esté en estado ACTIVO.

			- Si `data` coincide con un email → se busca por email.
			- Si coincide con username → se busca por username.
			- Si no existe coincidencia → se lanza excepción de no encontrado.
			""")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Usuario encontrado.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UsuarioDto.class))),
			@ApiResponse(responseCode = "404", description = "Usuario no encontrado.", content = @Content) })
	@GetMapping("/{data}")
	public ResponseEntity<UsuarioDto> getByEmailOrUsername(
			@Parameter(description = "Email o username del usuario.", example = "user@example.com") @PathVariable(required = true) String data) {

		return ResponseEntity.ok(usuarioService.findByEmailOrUsernameAndStatusIs(data, data));

	}

	@Operation(summary = "Obtener información del usuario autenticado", description = """
			Devuelve información del usuario autenticado en base al token JWT:

			- id
			- email
			- username
			- roles

			Es útil para inicializar el contexto del frontend (datos del perfil/logged user).
			""")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Información del usuario autenticado.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UsuarioAuthInfoDto.class))),
			@ApiResponse(responseCode = "401", description = "No autenticado o token inválido.", content = @Content) })
	@GetMapping("/me")
	public ResponseEntity<UsuarioAuthInfoDto> getUserAuthInfo(@AuthenticationPrincipal CustomUserDetails authUser) {

		return ResponseEntity.ok(usuarioService.findUserById(authUser.getUserId()));

	}

	@Operation(summary = "Actualizar información del usuario autenticado", description = """
			Actualiza los datos del usuario autenticado:

			- email
			- username
			- password (requiere password actual)

			Reglas principales (implementadas en el servicio):
			- Se valida que `password` coincida con la contraseña actual.
			- Se valida que el nuevo email no esté en uso.
			- Se valida que el nuevo username no esté en uso.
			- Si `newPassword` tiene valor, se actualiza la contraseña.
			""")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Datos del usuario actualizados correctamente.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UsuarioAuthInfoDto.class))),
			@ApiResponse(responseCode = "400", description = "Datos inválidos o reglas de negocio incumplidas (email/username en uso).", content = @Content),
			@ApiResponse(responseCode = "401", description = "No autenticado.", content = @Content),
			@ApiResponse(responseCode = "403", description = "Credenciales actuales incorrectas.", content = @Content) })
	@PatchMapping("/me")
	public ResponseEntity<UsuarioAuthInfoDto> updateUserAuthInfo(@RequestBody @Valid UsuarioUpdateDto dto,
			@AuthenticationPrincipal CustomUserDetails authUser) {

		return ResponseEntity.ok(usuarioService.updateUserInfo(dto, authUser.getUserId()));

	}

}
