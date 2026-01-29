package com.springboot.app.models.dtos;

import java.time.LocalDateTime;

import com.springboot.app.models.entities.Invitation;
import com.springboot.app.utils.ProjectRole;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(
    name = "InvitationDto",
    description = "Representa una invitación de un usuario hacia otro para colaborar en un proyecto con un rol específico."
)
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

    @Schema(
        description = "Identificador único de la invitación.",
        example = "c6dfb42d-ef83-4531-9d20-0d20e61f5b8b"
    )
    private String id;

    @Schema(
        description = "ID del usuario que envía la invitación (host). Se resuelve normalmente desde el usuario autenticado.",
        example = "10"
    )
    private Long userHostId;

    @NotNull
    @Schema(
        description = "ID del usuario invitado (guest) que recibirá la invitación.",
        example = "25",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Long userGuestId;

    @NotNull
    @Schema(
        description = "ID GUID del proyecto al que se invita al usuario.",
        example = "c6dfb42d-ef83-4531-9d20-0d20e61f5b8b",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String projectId;

    @NotNull
    @Schema(
        description = "Rol que tendrá el usuario invitado dentro del proyecto (OWNER, MEMBER, VIEWER, etc.).",
        example = "MEMBER",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private ProjectRole role;

    @Schema(
        description = "Estatus actual de la invitación (por ejemplo, pendiente, aceptada, rechazada).",
        example = "1"
    )
    private Short status;

    @Schema(
        description = "Fecha y hora en que se creó la invitación.",
        example = "2025-12-01T10:15:30"
    )
    private LocalDateTime fechaCreacion;
}
