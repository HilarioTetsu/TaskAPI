package com.springboot.app.models.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.springboot.app.models.dao.IUsuarioDao;
import com.springboot.app.models.entities.Usuario;
import com.springboot.app.utils.Constants;
import com.springboot.app.utils.CustomUserDetails;

@Service
public class UsuarioDetailsService implements UserDetailsService{

    @Autowired
    private IUsuarioDao repo;

	@Override
	public UserDetails loadUserByUsername(String data) throws UsernameNotFoundException {
		
		Usuario usuario = repo.findByEmailOrUsernameAndStatusIs(data, data, Constants.STATUS_ACTIVE)
				.orElseThrow(() -> new UsernameNotFoundException("No encontrado"));
		
		
		List<GrantedAuthority> authorities=usuario.getRoles()
				.stream()
				.map(rol -> new SimpleGrantedAuthority("ROLE_"+rol.getNombre() ))
				.collect(Collectors.toList());
		
		return new CustomUserDetails(usuario.getEmail(), usuario.getPassword(), authorities,usuario.getId());
	}


}
