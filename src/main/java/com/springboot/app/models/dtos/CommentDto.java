package com.springboot.app.models.dtos;

import java.time.LocalDateTime;
import java.util.List;

import com.springboot.app.models.entities.Comment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "CommentDto", description = "Representa un comentario en una tarea. Se usa tanto para crear como para listar.")
public class CommentDto {

	public CommentDto(Comment comment) {
		this.id = comment.getId();
		this.tareaId = comment.getTarea().getIdGuid();
		this.ownerUserId = comment.getAutor().getId();
		this.body = comment.getBody();
		this.mentionsUserIds = comment.getMentions() != null && comment.getMentions().size() > 0
				? comment.getMentions().stream().map(u -> u.getId()).toList()
				: null;
		this.confirmMediasStorageKeyId = comment.getAdjuntos() != null && comment.getAdjuntos().size() > 0
				? comment.getAdjuntos().stream().map(adj -> adj.getStorageKey()).toList()
				: null;
		this.status = comment.getStatus();
		this.fechaCreacion = comment.getFechaCreacion();
	}

	@Schema(description = "ID interno del comentario.", example = "101")
	private Long id;

	@Schema(description = "GUID de la tarea a la que pertenece el comentario.", example = "a1b2c3d4-e5f6-7890-1234-56789abcdef0")
	@NotBlank
	private String tareaId;

	@Schema(description = "ID del usuario autor del comentario.", example = "15")
	private Long ownerUserId;

	@Schema(description = "Contenido de texto del comentario.", example = "Adjunto la evidencia del error encontrado.")
	@NotBlank
	private String body;

	@Schema(description = "Lista de IDs de usuarios mencionados en el comentario.", example = "[2, 5]")
	private List<Long> mentionsUserIds;

	@Schema(description = "Al crear: Lista de Keys de S3 de archivos previamente subidos para confirmar/adjuntar. Al leer: puede venir nulo.", example = "[\"users/1/.../file.png\"]")
	private List<String> confirmMediasStorageKeyId;

	@Schema(description = "Estado del comentario (1=Activo, 0=Eliminado).", example = "1")
	private Short status;

	@Schema(description = "Fecha de creación del comentario.", example = "2025-10-21T15:30:00")
	private LocalDateTime fechaCreacion;
}