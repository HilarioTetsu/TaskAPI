package com.springboot.app.models.services;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.springboot.app.models.dao.IPrioridadTareaDao;
import com.springboot.app.models.dao.ITareaDao;
import com.springboot.app.models.dao.ITareaStatusDao;
import com.springboot.app.models.dao.ITareaTagsDao;
import com.springboot.app.models.dtos.TagDto;
import com.springboot.app.models.dtos.TareaDto;
import com.springboot.app.models.entities.PrioridadTarea;
import com.springboot.app.models.entities.Project;
import com.springboot.app.models.entities.Tag;
import com.springboot.app.models.entities.Tarea;
import com.springboot.app.models.entities.TareaStatus;
import com.springboot.app.models.entities.TareaTags;
import com.springboot.app.models.entities.Usuario;
import com.springboot.app.utils.Constants;
import com.springboot.app.utils.Utils;



@Service
public class TareaServiceImpl implements ITareaService {

	private final ITareaDao tareaDao;
	
	private final IPrioridadTareaDao prioridadDao;
	
	private final ITareaStatusDao tareaStatusDao;
	
	private final ITareaTagsDao tareaTagsDao;
		
	private final IUsuarioService usuarioService;
		
	private final IProjectService projectService;

	private final IProjectMemberService projectMemberService;
	





	public TareaServiceImpl(ITareaDao tareaDao, IPrioridadTareaDao prioridadDao, ITareaStatusDao tareaStatusDao,
			ITareaTagsDao tareaTagsDao, IUsuarioService usuarioService, IProjectService projectService,
			IProjectMemberService projectMemberService) {
		super();
		this.tareaDao = tareaDao;
		this.prioridadDao = prioridadDao;
		this.tareaStatusDao = tareaStatusDao;
		this.tareaTagsDao = tareaTagsDao;
		this.usuarioService = usuarioService;
		this.projectService = projectService;
		this.projectMemberService = projectMemberService;
	}



	@Override
	@Transactional(readOnly = true)
	public Optional<TareaDto> findByIdGuid(String id,Long userId) {
	
		Optional<Tarea> tarea=tareaDao.findById(id,userId);
		
		if (tarea.isEmpty()) {
		
			return Optional.empty();
		}
				
		
		
		return Optional.of(new TareaDto(tarea.get()));
	}



	@Override
	@Transactional
	public TareaDto save(TareaDto dto,Long userId) {
		
		boolean isUpdate=false;
		
		Optional<PrioridadTarea> prioridadTarea = prioridadDao.findById(dto.getId_prioridad());
		
	    if (prioridadTarea.isEmpty()) {
	        throw new NoSuchElementException("Prioridad no encontrada");
	    }
	    
	    Optional<TareaStatus> tareaStatus=tareaStatusDao.findById(dto.getId_tarea_status());
	    
	    if(tareaStatus.isEmpty()) throw new NoSuchElementException("Status de tarea no encontrada");
	    
	    if (dto.getIdGuid()!=null) {
	    	isUpdate=true;
		}
	    
	    Tarea tarea= (dto.getIdGuid()==null) ?  
	    		new Tarea(UUID.randomUUID().toString())
	    		:  tareaDao.findById(dto.getIdGuid(),userId)
                .orElseThrow(() -> new NoSuchElementException("Tarea no encontrada"));
	    
	    Usuario user = usuarioService.findByUserId(userId).orElseThrow(() -> new NoSuchElementException("Informacion no encontrada"));
	    
	    Project project = projectService.findByProjectId(dto.getProject_id()).orElse(null);
	    
	    
	    
	    tarea.setTitulo(dto.getTitulo());	    
	    tarea.setDescripcion(dto.getDescripcion());
	    tarea.setFechaLimite(dto.getFechaLimite());
	    tarea.setStatus(dto.getStatus());
	    tarea.setPrioridad(prioridadTarea.get());
        tarea.setTareaStatus(tareaStatus.get());
        tarea.setProject(project);
	    
	    
	    if (isUpdate) {
	    	tarea.setUsuarioModificacion(user.getEmail());	    	
	    }else {
	    	tarea.setOwner(user);
	    }
	    	
	    	
	    TareaDto saved=	new TareaDto(tareaDao.saveAndFlush(tarea));
	    
	    
	    if (project==null) {
			asignarTarea(Arrays.asList(userId), saved.getIdGuid(), userId);
		}
	    
        return saved;
	}



	
	
	@Override
	@Transactional(readOnly = true)
	public Page<TareaDto> getAllActives(int pagina, int tamanio, List<Short> tareaStatusIds, List<Short> prioridadIds,
			LocalDate fechaLimiteDesde, LocalDate fechaLimiteHasta, String busquedaDesc, String busquedaTitulo,
			String sorts,Long ownerId) {
		

		Pageable pageable = PageRequest.of(pagina, tamanio,Utils.parseSortParams(sorts));
		
		boolean aplicarPrioridad= prioridadIds!=null && !prioridadIds.isEmpty();
		
		boolean aplicarTareaStatus= tareaStatusIds!=null && !tareaStatusIds.isEmpty();
		
		
		
		return tareaDao.getAllActives(pageable,
				aplicarTareaStatus ? tareaStatusIds: List.of(),
				aplicarPrioridad ? prioridadIds : List.of() ,
						fechaLimiteDesde,
						fechaLimiteHasta,
						busquedaDesc,
						busquedaTitulo,						
						aplicarPrioridad,
						aplicarTareaStatus,ownerId)
				.map(tarea -> new TareaDto(tarea));
	}






