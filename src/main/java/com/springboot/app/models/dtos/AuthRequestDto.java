package com.springboot.app.models.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(name = "AuthRequestDto", description = "Credenciales para inicio de sesión.")
public class AuthRequestDto {

    @Schema(description = "Puede ser el nombre de usuario (username) o el correo electrónico.", example = "juanperez")
	@NotNull
	private String data;	
	
    @Schema(description = "Contraseña del usuario en texto plano.", example = "12345")
	@NotNull
	private String password;
	
}