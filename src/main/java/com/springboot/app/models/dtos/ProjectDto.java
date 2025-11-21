package com.springboot.app.models.dtos;

import java.util.List;

import org.hibernate.validator.constraints.Length;

import com.springboot.app.models.entities.Project;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ProjectDto {

	@Length(min = 36,max = 36)
	private String idGuid;
	
	
	@Length(max = 100)
	@NotBlank
	private String name;
	
	private String descripcion;
	
	private Long ownerId;
	
	private Short status;
	
	
	private List<TareaDto> listTask;
	
	
	public ProjectDto(Project project) {
		this.idGuid=project.getIdGuid();
		this.name=project.getName();
		this.descripcion=project.getDescripcion();
		this.ownerId=(project.getOwner()!=null)?project.getOwner().getId() : null;
		this.status=project.getStatus();
		this.listTask=(project.getListTarea()!=null)?project.getListTarea().stream().map(task -> new TareaDto(task)).toList(): null;
		
	}
}
