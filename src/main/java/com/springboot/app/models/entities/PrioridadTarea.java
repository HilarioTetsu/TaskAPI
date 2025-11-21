package com.springboot.app.models.entities;


import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;

@Table(name = "prioridades")
@Data
@Entity
public class PrioridadTarea {

	@Id
	private Short id;
	
	@Column(nullable = false,name="tipo_prioridad")
	private String prioridadTipo;
	
	@OneToMany(fetch = FetchType.LAZY,mappedBy = "prioridad")
	private List<Tarea> listTareas;
	
}
