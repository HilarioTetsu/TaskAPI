package com.springboot.app.models.dtos;

import java.time.LocalDateTime;
import com.springboot.app.models.entities.Invitation;
import com.springboot.app.utils.ProjectRole;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "InvitationViewDto", description = "Vista de lectura de una invitación para listados.")
public class InvitationViewDto {

  
	public InvitationViewDto(Invitation inv) {
        // ...
		this.id=inv.getId();
		this.project=inv.getProject().getName();
		this.projectId=inv.getProject().getIdGuid();
		this.usernameGuest=inv.getGuest().getUsername();
		this.usernameHost=inv.getHost().getUsername();
		this.role=inv.getRole();
		this.fechaCreacion=inv.getFechaCreacion();
	}

    @Schema(description = "ID de la invitación.", example = "inv-uuid-1234")
	private String id;
	
    @Schema(description = "Username de quien envía la invitación.", example = "admin_user")
	private String usernameHost;
	
    @Schema(description = "Username del invitado.", example = "new_member")
	private String usernameGuest;
	
    @Schema(description = "Nombre del proyecto.", example = "Proyecto Alpha")
	private String project;
	
    @Schema(description = "ID del proyecto.", example = "proj-uuid-5678")
	private String projectId;
	
    @Schema(description = "Rol propuesto en la invitación.", example = "EDITOR")
	private ProjectRole role;
	
    @Schema(description = "Fecha de envío.", example = "2025-10-20T10:00:00")
	private LocalDateTime fechaCreacion;
	
}