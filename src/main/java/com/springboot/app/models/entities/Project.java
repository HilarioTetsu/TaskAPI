package com.springboot.app.models.entities;

import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.validator.constraints.Length;

import com.springboot.app.utils.Constants;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "project")
@Data
@NoArgsConstructor
public class Project {

	
	public Project(String GUID) {
		
		this.idGuid=GUID;			
	}
	
	@Id
	@Column(length = 36)
	@Length(min = 36,max = 36)
	private String idGuid;
	
	@Column(length = 100,nullable = false)
	@Length(max = 100)
	@NotBlank
	private String name;
	
	@Lob
	@Column(columnDefinition = "TEXT")
	private String descripcion;
	
	@ManyToOne
	@JoinColumn(name = "owner_id",nullable = true)
	private Usuario owner;
	
    @OneToMany(mappedBy = "project")
    private List<Tarea> listTarea;
    
    @OneToMany(mappedBy = "project")
    private List<ProjectMember> projectMember;
	
	@NotNull
	@Column(nullable = false)
	private Short status;
	
	@Column(name = "fecha_creacion")
	@NotNull
	private LocalDateTime fechaCreacion;


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
