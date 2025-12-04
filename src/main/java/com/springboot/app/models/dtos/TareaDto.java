package com.springboot.app.models.dtos;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.hibernate.validator.constraints.Length;

import com.springboot.app.models.entities.Tarea;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
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

	
	
	@Length(max = 36,min = 36)
	private String idGuid;

	@NotBlank
	@Length(max = 80)
	private String titulo;

	private String descripcion;

	@NotNull
	private Short id_tarea_status;

	@NotNull
	private Short id_prioridad;
	
	private String project_id;
	
	private List<TagDto> listTag;

	@FutureOrPresent
	private LocalDateTime fechaLimite;

	@NotNull
	private Short status;

}
