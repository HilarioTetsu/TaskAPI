package com.springboot.app.models.dtos;

import com.springboot.app.models.entities.ProjectMember;
import com.springboot.app.utils.ProjectRole;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Schema(name = "ProjectMemberDto", description = "Representa a un usuario miembro de un proyecto, con su rol y estatus dentro de ese proyecto.")
public class ProjectMemberDto {

	public ProjectMemberDto(ProjectMember member) {
		if (member != null) {
			this.id = member.getId();
			this.usuarioId = member.getUsuario() != null ? member.getUsuario().getId() : null;
			this.projectId = member.getProject() != null ? member.getProject().getIdGuid() : null;
			this.role = member.getRole();
			this.status = member.getStatus();
			this.username = member.getUsuario() != null ? member.getUsuario().getUsername() : null;
		} else {
			this.id = null;
			this.usuarioId = null;
			this.projectId = null;
			this.role = null;
			this.status = null;
			this.username = null;
		}
	}

	@Schema(description = "ID interno del registro de miembro de proyecto.", example = "100")
	private Long id;

	@NotNull
	@Schema(description = "ID del usuario que participa en el proyecto.", example = "15", requiredMode = Schema.RequiredMode.REQUIRED)
	private Long usuarioId;

	@Schema(description = "Nombre de usuario asociado al miembro (denormalizado para lecturas rápidas).", example = "team.master")
	private String username;

	@Schema(description = "ID GUID del proyecto en el que participa el usuario.", example = "c6dfb42d-ef83-4531-9d20-0d20e61f5b8b")
	private String projectId;

	@NotNull
	@Schema(description = "Rol del usuario dentro del proyecto (OWNER, MEMBER, VIEWER, etc.).", example = "MEMBER", requiredMode = Schema.RequiredMode.REQUIRED)
	private ProjectRole role;

	@Schema(description = "Estatus del vínculo usuario-proyecto (1 = activo, 0 = inactivo/baja lógica).", example = "1")
	private Short status;

}
