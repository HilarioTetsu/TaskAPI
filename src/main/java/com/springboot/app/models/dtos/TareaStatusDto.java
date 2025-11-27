package com.springboot.app.models.dtos;


import com.springboot.app.models.entities.TareaStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TareaStatusDto {

	
	public TareaStatusDto(TareaStatus tareaStatus) {
		
		this.id=tareaStatus.getId();
		this.status=tareaStatus.getStatus();
		
	}
	
	private Short id;
	
	
	private String status;
	
	
}
