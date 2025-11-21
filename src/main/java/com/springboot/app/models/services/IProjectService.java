package com.springboot.app.models.services;

import java.util.List;
import java.util.Optional;

import com.springboot.app.models.dtos.ProjectDto;
import com.springboot.app.models.dtos.TareaDto;
import com.springboot.app.models.entities.Project;

public interface IProjectService {

	List<ProjectDto> findByOwnerId(Long userId);
	
	ProjectDto save(ProjectDto dto,Long userId);

	ProjectDto findByProjectIdAndUserId(String id, Long userId);

	List<TareaDto> findTasksByProjectId(String id, Long userId);
	
	boolean existsProjectActive(String id);

	Optional<Project> findByProjectId(String projectId);

	void deleteTarea(String projectId, Long userId);

	

}
