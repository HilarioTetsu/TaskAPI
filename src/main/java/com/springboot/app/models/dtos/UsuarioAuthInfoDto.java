package com.springboot.app.models.dtos;

import java.util.List;

import com.springboot.app.models.entities.Usuario;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UsuarioAuthInfoDto {

	public UsuarioAuthInfoDto(Usuario user) {
	this.id=user.getId();
	this.email=user.getEmail();
	this.username=user.getUsername();
	this.roles=user.getRoles().stream().map(rol -> rol.getNombre()).toList();
	}
	
	
	private Long id;
	

	private String email;
	

	private String username;
	
	private List<String> roles;
	
	
}
