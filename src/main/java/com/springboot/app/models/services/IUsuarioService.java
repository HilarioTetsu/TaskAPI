package com.springboot.app.models.services;

import java.util.List;

import com.springboot.app.models.dtos.UsuarioAuthInfoDto;
import com.springboot.app.models.dtos.UsuarioDto;
import com.springboot.app.models.dtos.UsuarioUpdateDto;
import com.springboot.app.models.entities.Usuario;

public interface IUsuarioService {
	
	UsuarioDto findByEmailOrUsernameAndStatusIs(String email,String username);
	
	List<UsuarioDto> findByStatusIs(Short status);
	
	UsuarioDto save(UsuarioDto user);

	boolean existsById(Long id);

	Usuario findByUserId(Long userId);
	
	List<Usuario> findAllByIds(List<Long> ids);

	UsuarioAuthInfoDto findUserById(Long userId);

	List<UsuarioAuthInfoDto> findByUsernameContainingAndProjectId(String term, String projectId, Long userId);
	
	List<UsuarioAuthInfoDto> findByUsernameContainingOrEmailContaining(String term, String projectId, Long authUserId);

	UsuarioAuthInfoDto updateUserInfo(UsuarioUpdateDto dto, Long userId);

	String findUsernameById(Long userAuthId);
	
	
}
