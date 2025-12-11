package com.springboot.app.models.dtos;

import org.hibernate.validator.constraints.Length;
import com.springboot.app.models.entities.Tag;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Schema(name = "TagDto", description = "Etiqueta clasificatoria para tareas (ej. Bug, Feature, Urgente).")
public class TagDto {

	public TagDto(Tag tag) {
		this.id = tag.getId();
		this.name = tag.getName();
		this.color = tag.getColor();
		this.status = tag.getStatus();
	}

	@Schema(description = "ID interno del tag.", example = "5")
	private Integer id;

	@Schema(description = "Nombre de la etiqueta (se suele guardar en mayúsculas).", example = "URGENTE")
	@NotBlank
	@Length(max = 30)
	private String name;

	@Schema(description = "Color hexadecimal representativo.", example = "#FF5733")
	@NotBlank
	@Length(min = 7, max = 7)
	private String color;

	@Schema(description = "Estado del tag (1=Activo).", example = "1")
	private Short status;
}