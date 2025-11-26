package com.springboot.app.models.services;

import java.util.List;
import java.util.Optional;

import com.springboot.app.models.dtos.UsuarioAuthInfoDto;
import com.springboot.app.models.dtos.UsuarioDto;
import com.springboot.app.models.entities.Usuario;

public interface IUsuarioService {
	
	Optional<UsuarioDto> findByEmailOrUsernameAndStatusIs(String email,String username);
	
	List<UsuarioDto> findByStatusIs(Short status);
	
	UsuarioDto save(UsuarioDto user);

	boolean existsById(Long id);

	Usuario findByUserId(Long userId);
	
	List<Usuario> findAllByIds(List<Long> ids);

	UsuarioAuthInfoDto findUserById(Long userId);
	
	
}