	@Override
	@Transactional(readOnly = true)
	public List<TagDto> asignarTag(Tarea tarea,Tag tag) {
		
		if (tarea.getTareaTagsList().stream().anyMatch(x ->x.getTag().getId().equals(tag.getId()))) {
			throw new IllegalStateException("Tag ya asociado a la tarea");
		}
		
		
		
		TareaTags tareaTags = new TareaTags(tarea, tag);
		
		tareaTagsDao.saveAndFlush(tareaTags);
		
		
		List<Tag> listTags = tareaDao.getTagsFromTarea(tarea.getIdGuid());
				
		
		return listTags.stream().map(x -> new TagDto(x)).toList();
	}



	@Override
	@Transactional(readOnly = true)
	public Optional<Tarea> findTareaByIdGuid(String id,Long userId) {
		
		Optional<Tarea> tarea=tareaDao.findById(id,userId);
		
		if (tarea.isEmpty()) {
		
			return Optional.empty();
		}
				
		
		return tarea;
	}




	@Override
	@Transactional
	public void asignarTarea(List<Long> userIds, String tareaId, Long userAuthId) {
		
		
		Tarea tarea=tareaDao.findById(tareaId).orElseThrow(() -> new NoSuchElementException("Tarea no encontrada"));
		
		if (tarea.getProject()==null) {
			
			if (userIds.size()>1) {
				throw new IllegalStateException("Una tarea no ligada a un proyecto no puede asignarse a mas de un usuario");
			}
			
			if (tarea.getOwner().getId()!=userAuthId) {
				throw new SecurityException("No tienes los permisos necesarios sobre esta tarea");
			}
			
			List<Usuario> usuarios=usuarioService.findAllByIds(userIds);
			
			tarea.setUsuarios(usuarios);
			
			tareaDao.save(tarea);
			
			return;
			
			
			//throw new IllegalStateException("La tarea no se encuentra ligada a ningun proyecto");
		}
		
		if (!projectMemberService.isOwner(userAuthId, tarea.getProject().getIdGuid())) {
			throw new SecurityException("No tienes los permisos necesarios");
		}
		
		List<Usuario> usuarios=usuarioService.findAllByIds(userIds);
		
		usuarios=usuarios.stream().
				filter(user -> projectMemberService.isMember(user.getId(), tarea.getProject().getIdGuid())).collect(Collectors.toList());
		
		
		if (tarea.getUsuarios()!=null) {
			
			usuarios.addAll(tarea.getUsuarios());
		}
		
		
		tarea.setUsuarios(usuarios);
		
		tareaDao.save(tarea);
		
		
	}



	@Override
	@Transactional(readOnly = true)
	public List<TareaDto> getTareasAsignadasByProjectId(String projectId, Long authUserId) {
		
		if (!projectService.existsProjectActive(projectId)) {
			throw new NoSuchElementException("Proyecto inexistente");
		}
		
		if (!projectMemberService.isMember(authUserId, projectId)) {
			throw new SecurityException("No tiene los permisos necesarios en este proyecto");
		}
		
		Usuario user = usuarioService.findByUserId(authUserId).get();
		
		
		return user.getTareasAsignadas().stream().filter(t -> t.getProject().getIdGuid().equals(projectId)).map(t -> new TareaDto(t)).toList();
	}



	@Override
	@Transactional
	public void deleteTarea(String tareaId,Long userAuthId) {
		
		if (!tareaDao.existsById(tareaId)) {
			throw new NoSuchElementException("Tarea inexistente");
		}
		
		Tarea tarea=tareaDao.findById(tareaId).get();
		
		
		if (tarea.getProject()==null && tarea.getOwner().getId()!=userAuthId) {
			throw new SecurityException("No tiene los permisos necesarios sobre esta tarea");
		}
		
		Project projecto=tarea.getProject();
						
		
		if (!projectMemberService.canEditTasks(userAuthId, projecto.getIdGuid())) {
			throw new SecurityException("No tiene los permisos necesarios sobre esta tarea");
		}
		
		tarea.setStatus(Constants.STATUS_INACTIVE);
		tarea.setUsuarios(null);
		
		tareaDao.save(tarea);
		
		
	}



	@Override
	public boolean isAsignedToThisTask(String tareaId, Long userId) {
		
		Integer count = tareaDao.isAsignedToThisTask(tareaId, userId);
	    return count != null && count > 0;
	}



	@Override
	public Optional<Tarea> findByIdGuid(String tareaId) {
		
		return tareaDao.findById(tareaId);
	}





}
