package com.springboot.app.models.dtos;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.hibernate.validator.constraints.Length;

import com.springboot.app.models.entities.Tarea;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Schema(name = "TareaDto", description = "Objeto de transferencia de datos para Tareas.")
public class TareaDto {

    
	public TareaDto(Tarea tarea) {
        
		this.idGuid=tarea.getIdGuid();
		this.titulo=tarea.getTitulo();
		this.descripcion=tarea.getDescripcion();
		this.id_prioridad=tarea.getPrioridad().getId();
		this.id_tarea_status=tarea.getTareaStatus().getId();
		this.fechaLimite=tarea.getFechaLimite();
		this.status=tarea.getStatus();
		this.project_id=tarea.getProject()!=null ? tarea.getProject().getIdGuid() : null;
		this.listTag=tarea.getTareaTagsList()!=null&&tarea.getTareaTagsList().size()>0 
				? tarea.getTareaTagsList().stream()
						.map(tagTarea -> new TagDto(tagTarea.getTag()))
							.collect(Collectors.toList()) : null;
	}
	
    @Schema(description = "UUID de la tarea. Si es nulo en creación, se genera automáticamente.", example = "a1b2c3d4-e5f6-7890-1234-56789abcdef0")
	@Length(max = 36,min = 36)
	private String idGuid;

    @Schema(description = "Título de la tarea.", example = "Implementar Login", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotBlank
	@Length(max = 80)
	private String titulo;

    @Schema(description = "Descripción detallada de la tarea.", example = "Usar JWT y Spring Security.")
	private String descripcion;

    @Schema(description = "ID del estatus de la tarea (1=En Proceso, 4=Pendiente, etc).", example = "4", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotNull
	private Short id_tarea_status;

    @Schema(description = "ID de la prioridad (1=Baja, 2=Media, 3=Alta).", example = "3", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotNull
	private Short id_prioridad;
	
    @Schema(description = "ID del proyecto al que pertenece la tarea (opcional si es tarea personal).", example = "123e4567-e89b-12d3-a456-426614174000")
	private String project_id;
	
    @Schema(description = "Lista de etiquetas (tags) asignadas.")
	private List<TagDto> listTag;

    @Schema(description = "Fecha límite para completar la tarea.", example = "2025-12-31T23:59:00")
	@FutureOrPresent
	private LocalDateTime fechaLimite;

    @Schema(description = "Estado lógico del registro (1=Activo).", example = "1")
	@NotNull
	private Short status;
}