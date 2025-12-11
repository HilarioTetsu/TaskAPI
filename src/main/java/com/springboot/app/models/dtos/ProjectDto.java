package com.springboot.app.models.dtos;

import java.util.List;
import org.hibernate.validator.constraints.Length;
import com.springboot.app.models.entities.Project;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Schema(name = "ProjectDto", description = "Información completa de un proyecto, incluyendo tareas asociadas si aplica.")
public class ProjectDto {

    @Schema(description = "Identificador único (UUID) del proyecto.", example = "123e4567-e89b-12d3-a456-426614174000")
	@Length(min = 36,max = 36)
	private String idGuid;
	
    @Schema(description = "Nombre del proyecto.", example = "Desarrollo API REST", requiredMode = Schema.RequiredMode.REQUIRED)
	@Length(max = 100)
	@NotBlank
	private String name;
	
    @Schema(description = "Descripción detallada del proyecto.", example = "Proyecto para gestionar tareas de backend.")
	private String descripcion;
	
    @Schema(description = "ID del usuario propietario (Owner) del proyecto.", example = "1")
	private Long ownerId;
	
    @Schema(description = "Estatus del proyecto (1: Activo, 0: Inactivo).", example = "1")
	private Short status;
	
    @Schema(description = "Lista de tareas asociadas al proyecto.")
	private List<TareaDto> listTask;
	
	public ProjectDto(Project project) {
        // ... (Tu constructor existente se mantiene igual)
		this.idGuid=project.getIdGuid();
		this.name=project.getName();
		this.descripcion=project.getDescripcion();
		this.ownerId=(project.getOwner()!=null)?project.getOwner().getId() : null;
		this.status=project.getStatus();
		this.listTask=(project.getListTarea()!=null)?project.getListTarea().stream().map(task -> new TareaDto(task)).toList(): null;
	}
}