package com.springboot.app.models.entities;

import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;

@Table(name = "tarea_status")
@Entity
@Data
public class TareaStatus {

	@Id
	private Short id;
	
	@Column(nullable = false,name = "status")
	private String status;
	
	@OneToMany(fetch = FetchType.LAZY,mappedBy = "tareaStatus")
	private List<Tarea> listTareas;
	
}
