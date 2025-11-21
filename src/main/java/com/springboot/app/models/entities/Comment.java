package com.springboot.app.models.entities;

import java.time.LocalDateTime;
import java.util.List;

import com.springboot.app.utils.Constants;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.ToString;

@Entity
@Table(name = "comentarios")
@Data
@ToString
public class Comment {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@ManyToOne
	@JoinColumn(name = "tarea_id",nullable = false)
	private Tarea tarea;
	
	@ManyToOne
	@JoinColumn(name="user_id",nullable=false)
	private Usuario autor;
	
	@Lob
	@Column(columnDefinition = "TEXT")
	
	private String body;
	
	@ManyToMany
	@JoinTable(
			joinColumns = @JoinColumn(name="comment_id"),
			inverseJoinColumns = @JoinColumn(name = "user_id"),
			uniqueConstraints = @UniqueConstraint(columnNames = {"comment_id","user_id"})
			)	
	private List<Usuario> mentions;
	
	@ManyToMany
	@JoinTable(
			joinColumns = @JoinColumn(name="comment_id"),
			inverseJoinColumns = @JoinColumn(name = "media_id"),
					uniqueConstraints = @UniqueConstraint(columnNames = {"comment_id","media_id"})
			)	
	private List<Media> adjuntos;
	
	@Column(name = "fecha_creacion")
	@NotNull
	private LocalDateTime fechaCreacion;


	@Column(name = "fecha_modificacion", nullable = true)
	private LocalDateTime fechaModificacion;

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
