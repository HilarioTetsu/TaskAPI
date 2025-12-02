package com.springboot.app.models.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AuthRequestDto {

	@NotNull
	private String data;	
	
	@NotNull
	private String password;
	
}
