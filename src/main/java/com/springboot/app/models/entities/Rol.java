package com.springboot.app.models.entities;

import java.util.List;

import com.springboot.app.utils.Constants;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Entity
@Table(name = "roles")
@Data
public class Rol {

	@Id	
	private Short id;
	
	@Column(nullable = false)
	private String nombre;
	
	@NotNull
	@Column(nullable = false)
	private Short status;
	
	@ManyToMany(mappedBy = "roles", fetch = FetchType.LAZY)
	private List<Usuario> usuarios;
	
	@PrePersist
	public void prePersist() {
		this.status = Constants.STATUS_ACTIVE;
	}
	
}
