package com.springboot.app.models.dtos;

import com.springboot.app.models.entities.PrioridadTarea;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "PrioridadTareaDto", description = "Elemento del catálogo de prioridades.")
public class PrioridadTareaDto {

	public PrioridadTareaDto(PrioridadTarea p) {
		this.id = p.getId();
		this.prioridadTipo = p.getPrioridadTipo();
	}

	@Schema(description = "ID de la prioridad.", example = "3")
	private Short id;

	@Schema(description = "Nombre descriptivo de la prioridad.", example = "ALTA")
	private String prioridadTipo;
}