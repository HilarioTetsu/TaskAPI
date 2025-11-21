package com.springboot.app.models.entities;

import java.time.LocalDateTime;

import com.springboot.app.utils.Constants;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(name = "tarea_tags")
@NoArgsConstructor
public class TareaTags {
	
	
	
	
	
	public TareaTags(Tarea tarea,Tag tag) {
	this.tarea=tarea;
	this.tag=tag;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	
	@ManyToOne
	@JoinColumn(name = "id_tarea",nullable = false)
	private Tarea tarea;
	
	@ManyToOne
	@JoinColumn(name = "id_tag",nullable = false)
	private Tag tag;
	
	@Column(name = "fecha_creacion")
	@NotNull
	private LocalDateTime fechaCreacion;

	@Column(length = 50, name = "usuario_creacion")
	@NotBlank
	@Size(max = 50)
	private String usuarioCreacion;

	@Column(name = "fecha_modificacion", nullable = true)
	private LocalDateTime fechaModificacion;

	@Column(length = 50, nullable = true, name = "usuario_modificacion")
	@Size(max = 50)
	private String usuarioModificacion;

	@NotNull
	private Short status;

	@PrePersist
	public void prePersist() {
		this.fechaCreacion = LocalDateTime.now();
		this.usuarioCreacion = "admin";
		this.status = Constants.STATUS_ACTIVE;
	}

	@PreUpdate
	public void preUpdate() {
		this.usuarioModificacion = "admin";
		this.fechaModificacion = LocalDateTime.now();
	}

}
