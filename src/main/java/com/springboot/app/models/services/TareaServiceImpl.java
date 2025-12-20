package com.springboot.app.models.services;

import java.time.LocalDate;
import java.util.ArrayList;
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

import com.springboot.app.models.dao.ICommentDao;
import com.springboot.app.models.dao.ITareaDao;
import com.springboot.app.models.dtos.TareaDto;
import com.springboot.app.models.entities.Comment;
import com.springboot.app.models.entities.PrioridadTarea;
import com.springboot.app.models.entities.Project;
import com.springboot.app.models.entities.Tag;
import com.springboot.app.models.entities.Tarea;
import com.springboot.app.models.entities.TareaStatus;
import com.springboot.app.models.entities.Usuario;
import com.springboot.app.utils.Constants;
import com.springboot.app.utils.TareaChangeLogHelper;
import com.springboot.app.utils.Utils;

@Service
public class TareaServiceImpl implements ITareaService {

	private final ITareaDao tareaDao;

	private final IUsuarioService usuarioService;

	private final IProjectService projectService;

	private final IProjectMemberService projectMemberService;	

	private final ICommentDao commentDao;
	
	private final CatalogoService catalogoService;

	private final TareaChangeLogHelper tareaLogHelper;





	public TareaServiceImpl(ITareaDao tareaDao, IUsuarioService usuarioService, IProjectService projectService,
			IProjectMemberService projectMemberService, ICommentDao commentDao, CatalogoService catalogoService,
			TareaChangeLogHelper tareaLogHelper) {
		super();
		this.tareaDao = tareaDao;
		this.usuarioService = usuarioService;
		this.projectService = projectService;
		this.projectMemberService = projectMemberService;
		this.commentDao = commentDao;
		this.catalogoService = catalogoService;
		this.tareaLogHelper = tareaLogHelper;
	}

	@Override
	@Transactional(readOnly = true)
	public TareaDto findByIdGuidAndUserId(String id, Long userId) {

		Tarea tarea = tareaDao.findById(id, userId)
				.orElseThrow(() -> new NoSuchElementException("Tarea no encontrada"));

		return (new TareaDto(tarea));
	}

	@Override
	@Transactional
	public TareaDto save(TareaDto dto, Long userAuthId) {

		boolean isUpdate = false;

		String cambiosTarea;

		Optional<PrioridadTarea> prioridadTarea = catalogoService.findPrioridadTareaById(dto.getId_prioridad());

		if (prioridadTarea.isEmpty()) {
			throw new NoSuchElementException("Prioridad no encontrada");
		}

		Optional<TareaStatus> tareaStatus = catalogoService.findTareaStatusById(dto.getId_tarea_status());

		if (tareaStatus.isEmpty())
			throw new NoSuchElementException("Status de tarea no encontrada");

		if (dto.getIdGuid() != null) {
			isUpdate = true;
		}

		Tarea tarea = (dto.getIdGuid() == null) ? new Tarea(UUID.randomUUID().toString())
				: tareaDao.findById(dto.getIdGuid(), userAuthId)
						.orElseThrow(() -> new NoSuchElementException("Tarea no encontrada"));

		Usuario user = usuarioService.findByUserId(userAuthId);

		Project project = projectService.findByProjectId(dto.getProject_id()).orElse(null);

		if (project != null && !projectMemberService.canEditTasks(userAuthId, project.getIdGuid())) {

			throw new SecurityException("No tienes permisos para gestionar tareas en este proyecto");
		}

		if (isUpdate) {

			cambiosTarea = tareaLogHelper.generarMensajeCambiosTarea(tarea, dto, user, prioridadTarea.get(),
					tareaStatus.get(), project);

			List<Long> mentionsIds = new ArrayList<>();
			Comment comment = new Comment();

			comment.setBody(cambiosTarea);
			comment.setAutor(user);
			comment.setTarea(tarea);

			if (project != null) {
				mentionsIds.addAll(tarea.getUsuarios().stream().map(u -> u.getId()).toList());

				mentionsIds.add(project.getOwner().getId());

				mentionsIds.add(tarea.getOwner().getId());

				mentionsIds.removeIf(u -> u.equals(userAuthId));

				comment.setMentions(usuarioService.findAllByIds(mentionsIds));
			}

			commentDao.save(comment);

			tarea.setUsuarioModificacion(user.getUsername());
		} else {
			tarea.setOwner(user);
		}

		tarea.setTitulo(dto.getTitulo());
		tarea.setDescripcion(dto.getDescripcion());
		tarea.setFechaLimite(dto.getFechaLimite());
		tarea.setStatus(dto.getStatus());
		tarea.setPrioridad(prioridadTarea.get());
		tarea.setTareaStatus(tareaStatus.get());
		tarea.setProject(project);

		TareaDto saved = new TareaDto(tareaDao.saveAndFlush(tarea));

		if (project == null) {
			asignarTarea(Arrays.asList(userAuthId), saved.getIdGuid(), userAuthId);
		}

		return saved;
	}

