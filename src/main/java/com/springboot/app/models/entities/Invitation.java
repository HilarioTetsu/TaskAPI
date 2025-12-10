package com.springboot.app.models.entities;

import java.time.LocalDateTime;

import org.hibernate.validator.constraints.Length;

import com.springboot.app.utils.Constants;
import com.springboot.app.utils.ProjectRole;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Entity
@Table(name = "invitations")
public class Invitation {

	
	@Id
	@Column(length = 36)
	@Length(min = 36,max = 36)
	private String id;
	
	@ManyToOne
	@JoinColumn(nullable = false,name = "user_owner_id")
	private Usuario host;
	
	@ManyToOne
	@JoinColumn(nullable = false,name = "user_guest_id")
	private Usuario guest;
	
	@ManyToOne
	@JoinColumn(nullable = false,name = "project_id")
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


	@Column(name = "fecha_modificacion", nullable = true)
	private LocalDateTime fechaModificacion;
	
	@PrePersist
	public void prePersist() {
		this.fechaCreacion = LocalDateTime.now();		
		this.status = Constants.STATUS_PENDING;
	}

	@PreUpdate
	public void preUpdate() {		
		this.fechaModificacion = LocalDateTime.now();
	}
	
}
