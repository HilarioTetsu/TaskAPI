package com.springboot.app.models.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.springboot.app.models.entities.PrioridadTarea;

public interface IPrioridadTareaDao extends JpaRepository<PrioridadTarea, Short>{

}
