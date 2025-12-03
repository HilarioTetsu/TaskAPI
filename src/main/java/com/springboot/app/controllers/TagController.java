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
@RequestMapping(Constants.URL_BASE_API_V1 + "/tags")
@Tag(name = "Tags", description = "Gestión de etiquetas (tags) reutilizables que pueden asociarse a tareas.")
public class TagController {

	private final ITagService tagService;

	public TagController(ITagService tagService) {
		super();
		this.tagService = tagService;
	}

	@Operation(summary = "Listar tags", description = """
			Retorna un listado paginado de todos los tags activos.

			Está pensado como catálogo general de etiquetas disponibles para el usuario.
			""")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Listado paginado de tags devuelto correctamente.", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = TagDto.class)))) })
	@GetMapping
	public ResponseEntity<Page<TagDto>> getAll(
			@Parameter(description = "Número de página (0-based).", example = "0") @RequestParam(defaultValue = "0") Integer pagina,
			@Parameter(description = "Tamaño de página.", example = "10") @RequestParam(defaultValue = "10") Integer tamanio,
			@AuthenticationPrincipal CustomUserDetails authUser) {

		return ResponseEntity.ok(tagService.getAll(pagina, tamanio));

	}

	@Operation(summary = "Buscar tags por nombre (contiene)", description = """
			Busca tags cuyo nombre contenga la cadena proporcionada.
			Útil para autocompletar al momento de asignar etiquetas.
			""")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Lista de tags encontrados.", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = TagDto.class)))) })
	@GetMapping("/name/{nameTag}")
	public ResponseEntity<List<TagDto>> getByName(
			@Parameter(description = "Texto a buscar dentro del nombre del tag.", example = "URGENTE") @PathVariable String nameTag,
			@AuthenticationPrincipal CustomUserDetails authUser) {

		return ResponseEntity.ok(tagService.findByNameContaining(nameTag));

	}

	@Operation(summary = "Obtener un tag por id o nombre", description = """
			Obtiene un tag específico.

			- Si se envía únicamente `id`, se busca por identificador.
			- Si se envía únicamente `nameTag`, se busca por nombre.
			- Si se envían ambos, se puede considerar que el servicio aplique un OR o use uno de los dos
			  (según tu implementación de servicio).
			""")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Tag encontrado.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = TagDto.class))),
			@ApiResponse(responseCode = "404", description = "Tag no encontrado.", content = @Content) })
	@GetMapping("/{id}")
	public ResponseEntity<TagDto> getByIdOrName(
			@Parameter(description = "ID del tag", example = "1") @PathVariable Integer id,
			@Parameter(description = "Nombre del tag, opcional. Si se envía, se puede usar como criterio alterno de búsqueda.", example = "URGENTE") @RequestParam(required = false) String nameTag,
			@AuthenticationPrincipal CustomUserDetails authUser) {

		return ResponseEntity.ok(tagService.getTagActiveByIdOrName(id, nameTag));

	}

	@Operation(summary = "Crear un nuevo tag", description = """
			Crea un nuevo tag.

			- El nombre suele normalizarse a mayúsculas a nivel de servicio.
			- Si ya existe un tag con el mismo nombre, se lanza una excepción de conflicto.
			""")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Tag creado correctamente.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = TagDto.class))),
			@ApiResponse(responseCode = "400", description = "Datos inválidos (por ejemplo, nombre vacío).", content = @Content),
			@ApiResponse(responseCode = "409", description = "Conflicto: ya existe un tag con el mismo nombre.", content = @Content) })
	@PostMapping
	public ResponseEntity<TagDto> crearTag(@Valid @RequestBody TagDto dto,
			@AuthenticationPrincipal CustomUserDetails authUser) {

		return ResponseEntity.ok(tagService.save(dto, authUser.getUserId()));

	}

	@Operation(summary = "Actualizar un tag existente", description = """
			Actualiza la información de un tag:
			- nombre
			- color
			- estatus

			El tag debe existir y el usuario debe tener permisos para editarlo.
			""")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Tag actualizado correctamente.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = TagDto.class))),
			@ApiResponse(responseCode = "404", description = "Tag no encontrado.", content = @Content) })
	@PatchMapping
	public ResponseEntity<TagDto> actualizarTag(@Valid @RequestBody TagDto dto,
			@AuthenticationPrincipal CustomUserDetails authUser) {

		return ResponseEntity.ok(tagService.save(dto, authUser.getUserId()));

	}

	@Operation(summary = "Eliminar tag", description = """
			Elimina lógicamente un tag.

			- Si el tag está asociado a tareas, se eliminan primero las asociaciones
			  (por ejemplo, registros en la tabla de enlace tarea-tag).
			- Luego el tag se marca como INACTIVO.
			""")
	@ApiResponses({
			@ApiResponse(responseCode = "204", description = "Tag eliminado correctamente.", content = @Content),
			@ApiResponse(responseCode = "404", description = "Tag no encontrado.", content = @Content) })
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteTag(
			@Parameter(description = "ID del tag a eliminar", example = "1") @PathVariable Integer id,
			@AuthenticationPrincipal CustomUserDetails authUser) {

		tagService.deleteTag(id, authUser.getUserId());

		return ResponseEntity.noContent().build();

	}

}
