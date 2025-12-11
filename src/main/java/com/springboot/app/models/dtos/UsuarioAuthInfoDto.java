package com.springboot.app.models.dtos;

import java.util.List;
import com.springboot.app.models.entities.Usuario;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "UsuarioAuthInfoDto", description = "Información pública/básica del usuario para mostrar en el frontend.")
public class UsuarioAuthInfoDto {

	public UsuarioAuthInfoDto(Usuario user) {
		this.id = user.getId();
		this.email = user.getEmail();
		this.username = user.getUsername();
		this.roles = user.getRoles().stream().map(rol -> rol.getNombre()).toList();
	}

	@Schema(description = "ID del usuario.", example = "10")
	private Long id;

	@Schema(description = "Correo electrónico.", example = "juan@example.com")
	private String email;

	@Schema(description = "Nombre de usuario.", example = "juanperez")
	private String username;

	@Schema(description = "Roles asignados al usuario (ej. ADMIN, BASICO).", example = "[\"ADMIN\", \"BASICO\"]")
	private List<String> roles;
}