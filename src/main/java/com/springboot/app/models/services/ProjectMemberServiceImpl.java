package com.springboot.app.models.services;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.springboot.app.models.dao.IProjectDao;
import com.springboot.app.models.dao.IProjectMemberDao;
import com.springboot.app.models.dao.ITareaDao;
import com.springboot.app.models.dtos.ProjectMemberDto;
import com.springboot.app.models.entities.ProjectMember;
import com.springboot.app.models.entities.Tarea;
import com.springboot.app.models.entities.Usuario;
import com.springboot.app.utils.Constants;
import com.springboot.app.utils.CustomUserDetails;
import com.springboot.app.utils.ProjectRole;

import jakarta.validation.Valid;

@Service
public class ProjectMemberServiceImpl implements IProjectMemberService {

	private final IProjectMemberDao projectMemberDao;
	
	private final IProjectDao projectDao;
	
	private final IUsuarioService usuarioService;

	private final ITareaDao tareaDao;





	public ProjectMemberServiceImpl(IProjectMemberDao projectMemberDao, IProjectDao projectDao,
			IUsuarioService usuarioService, ITareaDao tareaDao) {
		super();
		this.projectMemberDao = projectMemberDao;
		this.projectDao = projectDao;
		this.usuarioService = usuarioService;
		this.tareaDao = tareaDao;
	}

	@Override
	@Transactional(readOnly = true)
	public boolean isOwner(Long userId, String projectId) {
		
		return projectMemberDao.existsByUsuarioIdAndProjectIdGuidAndRoleIsAndStatusIs(userId,projectId,ProjectRole.OWNER,Constants.STATUS_ACTIVE);
	}

	@Override
	@Transactional(readOnly = true)
	public boolean isMember(Long usuarioId, String projectId) {
		
		return projectMemberDao.existsByUsuarioIdAndProjectIdGuid(usuarioId, projectId);
	}
	
	
	@Override
	@Transactional(readOnly = true)
	public boolean isMemberActive(Long usuarioId, String projectId) {
		
		return projectMemberDao.existsByUsuarioIdAndProjectIdGuidAndStatusIs(usuarioId, projectId,Constants.STATUS_ACTIVE);
	}
	

	@Override
	@Transactional
	public ProjectMemberDto save(String projectId,@Valid ProjectMemberDto dto, CustomUserDetails authUser) {
		
		ProjectMember member;
		
		boolean isUpdate=false;
		
		if (!ProjectRole.existeRol(dto.getRole().toString())) {
			throw new IllegalArgumentException("Rol invalido");
		}

		if (!isOwner(authUser.getUserId(), projectId)) {

		throw new SecurityException("No tiene los permisos necesario en este proyecto");
		}

		if (!usuarioService.existsById(dto.getUsuarioId())) {
			throw new NoSuchElementException("Usuario no encontrado");
		}

		if (isMember(dto.getUsuarioId(), projectId)) {
			throw new IllegalStateException("Miembro ya registrado en el proyecto");
		}
		
		
		
		
		if (dto.getId()!=null) {
			member=projectMemberDao.findById(dto.getId()).orElseThrow(
					() -> new NoSuchElementException("Miembro inexistente"));
			
			member.setStatus(dto.getStatus());
			
			isUpdate=true;
			
			
		}else {
			
			member=new ProjectMember();
			
		}
		
		member.setProject(projectDao.findByIdGuidAndStatusIs(projectId,Constants.STATUS_ACTIVE).orElseThrow(
				() -> new NoSuchElementException("Projecto no encontrado")
				));
		
		member.setUsuario(usuarioService.findByUserId(dto.getUsuarioId()));
		member.setRole(dto.getRole());
		
		member.setUsuarioCreacion(authUser.getUsername());
		
		
		
		
		
		return new ProjectMemberDto(projectMemberDao.save(member));
	}

	@Override
	@Transactional
	public ProjectMember save(ProjectMember member) {
		
		return projectMemberDao.save(member);
	}


