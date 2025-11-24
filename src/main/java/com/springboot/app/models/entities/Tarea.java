package com.springboot.app.models.entities;

import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.validator.constraints.Length;

import com.springboot.app.models.dtos.TareaDto;
import com.springboot.app.utils.Constants;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Entity
@Table(name = "tareas")
@NoArgsConstructor
@ToString
public class Tarea {

	public Tarea(TareaDto dto,TareaStatus tareaStatus,PrioridadTarea prioridadStatus) {
		
		this.idGuid=dto.getIdGuid();
		this.titulo=dto.getTitulo();
		this.descripcion=dto.getDescripcion();
		this.fechaLimite=dto.getFechaLimite();
		this.prioridad=prioridadStatus;
		this.tareaStatus=tareaStatus;
		this.status=dto.getStatus();
		
		
	}
	
	public Tarea(String GUID) {
		
		this.idGuid=GUID;			
	}

	@Id	
	@Column(length = 36)
	@Length(min = 36,max = 36)
	private String idGuid;

	@Column(name = "titulo", length = 80, nullable = false)
	@NotBlank
	private String titulo;

	@Lob
	@Column(columnDefinition = "TEXT")
	private String descripcion;

	@ManyToOne
	@JoinColumn(name = "id_tarea_status", nullable = false)
	private TareaStatus tareaStatus;

	@ManyToOne
	@JoinColumn(name = "id_prioridad", nullable = false)
	private PrioridadTarea prioridad;
	
	@ManyToOne
	@JoinColumn(name = "owner_id",nullable = false)
	private Usuario owner;
	
	@ManyToOne
	@JoinColumn(name = "project_id")
	private Project project;

	@FutureOrPresent
	@Column(name = "fecha_limite", nullable = true)
	private LocalDateTime fechaLimite;
	
	@OneToMany(fetch = FetchType.LAZY,mappedBy = "tarea")
	private List<TareaTags> tareaTagsList;
	
	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(
	name = "tareas_usuario",
	joinColumns = @JoinColumn(name="tarea_id"),
	inverseJoinColumns = @JoinColumn(name="usuario_id"),
	
	uniqueConstraints = @UniqueConstraint(columnNames = {"tarea_id","usuario_id"})
	)
	private List<Usuario> usuarios;

	@Column(name = "fecha_creacion")
	@NotNull
	private LocalDateTime fechaCreacion;


	@Column(name = "fecha_modificacion", nullable = true)
	private LocalDateTime fechaModificacion;

	@Column(length = 50, nullable = true, name = "usuario_modificacion")
	@Size(max = 50)
	private String usuarioModificacion;

	@NotNull
	@Column(nullable = false)
	private Short status;

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
