package com.springboot.app.models.entities;

import java.time.LocalDateTime;

import com.springboot.app.utils.Constants;
import com.springboot.app.utils.ProjectRole;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "project_members",
uniqueConstraints = @UniqueConstraint(columnNames = {"project_id", "usuario_id"})
)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProjectMember {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@ManyToOne
	@JoinColumn(name = "usuario_id",nullable = false)
	@NotNull
	private Usuario usuario;
	
	@ManyToOne
	@JoinColumn(name = "project_id",nullable = false)
	@NotNull
	private Project project;
	
    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    @NotNull
	private ProjectRole role;
	
	@NotNull
	@Column(nullable = false)
	private Short status;
	
	@Column(name = "fecha_creacion")
	@NotNull
	private LocalDateTime fechaCreacion;

	@Column(length = 50, nullable = true, name = "usuario_creacion")
	@Size(max = 50)
	private String usuarioCreacion;
	
	
	@Column(name = "fecha_modificacion", nullable = true)
	private LocalDateTime fechaModificacion;

	@Column(length = 50, nullable = true, name = "usuario_modificacion")
	@Size(max = 50)
	private String usuarioModificacion;
	
	
	@PrePersist
	public void prePersist() {
		this.fechaCreacion = LocalDateTime.now();		
		this.status = Constants.STATUS_ACTIVE;
	}

	@PreUpdate
	public void preUpdate() {		
		this.fechaModificacion = LocalDateTime.now();
	}

	
}
