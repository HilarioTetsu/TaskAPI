package com.springboot.app.models.dtos;

import com.springboot.app.models.entities.TareaStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "TareaStatusDto", description = "Elemento del catálogo de estatus de tareas.")
public class TareaStatusDto {

	public TareaStatusDto(TareaStatus tareaStatus) {
		this.id = tareaStatus.getId();
		this.status = tareaStatus.getStatus();
	}

	@Schema(description = "ID del estatus.", example = "1")
	private Short id;

	@Schema(description = "Nombre descriptivo del estatus.", example = "EN PROCESO")
	private String status;
}