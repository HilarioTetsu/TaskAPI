package com.springboot.app.models.services;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.springboot.app.models.dao.IPrioridadTareaDao;
import com.springboot.app.models.dao.ITareaStatusDao;
import com.springboot.app.models.dtos.PrioridadTareaDto;
import com.springboot.app.models.dtos.TareaStatusDto;
import com.springboot.app.models.entities.PrioridadTarea;
import com.springboot.app.models.entities.TareaStatus;

@Service
@Transactional(readOnly = true)
public class CatalogoService {

	private final IPrioridadTareaDao prioridadDao;

	private final ITareaStatusDao tareaStatusDao;

	public CatalogoService(IPrioridadTareaDao prioridadDao, ITareaStatusDao tareaStatusDao) {
		super();
		this.prioridadDao = prioridadDao;
		this.tareaStatusDao = tareaStatusDao;
	}
	
	
	
	public List<PrioridadTareaDto> findAllPrioridadesTarea() {

		return prioridadDao.findAll().stream().map(p -> new PrioridadTareaDto(p)).toList();
	}
	
	public List<TareaStatusDto> findAllTareaStatus() {
		return tareaStatusDao.findAll().stream().map(ts -> new TareaStatusDto(ts)).toList();
	}
	
	public Optional<PrioridadTarea> findPrioridadTareaById(Short id){
		return prioridadDao.findById(id);
	}
	
	public Optional<TareaStatus> findTareaStatusById(Short id){
		return tareaStatusDao.findById(id);
	}
	
}
