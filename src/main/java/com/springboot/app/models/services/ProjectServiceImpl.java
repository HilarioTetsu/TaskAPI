package com.springboot.app.models.services;


import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.springboot.app.models.dao.IProjectDao;
import com.springboot.app.models.dao.ITareaDao;
import com.springboot.app.models.dao.IUsuarioDao;
import com.springboot.app.models.dtos.ProjectDto;
import com.springboot.app.models.dtos.TareaDto;
import com.springboot.app.models.entities.Project;
import com.springboot.app.models.entities.ProjectMember;
import com.springboot.app.models.entities.Tarea;
import com.springboot.app.models.entities.Usuario;
import com.springboot.app.utils.Constants;
import com.springboot.app.utils.ProjectRole;

@Service
public class ProjectServiceImpl implements IProjectService {

	private final IProjectDao projectDao;
	
	private final IUsuarioDao usuarioDao;
	
	private final IProjectMemberService projectMemberService;

	private final ITareaDao tareaDao;






	public ProjectServiceImpl(IProjectDao projectDao, IUsuarioDao usuarioDao,
			IProjectMemberService projectMemberService, ITareaDao tareaDao) {
		super();
		this.projectDao = projectDao;
		this.usuarioDao = usuarioDao;
		this.projectMemberService = projectMemberService;
		this.tareaDao = tareaDao;
	}

	@Override
	public List<ProjectDto> findByOwnerId(Long userId) {
		
		
		
		return projectDao.findByOwnerId(userId,Constants.STATUS_ACTIVE)
				.stream()
				.map
				(project -> new ProjectDto(project))
				.toList();
	}

	
    public Map<Short, String> getAllStatuses() {
        return Map.of(
            Constants.STATUS_INACTIVE, "INACTIVO",
            Constants.STATUS_ACTIVE, "ACTIVO"
        );
    }
    
    
    public String getStatusByKey(short statusKey) {
        String status = getAllStatuses().get(statusKey);
        if (status == null) {
            throw new NoSuchElementException(
                String.format("No existe un estatus de proyecto con la clave: %d", statusKey)
            );
        }
        return status;
    }
	
	@Override
	public ProjectDto save(ProjectDto dto,Long userId) {
		
		Project project;
		
		ProjectMember member;
		
		boolean isUpdate=false;
		
		if (dto.getIdGuid()!=null && dto.getIdGuid().length()>0) {
		
			
			if (!projectMemberService.isOwner(userId, dto.getIdGuid())) {
			
				throw new SecurityException("Usuario no tiene permisos necesarios");
			}
			
			project=projectDao.findProjectActiveById(dto.getIdGuid(), Constants.STATUS_ACTIVE)
					.orElseThrow(() -> new NoSuchElementException("Projecto no encontrado") );	
			isUpdate=true;
		}else {
			project=new Project(UUID.randomUUID().toString());
		}
		
		Usuario user = usuarioDao.findById(userId).orElseThrow(() -> new NoSuchElementException("Informacion no encontrada"));
		
		project.setName(dto.getName());
		project.setDescripcion(dto.getDescripcion());
		project.setStatus(dto.getStatus());
		
	    if (isUpdate) {
	    	project.setUsuarioModificacion(user.getEmail());	    	
	    }else {
	    	project.setOwner(user);
	    	
	    }
	    
	    project=projectDao.save(project);
	    	
		if (!isUpdate) {
			member=new ProjectMember();
			member.setProject(project);
			member.setRole(ProjectRole.OWNER);
			member.setUsuario(user);
			member.setUsuarioCreacion(user.getEmail());
			
			projectMemberService.save(member);
			
		}
	    
	    
		return new ProjectDto(project);
	}



	@Override
	public ProjectDto findByProjectIdAndUserId(String id, Long userId) {
		
		Project project = projectDao.findByIdAndUserId(id,userId).orElseThrow(() -> new NoSuchElementException("Projecto no encontrado"));
		
		return new ProjectDto(project);
	}



	@Override
	public List<TareaDto> findTasksByProjectId(String id, Long userId) {
		
		
		
		return findByProjectIdAndUserId(id, userId).getListTask(); 
	}



	@Override
	public boolean existsProjectActive(String id) {
		
		return projectDao.existsByIdGuidAndStatusIs(id, Constants.STATUS_ACTIVE);
	}



	@Override
	public Optional<Project> findByProjectId(String projectId) {
		
		return projectDao.findByIdGuidAndStatusIs(projectId,Constants.STATUS_ACTIVE);
	}

	@Override
	@Transactional
	public void deleteProject(String projectId, Long authUserId) {
		
		Project project = findByProjectId(projectId).orElseThrow(() -> new NoSuchElementException("Proyecto no encontrado"));
		
		if (!projectMemberService.isOwner(authUserId, projectId)) {
			throw new SecurityException("No tienes los permisos necesarios para realizar la accion");
		}							
				
		List<Tarea> tareas=project.getListTarea();
		
		String username = usuarioDao.findUsernameById(authUserId);
		
		tareas.stream().forEach(item -> {
			item.setStatus(Constants.STATUS_INACTIVE);
			item.setUsuarioModificacion(username);
			item.setUsuarios(null);
		});
		
		
		tareaDao.saveAll(tareas);
				
		
		List<ProjectMember> members=project.getProjectMember();
		
		members.stream().forEach(member -> {
			member.setStatus(Constants.STATUS_INACTIVE);
		});
		
		projectMemberService.saveAll(members);
		
		project.setStatus(Constants.STATUS_INACTIVE);
		
		projectDao.save(project);
		
		
	}

	@Override
	public int getProjectCountRoleOwner(Long userId) {
		
		return projectDao.getProjectCountRoleOwner(userId);
	}

	@Override
	public int getProjectCountActive(Long userId) {
		
		return projectDao.getProjectCountActive(userId);
	}




	
	
	
	
}
