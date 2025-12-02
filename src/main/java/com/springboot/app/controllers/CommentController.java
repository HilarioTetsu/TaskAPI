package com.springboot.app.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.springboot.app.models.dtos.CommentDto;
import com.springboot.app.models.dtos.CommentUpdateDto;
import com.springboot.app.models.dtos.UploadRequestDto;
import com.springboot.app.models.dtos.UploadResponseDto;
import com.springboot.app.models.entities.Media;
import com.springboot.app.models.services.ICommentService;
import com.springboot.app.models.services.IMediaService;
import com.springboot.app.models.services.MediaStorageService;
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
import jakarta.validation.constraints.NotNull;

@RestController
@RequestMapping(Constants.URL_BASE_API_V1 + "/comment")
@Tag(
	    name = "Comentarios",
	    description = "Gestión de comentarios sobre tareas, incluyendo adjuntos (archivos) relacionados."
	)
public class CommentController {

	private final ICommentService commentService;

	private final MediaStorageService storageService;

	private final IMediaService mediaService;

	@Value("${app.s3.maxSizeBytes}")
	long maxSizeBytes;

	public CommentController(ICommentService commentService, MediaStorageService storageService,
			IMediaService mediaService) {
		super();
		this.commentService = commentService;
		this.storageService = storageService;
		this.mediaService = mediaService;
	}

	
    @Operation(
            summary = "Listar comentarios",
            description = """
                    Retorna los comentarios del usuario autenticado.
                    
                    - Si **no** se envía `tareaId`, lista todos los comentarios creados por el usuario.
                    - Si se envía `tareaId`, lista los comentarios asociados a esa tarea, siempre y cuando
                      el usuario tenga permisos sobre ella (owner/asignado/miembro de proyecto).
                    """
        )
        @ApiResponses({
            @ApiResponse(
                responseCode = "200",
                description = "Listado paginado de comentarios.",
                content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = CommentDto.class))
                )
            ),
            @ApiResponse(
                responseCode = "403",
                description = "El usuario no tiene permisos para ver los comentarios de la tarea indicada.",
                content = @Content
            )
        })
	@GetMapping
	public ResponseEntity<Page<CommentDto>> getAllComments(@RequestParam(required = false, defaultValue = "0") Integer pagina,
			@RequestParam(required = false, defaultValue = "5") Integer tamanio,
			@RequestParam(required = false, defaultValue = "fecha_creacion,desc;") String sorts,
			@RequestParam(required = false) String tareaId, @AuthenticationPrincipal CustomUserDetails authUser) {

		if (StringUtils.hasText(tareaId)) {

			return ResponseEntity.ok()
					.body(commentService.getAllByTareaId(pagina, tamanio, sorts, authUser.getUserId(), tareaId));

		}

		return ResponseEntity.ok().body(commentService.getAll(pagina, tamanio, sorts, authUser.getUserId()));

	}

    
    
    
    @Operation(
            summary = "Generar URLs pre-firmadas para subir adjuntos",
            description = """
                    Genera URLs pre-firmadas de S3 para subir archivos adjuntos (como imágenes o documentos).
                    
                    - Máximo **10 archivos** por petición.
                    - Cada archivo debe respetar el tamaño máximo configurado (`app.s3.maxSizeBytes`).
                    - Se crean registros `Media` en estado *PENDING* asociados al usuario autenticado.
                    - Después de subirlos a S3, se deben confirmar en el POST `/comment` usando
                      el campo `confirmMediaStorageKeyId` de `CommentDto`.
                    """
        )
        @ApiResponses({
            @ApiResponse(
                responseCode = "200",
                description = "URLs de subida generadas correctamente.",
                content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = UploadResponseDto.class))
                )
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Solicitud inválida (más de 10 archivos o tamaño excedido).",
                content = @Content
            )
        })
	@PostMapping("/attachment/presign")
	public ResponseEntity<List<UploadResponseDto>> createUploadUrl(@RequestBody List<UploadRequestDto> adjuntos,
			@AuthenticationPrincipal CustomUserDetails authUser) {

		if (adjuntos.size() > 10)
			throw new IllegalArgumentException("Máximo 10 archivos");

		List<UploadResponseDto> urlResponses = mediaService.createUploadUrls(adjuntos, maxSizeBytes,
				authUser.getUserId());

		return ResponseEntity.ok().body(urlResponses);

	}

    
    
    
    @Operation(
            summary = "Crear un comentario y confirmar adjuntos",
            description = """
                    Crea un nuevo comentario asociado a una tarea y, opcionalmente,
                    confirma los adjuntos previamente subidos a S3.
                    
                    - Si `confirmMediaStorageKeyId` está vacío o es null, se crea un comentario sin adjuntos.
                    - Si se envían claves en `confirmMediaStorageKeyId`:
                      - Se valida el tamaño real en S3 (`verifySizeFiles`).
                      - Se actualiza el estado de esos `Media` a *READY*.
                      - Se asocian dichos adjuntos al comentario creado.
                    """
        )
        @ApiResponses({
            @ApiResponse(
                responseCode = "201",
                description = "Comentario creado correctamente junto con sus adjuntos (si aplica).",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = CommentDto.class)
                )
            ),
            @ApiResponse(
                responseCode = "200",
                description = "Comentario creado sin adjuntos (cuando no se envía `confirmMediaStorageKeyId`).",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = CommentDto.class)
                )
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Alguno de los archivos excede el límite permitido de tamaño.",
                content = @Content
            ),
            @ApiResponse(
                responseCode = "403",
                description = "El usuario no tiene permisos sobre la tarea asociada.",
                content = @Content
            )
        })
	@PostMapping
	public ResponseEntity<CommentDto> createCommAndConfirmAttachments(@RequestBody @Valid CommentDto dto,
			@AuthenticationPrincipal CustomUserDetails authUser) {

		if (dto.getConfirmMediaStorageKeyId() == null || dto.getConfirmMediaStorageKeyId().size() == 0) {
			return ResponseEntity.ok().body(commentService.saveComment(dto, authUser.getUserId()));
		}

		if (!storageService.verifySizeFiles(dto.getConfirmMediaStorageKeyId())) {
			throw new IllegalArgumentException("Archivo excede el límite de 30MB");
		}

		List<Media> mediasSaved = mediaService.updateStatusMedia(dto.getConfirmMediaStorageKeyId(),
				authUser.getUserId());

		return ResponseEntity.status(HttpStatus.CREATED)
				.body(commentService.saveComment(dto, mediasSaved, authUser.getUserId()));

	}

    
    @Operation(
            summary = "Eliminar un comentario",
            description = """
                    Elimina lógicamente un comentario.
                    
                    - Solo puede eliminarlo el autor del comentario o un OWNER del proyecto de la tarea asociada.
                    - Si el comentario tiene adjuntos, estos también se marcan como inactivos.
                    """
        )
        @ApiResponses({
            @ApiResponse(
                responseCode = "204",
                description = "Comentario eliminado correctamente.",
                content = @Content
            ),
            @ApiResponse(
                responseCode = "403",
                description = "El usuario no tiene permisos para eliminar este comentario.",
                content = @Content
            ),
            @ApiResponse(
                responseCode = "404",
                description = "Comentario no encontrado.",
                content = @Content
            )
        })
	@DeleteMapping("/{commentId}")
	public ResponseEntity<Void> deleteComment(@PathVariable @NotNull Long commentId,
			@AuthenticationPrincipal CustomUserDetails authUser) {

		commentService.deleteComment(commentId, authUser.getUserId());

		return ResponseEntity.noContent().build();

	}

    
    @Operation(
            summary = "Actualizar un comentario",
            description = """
                    Actualiza el cuerpo y las menciones de un comentario existente.
                    
                    - Solo el autor del comentario puede modificarlo.
                    """
        )
        @ApiResponses({
            @ApiResponse(
                responseCode = "200",
                description = "Comentario actualizado correctamente.",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = CommentDto.class)
                )
            ),
            @ApiResponse(
                responseCode = "403",
                description = "El usuario no tiene permisos para modificar este comentario.",
                content = @Content
            ),
            @ApiResponse(
                responseCode = "404",
                description = "Comentario no encontrado.",
                content = @Content
            )
        })
	@PatchMapping("/{commentId}")
	public ResponseEntity<CommentDto> updateComment(@RequestBody CommentUpdateDto dto, @PathVariable Long commentId,
			@AuthenticationPrincipal CustomUserDetails authUser) {

		return ResponseEntity.ok().body(commentService.updateComment(commentId, dto, authUser.getUserId()));

	}

}
