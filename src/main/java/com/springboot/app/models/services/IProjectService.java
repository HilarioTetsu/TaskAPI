package com.springboot.app.models.services;

import java.util.List;
import java.util.Map;
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

	void deleteProject(String projectId, Long userId);

	int getProjectCountRoleOwner(Long userId);
	
	int getProjectCountActive(Long userId);
	
	public Map<Short, String> getAllStatuses();
	
	 public String getStatusByKey(short statusKey);

	 Map<String, List<ProjectDto>> findProjectsById(Long userId);
	 
	 List<ProjectDto> findProjectsLikeMemberByUserId(Long userId);
	

}