	@Override
	@Transactional(readOnly = true)
	public boolean validationOwnerAndMemberProject(ProjectMemberDto dto, Long userId, Long authUserId, String projectId) {
		
		int countOwners=getCountOwners(projectId);
		
		if (!ProjectRole.existeRol(dto.getRole().toString())) {
			throw new IllegalArgumentException("Rol no valido");
		}

		if (!isOwner(authUserId, projectId)) {

			throw new SecurityException("No tiene los permisos necesario en este proyecto");
		}

		if (!usuarioService.existsById(userId)) {
			throw new NoSuchElementException("Usuario no encontrado");
		}

	
		if (countOwners==1 && userId==authUserId) {
			
			if (dto.getRole()!=ProjectRole.OWNER) {
				throw new SecurityException("Accion no permitida al ser el unico Owner del proyecto");
			}
			
			if (dto.getStatus()==Constants.STATUS_INACTIVE) {
				throw new SecurityException("Accion no permitida al ser el unico Owner del proyecto");
			}
			
		}
		
		
		return true;
	}

	@Override
	@Transactional(readOnly = true)
	public int getCountOwners(String projectId) {
		
		return projectMemberDao.getCountOwners(projectId);
	}

	@Override
	@Transactional(readOnly = true)
	public ProjectMember findByUsuarioIdAndProjectIdGuidAndStatusIs(Long usuarioId, String projectId,
			Short statusActive) {
		
		return projectMemberDao.findByUsuarioIdAndProjectIdGuidAndStatusIs(usuarioId, projectId, statusActive).orElseThrow(
				() -> new NoSuchElementException("Miembro no encontrado")
				);
	}

	@Override
	@Transactional(readOnly = true)
	public ProjectMember findByUsuarioIdAndProjectIdGuid(Long userId, String projectId) {
		
		return projectMemberDao.findByUsuarioIdAndProjectIdGuid(userId,projectId).orElseThrow(
				() -> new NoSuchElementException("Miembro no encontrado")
				);
	}

	@Override
	@Transactional(readOnly = true)
	public List<ProjectMemberDto> findProjectMembersByProjectId(Long userId, String projectId) {
		
		if (!projectDao.existsByIdGuidAndStatusIs(projectId, Constants.STATUS_ACTIVE)) {
			throw new NoSuchElementException("Projecto no encontrado o inactivo");
		}
		
		if (!isMember(userId, projectId)) {
			throw new SecurityException("No tiene los permisos necesario en este proyecto");
		}
		
		return projectMemberDao.findByProjectIdGuid(projectId).stream().map(
				member -> new ProjectMemberDto(member)
				).toList();
				
	}

	@Override
	@Transactional(readOnly = true)
	public boolean canEditTasks(Long userId, String projectId) {
	
		return projectMemberDao.existsByUsuarioAndProjectWithRoles(userId, projectId, Arrays.asList(ProjectRole.OWNER,ProjectRole.EDITOR), Constants.STATUS_ACTIVE);
	}

	@Override
	@Transactional
	public List<ProjectMember> saveAll(List<ProjectMember> members) {
		
		return projectMemberDao.saveAll(members);
	}

	@Override
	@Transactional
	public void deleteProjectMember(Long authUserId, String projectId, Long userId) {
		
		if (!isOwner(authUserId, projectId)) {
			throw new AccessDeniedException("No tiene los permisos necesario en este proyecto");
		}
		
		if (!isMemberActive(userId, projectId)) {
			throw new IllegalStateException("El usuario a eliminar no es miembro del proyecto");
		}
		
		
		int countOwners = getCountOwners(projectId);
		
		
		if (countOwners==1 && userId==authUserId) {
					
			throw new IllegalStateException("No se permite la accion al ser el unico owner del proyecto");
		}
		
		 ProjectMember member= findByUsuarioIdAndProjectIdGuid(userId, projectId);
		
		 List<Tarea> taskAssigned =member.getProject().getListTarea().stream().filter(t -> t.getUsuarios().stream().anyMatch(u -> u.getId()==userId)).collect(Collectors.toList()) ;
		 
		 for (Tarea tarea : taskAssigned) {
			
			 List<Usuario> asigned = tarea.getUsuarios();
			 
			 asigned.removeIf(u -> u.getId()==userId);
			 
			 tarea.setUsuarios(asigned);
			 
		}
		 
		 
		 tareaDao.saveAll(taskAssigned);
		 
		 member.setStatus(Constants.STATUS_INACTIVE);
		 
		 save(member);
		 
		 
	}

	
	
	
}
