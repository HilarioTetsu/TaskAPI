package com.springboot.app.models.dtos;

import java.time.LocalDateTime;

import com.springboot.app.models.entities.Invitation;
import com.springboot.app.utils.ProjectRole;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InvitationDto {

	public InvitationDto(Invitation inv) {
		this.id = inv.getId();
		this.projectId = inv.getProject().getIdGuid();
		this.role = inv.getRole();
		this.status = inv.getStatus();
		this.userGuestId = inv.getGuest().getId();
		this.userHostId = inv.getHost().getId();
		this.fechaCreacion = inv.getFechaCreacion();
	}

	private String id;

	private Long userHostId;
	@NotNull
	private Long userGuestId;
	@NotNull
	private String projectId;
	@NotNull
	private ProjectRole role;

	private Short status;

	private LocalDateTime fechaCreacion;
}
