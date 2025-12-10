package com.springboot.app.models.entities;


import java.util.List;

import com.springboot.app.utils.Constants;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Entity
@Table(name = "usuarios")
@Data
public class Usuario {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(nullable = false,unique = true)
	@NotBlank
	@Email
	private String email;
	
	@Column(nullable = false,length = 50,unique = true)
	@NotBlank
	private String username;
	
	@Column(nullable = false)
	@NotBlank
	private String password;
	
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "usuario_roles",
        joinColumns = @JoinColumn(name = "usuario_id"),
        inverseJoinColumns = @JoinColumn(name = "rol_id")
    )
    private List<Rol> roles;
    
    @ManyToMany(mappedBy = "usuarios")
    private List<Tarea> tareasAsignadas;
    
    @ManyToMany(mappedBy = "mentions")
    private List<Comment> comments;
    
    
    @OneToMany(mappedBy = "owner")
    private List<Tarea> listTareas;
    
    @OneToMany(mappedBy = "owner")
    private List<Project> listProject;
    
    @OneToMany(mappedBy = "usuario")
    private List<ProjectMember> projectMembers;
    
    @OneToMany(mappedBy = "host")
    private List<Invitation> hostInvitations;
    
    @OneToMany(mappedBy = "guest")
    private List<Invitation> guestInvitations;
    
	@NotNull
	@Column(nullable = false)
	private Short status;
	
	@PrePersist
	public void prePersist() {
		this.status = Constants.STATUS_ACTIVE;
	}
}
