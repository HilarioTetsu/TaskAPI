package com.springboot.app.models.dtos;

import com.springboot.app.models.entities.PrioridadTarea;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrioridadTareaDto {

	
	public PrioridadTareaDto(PrioridadTarea p) {
		this.id=p.getId();
		this.prioridadTipo=p.getPrioridadTipo();
	}


	private Short id;
	
	
	private String prioridadTipo;
	
	
}
