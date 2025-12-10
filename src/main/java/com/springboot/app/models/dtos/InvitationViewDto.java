package com.springboot.app.models.dtos;



import java.time.LocalDateTime;

import com.springboot.app.models.entities.Invitation;
import com.springboot.app.utils.ProjectRole;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InvitationViewDto {

	
	public InvitationViewDto(Invitation inv) {
		this.id=inv.getId();
		this.project=inv.getProject().getName();
		this.projectId=inv.getProject().getIdGuid();
		this.usernameGuest=inv.getGuest().getUsername();
		this.usernameHost=inv.getHost().getUsername();
		this.role=inv.getRole();
		this.fechaCreacion=inv.getFechaCreacion();
	}

	private String id;
	
	private String usernameHost;
	
	private String usernameGuest;
	
	private String project;
	
	private String projectId;
	
	private ProjectRole role;
	
	private LocalDateTime fechaCreacion;
	
}
