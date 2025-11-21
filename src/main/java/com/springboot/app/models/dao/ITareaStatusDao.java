package com.springboot.app.models.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.springboot.app.models.entities.TareaStatus;

public interface ITareaStatusDao extends JpaRepository<TareaStatus, Short>{

}
