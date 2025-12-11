package com.springboot.app.controllers;



import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.springboot.app.models.dtos.InvitationDto;
import com.springboot.app.models.dtos.InvitationViewDto;
import com.springboot.app.models.services.IInvitationService;
import com.springboot.app.utils.Constants;
import com.springboot.app.utils.CustomUserDetails;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Tag(name = "Invitaciones", description = "Gestión de invitaciones para proyectos: consulta de estatus, envío y confirmación (aceptar / rechazar).")
@RestController
@RequestMapping(Constants.URL_BASE_API_V1 + "/invitations")
public class InvitationController {

	private final IInvitationService invitationService;

	public InvitationController(IInvitationService invitationService) {
		super();
		this.invitationService = invitationService;
	}

	@Operation(summary = "Obtener catálogo de estatus de invitaciones", description = """
			Devuelve el catálogo de estatus posibles para una invitación.

			Ejemplo típico:
			- 2 = PENDING
			- 3 = READY
			- 4 = ACCEPTED
			- 5 = REJECTED
			""")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Catálogo de estatus de invitaciones devuelto correctamente.", content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"2\":\"PENDING\",\"4\":\"ACCEPTED\",\"5\":\"REJECTED\"}"))) })
	@GetMapping("/statuses")
	public ResponseEntity<Map<Short, String>> getInvitationStatuses() {

		return ResponseEntity.ok(invitationService.getInvitationStatuses());

	}
	
	
	@Operation(
	        summary = "Listar invitaciones recibidas o enviadas",
	        description = """
	            Obtiene un listado paginado de las invitaciones asociadas al usuario.
	            
	            - Puede filtrar por estatus específico (PENDING, ACCEPTED, REJECTED).
	            - Permite ordenamiento dinámico.
	            """
	    )
	    @ApiResponses({
	        @ApiResponse(
	            responseCode = "200",
	            description = "Pagina de invitaciones obtenida correctamente.",
	            content = @Content(
	                mediaType = "application/json",
	                array = @ArraySchema(schema = @Schema(implementation = InvitationViewDto.class))
	            )
	        )
	    })
		@GetMapping
		public ResponseEntity<Page<InvitationViewDto>> getAllInvitations(@RequestParam(required = false) String status,
				@Parameter(description = "Número de página (0-based).", example = "0") @RequestParam(defaultValue = "0") Integer pagina,
				@Parameter(description = "Tamaño de página.", example = "5") @RequestParam(defaultValue = "5") Integer tamanio,
				@Parameter(description = "Ordenamiento. Formato: campo,dir; Ej: fecha_creacion,desc;", example = "fecha_creacion,desc;") @RequestParam(defaultValue = "fecha_creacion,desc;") String sorts,
				@AuthenticationPrincipal CustomUserDetails authUser) {

			return ResponseEntity.ok(invitationService.getAllInvitations(authUser.getUserId(),status,pagina,tamanio,sorts));

		}

	@Operation(summary = "Invitar usuario a un proyecto", description = """
			Crea una invitación para que un usuario se una a un proyecto con un rol específico.

			Reglas principales (en la capa de servicio):
			- El usuario autenticado debe tener permisos sobre el proyecto (normalmente OWNER).
			- El usuario invitado debe existir y no estar ya invitado/miembro con el mismo rol.
			- El estatus inicial de la invitación suele ser PENDING.
			""")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Invitación creada correctamente.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = InvitationDto.class))),
			@ApiResponse(responseCode = "400", description = "Datos inválidos en la petición (usuario/proyecto/rol).", content = @Content),
			@ApiResponse(responseCode = "403", description = "El usuario autenticado no tiene permisos para invitar a este proyecto.", content = @Content),
			@ApiResponse(responseCode = "409", description = "Conflicto: ya existe una invitación similar o el usuario ya es miembro.", content = @Content) })
	@PostMapping
	public ResponseEntity<InvitationDto> inviteUserToProject(@RequestBody @Valid InvitationDto dto,
			@AuthenticationPrincipal CustomUserDetails authUser) {

		return ResponseEntity.ok(invitationService.inviteUserToProject(dto, authUser.getUserId()));

	}

	@Operation(summary = "Confirmar invitación a proyecto (aceptar o rechazar)", description = """
			Permite que el usuario invitado **acepte** o **rechace** una invitación existente.

			Reglas principales (en la capa de servicio):
			- `invitationId` debe existir y pertenecer al usuario autenticado.
			- `status` suele usar:
			    - 4 = ACCEPTED
			    - 5 = REJECTED
			- Una vez aceptada o rechazada, la invitación no debería poder cambiarse nuevamente.
			""")
	@ApiResponses({
			@ApiResponse(responseCode = "204", description = "Invitación confirmada correctamente (aceptada o rechazada).", content = @Content),
			@ApiResponse(responseCode = "400", description = "Estatus inválido o reglas de negocio incumplidas.", content = @Content),
			@ApiResponse(responseCode = "403", description = "El usuario autenticado no es el invitado o no tiene permisos.", content = @Content),
			@ApiResponse(responseCode = "404", description = "Invitación no encontrada.", content = @Content) })
	@PatchMapping("/{invitationId}/{status}")
	public ResponseEntity<Void> confirmInvitation(
			@Parameter(description = "Nuevo estatus de la invitación (4=ACCEPTED, 5=REJECTED).", example = "4") @PathVariable(required = true) Short status,
			@Parameter(description = "Identificador GUID de la invitación.", example = "e7f5c1e9-1234-4a56-b789-0abc1234def0") @PathVariable(required = true) String invitationId,
			@AuthenticationPrincipal CustomUserDetails authUser) {

		invitationService.confirmInvitationToProject(invitationId, status, authUser.getUserId());

		return ResponseEntity.noContent().build();

	}

}
