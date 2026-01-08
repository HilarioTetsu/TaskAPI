package com.springboot.app.models.dtos;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(name = "CommentUpdateDto", description = "Datos para la actualización parcial de un comentario.")
public class CommentUpdateDto {

    @Schema(description = "Nuevo contenido del cuerpo del comentario.", example = "Actualizando el estado de la tarea.")
	@NotBlank
	private String body;
	
    @Schema(description = "Lista actualizada de IDs de usuarios mencionados.", example = "[10, 15]")
	private List<Long> mentionsUserIds;
	
}