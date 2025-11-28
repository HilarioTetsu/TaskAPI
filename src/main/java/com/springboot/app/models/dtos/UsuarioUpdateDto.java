package com.springboot.app.models.dtos;


import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UsuarioUpdateDto {

	
	
	
	private String email;
	
	private String username;
	
	@NotBlank
	private String password;
	
	private String newPassword;
	
	
	
}
