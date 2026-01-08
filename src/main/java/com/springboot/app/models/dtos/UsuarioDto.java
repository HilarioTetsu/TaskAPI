package com.springboot.app.models.dtos;

import java.util.List;

import org.hibernate.validator.constraints.Length;

import com.springboot.app.models.entities.Usuario;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "UsuarioDto", description = "Información de registro o perfil de usuario.")
public class UsuarioDto {

    
	public UsuarioDto(Usuario user) {
        // ...
		this.id=user.getId();
		this.email=user.getEmail();
		this.username=user.getUsername();
		this.roles=user.getRoles().stream().map(rol -> rol.getId()).toList();
	}
	
    @Schema(description = "ID interno del usuario.", example = "1")
	private Long id;
	
    @Schema(description = "Correo electrónico único.", example = "usuario@mail.com")
	@NotBlank
	@Email
	private String email;
	
    @Schema(description = "Nombre de usuario único.", example = "usuario123")
	@NotBlank
	@Length(max = 50)
	private String username;
	
    @Schema(description = "Contraseña (solo requerida al crear/actualizar). No se devuelve en consultas.", example = "passwordSeguro123")
	private String password;
		
    @Schema(description = "Lista de IDs de roles asignados.", example = "[1, 2]")
	private List<Short> roles;
}