package com.springboot.app.models.services;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.springboot.app.models.dao.IProjectDao;
import com.springboot.app.models.dao.ITareaDao;

@Service
@Transactional(readOnly = true)
public class StatsService {

	private final ITareaDao tareaDao;
	
	private final IProjectDao projectDao;

	public StatsService(ITareaDao tareaDao, IProjectDao projectDao) {
		super();
		this.tareaDao = tareaDao;
		this.projectDao = projectDao;
	}
	
	
	public List<Object[]> countTareasByPrioridad(Long userId) {

		return tareaDao.countTareasByPrioridad(userId);
	}

	
	public List<Object[]> countTareasByTareaStatus(Long userId) {

		return tareaDao.countTareasByTareaStatus(userId);
	}


	public int getTareasHoyCountByUserId(Long userId) {

		return tareaDao.getTareasHoyCountByUserId(userId);
	}


	public int getTareasVencidasCountByUserId(Long userId) {

		return tareaDao.getTareasVencidasCountByUserId(userId);
	}


	public int getTareasPendientesCountByUserId(Long userId) {

		return tareaDao.getTareasPendientesCountByUserId(userId);
	}
	
	
	public int getProjectCountRoleOwner(Long userId) {
		
		return projectDao.getProjectCountRoleOwner(userId);
	}

	
	public int getProjectCountActive(Long userId) {
		
		return projectDao.getProjectCountActive(userId);
	}

	
}
