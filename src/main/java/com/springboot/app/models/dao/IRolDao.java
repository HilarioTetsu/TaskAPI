package com.springboot.app.models.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.NativeQuery;

import com.springboot.app.models.entities.Rol;

public interface IRolDao extends JpaRepository<Rol, Short>{


	
	@NativeQuery("SELECT * FROM roles WHERE id IN (?1) AND status=?2")
	List<Rol> findByIdAndStatusIs(List<Short> ids,Short Status);
	
}
