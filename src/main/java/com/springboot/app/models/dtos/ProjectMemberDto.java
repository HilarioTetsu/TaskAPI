package com.springboot.app.models.dtos;


import com.springboot.app.models.entities.ProjectMember;
import com.springboot.app.utils.ProjectRole;


import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ProjectMemberDto {

	public ProjectMemberDto(ProjectMember member) {
		this.id=member.getId();
		this.usuarioId=member.getUsuario().getId();
		this.projectId=member.getProject().getIdGuid();
		this.role=member.getRole();
		this.status=member.getStatus();
	}


	private Long id;


	@NotNull
	private Long usuarioId;


	
	private String projectId;

	@NotNull
	private ProjectRole role;


	private Short status;

}
