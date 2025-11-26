package com.springboot.app.models.services;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.springboot.app.models.dao.IRolDao;
import com.springboot.app.models.dao.IUsuarioDao;
import com.springboot.app.models.dtos.UsuarioAuthInfoDto;
import com.springboot.app.models.dtos.UsuarioDto;
import com.springboot.app.models.entities.Rol;
import com.springboot.app.models.entities.Usuario;
import com.springboot.app.utils.Constants;

import jakarta.persistence.EntityNotFoundException;

@Service
public class UsuarioServiceImpl implements IUsuarioService {

	private final IUsuarioDao usuarioDao;
	
	private final PasswordEncoder encoder;
	
	private final IRolDao rolDao;
	


	public UsuarioServiceImpl(IUsuarioDao usuarioDao, PasswordEncoder encoder, IRolDao rolDao) {		
		this.usuarioDao = usuarioDao;
		this.encoder = encoder;
		this.rolDao = rolDao;
	}



	@Override
	public Optional<UsuarioDto> findByEmailOrUsernameAndStatusIs(String email, String username) {
		
		Optional<Usuario> user= usuarioDao.findByEmailOrUsernameAndStatusIs(email.toLowerCase(), username.toLowerCase(), Constants.STATUS_ACTIVE);
		
		if (user.isEmpty()) return Optional.empty();
		
		
		
		return Optional.of(new UsuarioDto(user.get()));
	}

	@Override
	public List<UsuarioDto> findByStatusIs(Short status) {
		
		return usuarioDao.findByStatusIs(Constants.STATUS_ACTIVE).stream().map(user -> new UsuarioDto(user)).toList();
	}

	@Override
	public UsuarioDto save(UsuarioDto user) {
		
		if (user.getRoles().equals(null) || user.getRoles().isEmpty() ) {
			 throw new IllegalArgumentException("Lista de roles a asignar esta vacia");
		}
		
		List<Rol> roles=rolDao.findByIdAndStatusIs(user.getRoles(), Constants.STATUS_ACTIVE);
		
		if (roles.equals(null) || roles.isEmpty() ) {
			 throw new EntityNotFoundException("Roles no encontrados");
		}
		
		Usuario usuario= user.getId()==null 
				? new Usuario() 
						: usuarioDao.findByEmailOrUsernameAndStatusIs(user.getEmail(), user.getUsername(), Constants.STATUS_ACTIVE)
						.orElseThrow(() -> new NoSuchElementException("Usuario no encontrado"));
		
		
		usuario.setEmail(user.getEmail().toLowerCase());
		usuario.setUsername(user.getUsername().toLowerCase());
		usuario.setRoles(roles);
		usuario.setPassword(encoder.encode(user.getPassword()));
		
		Usuario userSaved = usuarioDao.save(usuario);
		userSaved.setPassword(null);
		
		return new UsuarioDto(userSaved);
	}



	@Override
	public boolean existsById(Long id) {
		
		return usuarioDao.existsByIdAndStatusIs(id,Constants.STATUS_ACTIVE);
	}



	@Override
	public Usuario findByUserId(Long userId) {
		
		return usuarioDao.findByIdAndStatus(userId,Constants.STATUS_ACTIVE).orElseThrow(() -> new NoSuchElementException("Usuario no encontrado"));
	}



	@Override
	public List<Usuario> findAllByIds(List<Long> ids) {
		
		return usuarioDao.findAllById(ids).stream().filter(user -> user.getStatus()==Constants.STATUS_ACTIVE).collect(Collectors.toList());
	}



	@Override
	public UsuarioAuthInfoDto findUserById(Long userId) {
		
		return new UsuarioAuthInfoDto(findByUserId(userId));
	}

}
