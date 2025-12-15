package com.springboot.app.models.services;





import java.util.List;
import java.util.Optional;

import com.springboot.app.models.dtos.ProjectMemberDto;
import com.springboot.app.models.entities.ProjectMember;
import com.springboot.app.utils.CustomUserDetails;

import jakarta.validation.Valid;

public interface IProjectMemberService {

	boolean isOwner(Long userId, String projectId);
	
	boolean canEditTasks(Long userId, String projectId);

	boolean isMember(Long usuarioId, String projectId);

	ProjectMemberDto save(String projectId, @Valid ProjectMemberDto dto, Long authUserId);

	ProjectMember save(ProjectMember member);


	boolean validationOwnerAndMemberProject(ProjectMemberDto dto, Long userId, Long userId2, String projectId);
	
	
	ProjectMember findByUsuarioIdAndProjectIdGuidAndStatusIs(Long usuarioId, String projectId, Short statusActive);

	ProjectMember findByUsuarioIdAndProjectIdGuid(Long userId, String projectId);
	
	
	Optional<ProjectMember> findByUsuarioIdAndProjectId(Long userId, String projectId);

	List<ProjectMemberDto> findProjectMembersByProjectId(Long userId, String projectId);
	
	int getCountOwners(String projectId);

	List<ProjectMember> saveAll(List<ProjectMember> members);

	void deleteProjectMember(Long authUserId, String projectId, Long userId);

	boolean isMemberActive(Long usuarioId, String projectId);
}
