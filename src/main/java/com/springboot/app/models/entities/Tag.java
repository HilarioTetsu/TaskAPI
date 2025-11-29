package com.springboot.app.models.entities;

import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.validator.constraints.Length;

import com.springboot.app.utils.Constants;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
@Table(name = "tags")
@Data
@NoArgsConstructor
public class Tag {

	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	
	@Column(name = "name",length = 30,nullable = false)
	@NotBlank
	@Length(max = 30)
	private String name;
	
	@Column(nullable = false,length = 7)
	@NotBlank
	@Length(min = 7,max = 7)
	private String color;
	
	@OneToMany(fetch = FetchType.LAZY,mappedBy = "tag")
	private List<TareaTags> ListTareaTags;
	
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