	@Override
	@Transactional(readOnly = true)
	public Page<TareaDto> getAllActives(int pagina, int tamanio, List<Short> tareaStatusIds, List<Short> prioridadIds,
			LocalDate fechaLimiteDesde, LocalDate fechaLimiteHasta, String busquedaDesc, String busquedaTitulo,
			String sorts, Long ownerId) {

		Pageable pageable = PageRequest.of(pagina, tamanio, Utils.parseSortParams(sorts));

		boolean aplicarPrioridad = prioridadIds != null && !prioridadIds.isEmpty();

		boolean aplicarTareaStatus = tareaStatusIds != null && !tareaStatusIds.isEmpty();

		return tareaDao
				.getAllActives(pageable, aplicarTareaStatus ? tareaStatusIds : List.of(),
						aplicarPrioridad ? prioridadIds : List.of(), fechaLimiteDesde, fechaLimiteHasta, busquedaDesc,
						busquedaTitulo, aplicarPrioridad, aplicarTareaStatus, ownerId)
				.map(tarea -> new TareaDto(tarea));
	}


	@Override
	@Transactional(readOnly = true)
	public Tarea findTareaByIdGuidAndUserId(String id, Long userId) {

		Tarea tarea = tareaDao.findById(id, userId)
				.orElseThrow(() -> new NoSuchElementException("Tarea no encontrada"));

		return tarea;
	}

	@Override
	@Transactional
	public void asignarTarea(List<Long> userIds, String tareaId, Long userAuthId) {

		Tarea tarea = tareaDao.findById(tareaId).orElseThrow(() -> new NoSuchElementException("Tarea no encontrada"));

		if (tarea.getProject() == null) {

			if (userIds.size() > 1) {
				throw new IllegalStateException(
						"Una tarea no ligada a un proyecto no puede asignarse a mas de un usuario");
			}

			if (tarea.getOwner().getId() != userAuthId) {
				throw new SecurityException("No tienes los permisos necesarios sobre esta tarea");
			}

			List<Usuario> usuarios = usuarioService.findAllByIds(userIds);

			tarea.setUsuarios(usuarios);

			tareaDao.save(tarea);

			return;

		}

		if (!projectMemberService.isOwner(userAuthId, tarea.getProject().getIdGuid())) {
			throw new SecurityException("No tienes los permisos necesarios");
		}

		List<Usuario> usuarios = usuarioService.findAllByIds(userIds);

		usuarios = usuarios.stream()
				.filter(user -> projectMemberService.isMember(user.getId(), tarea.getProject().getIdGuid()))
				.collect(Collectors.toList());

		if (tarea.getUsuarios() != null) {

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

		Usuario user = usuarioService.findByUserId(authUserId);

		return user.getTareasAsignadas().stream().filter(t -> t.getProject().getIdGuid().equals(projectId))
				.map(t -> new TareaDto(t)).toList();
	}

	@Override
	@Transactional
	public void deleteTarea(String tareaId, Long userAuthId) {

		if (!tareaDao.existsById(tareaId)) {
			throw new NoSuchElementException("Tarea inexistente");
		}

		Tarea tarea = tareaDao.findById(tareaId).get();

		if (tarea.getProject() == null) {
			
	        if (!tarea.getOwner().getId().equals(userAuthId)) {
	            throw new SecurityException("No tienes permiso (Solo el dueño puede borrar tareas personales)");
	        }
	      
	    } 
	   
	    else {
	       
	        if (!projectMemberService.canEditTasks(userAuthId, tarea.getProject().getIdGuid())) {
	            throw new SecurityException("No tienes permisos de proyecto para borrar esta tarea");
	        }
	    }
		
		
		String username = usuarioService.findUsernameById(userAuthId);

		tarea.setStatus(Constants.STATUS_INACTIVE);
		tarea.setUsuarioModificacion(username);
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





	@Override
	@Transactional
	public TareaDto save(Tarea tarea) {

		return new TareaDto(tareaDao.save(tarea));
	}



	@Override
	public List<Tag> getTagsFromTarea(String idGuid) {
		
		return tareaDao.getTagsFromTarea(idGuid);
	}

}
