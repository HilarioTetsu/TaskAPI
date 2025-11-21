package com.springboot.app.models.dtos;

import java.time.LocalDateTime;

import org.hibernate.validator.constraints.Length;

import com.springboot.app.models.entities.Tarea;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import lombok.Data;
import lombok.NoArgsConstructor;

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
	}

	
	@Null
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

	@FutureOrPresent
	private LocalDateTime fechaLimite;

	@NotNull
	private Short status;

}
