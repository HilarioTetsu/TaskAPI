package com.springboot.app.models.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "UsuarioUpdateDto", description = "Datos para actualizar el perfil del usuario autenticado.")
public class UsuarioUpdateDto {

	@Schema(description = "Nuevo correo electrónico (debe ser único).", example = "nuevo.email@example.com")
	private String email;

	@Schema(description = "Nuevo nombre de usuario (debe ser único).", example = "nuevoUser123")
	private String username;

	@Schema(description = "Contraseña ACTUAL del usuario. Requerida para confirmar cambios sensibles.", example = "passwordActual123", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotBlank
	private String password;

	@Schema(description = "Nueva contraseña. Si se envía, se actualizará.", example = "NuevaPasswordSegura@1")
	private String newPassword;
}