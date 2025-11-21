package com.springboot.app.models.dtos;

import java.util.List;

import org.hibernate.validator.constraints.Length;

import com.springboot.app.models.entities.Usuario;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UsuarioDto {

	
	public UsuarioDto(Usuario user) {
	this.id=user.getId();
	this.email=user.getEmail();
	this.username=user.getUsername();
	this.roles=user.getRoles().stream().map(rol -> rol.getId()).toList();
	}
	
	
	private Long id;
	
	@NotBlank
	@Email
	private String email;
	
	@NotBlank
	@Length(max = 50)
	private String username;
	
	private String password;
		
	private List<Short> roles;
	
}
