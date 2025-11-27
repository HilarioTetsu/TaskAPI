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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.springboot.app.models.dao.IPrioridadTareaDao;
import com.springboot.app.models.dao.ITareaDao;
import com.springboot.app.models.dao.ITareaStatusDao;
import com.springboot.app.models.dao.ITareaTagsDao;
import com.springboot.app.models.dtos.PrioridadTareaDto;
import com.springboot.app.models.dtos.TagDto;
import com.springboot.app.models.dtos.TareaDto;
import com.springboot.app.models.dtos.TareaStatusDto;
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

	private final ITagService tagService;

	public TareaServiceImpl(ITareaDao tareaDao, IPrioridadTareaDao prioridadDao, ITareaStatusDao tareaStatusDao,
			ITareaTagsDao tareaTagsDao, IUsuarioService usuarioService, IProjectService projectService,
			IProjectMemberService projectMemberService, ITagService tagService) {
		super();
		this.tareaDao = tareaDao;
		this.prioridadDao = prioridadDao;
		this.tareaStatusDao = tareaStatusDao;
		this.tareaTagsDao = tareaTagsDao;
		this.usuarioService = usuarioService;
		this.projectService = projectService;
		this.projectMemberService = projectMemberService;
		this.tagService = tagService;
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
	public TareaDto save(TareaDto dto, Long userId) {

		boolean isUpdate = false;

		Optional<PrioridadTarea> prioridadTarea = prioridadDao.findById(dto.getId_prioridad());

		if (prioridadTarea.isEmpty()) {
			throw new NoSuchElementException("Prioridad no encontrada");
		}

		Optional<TareaStatus> tareaStatus = tareaStatusDao.findById(dto.getId_tarea_status());

		if (tareaStatus.isEmpty())
			throw new NoSuchElementException("Status de tarea no encontrada");

		if (dto.getIdGuid() != null) {
			isUpdate = true;
		}

		Tarea tarea = (dto.getIdGuid() == null) ? new Tarea(UUID.randomUUID().toString())
				: tareaDao.findById(dto.getIdGuid(), userId)
						.orElseThrow(() -> new NoSuchElementException("Tarea no encontrada"));

		Usuario user = usuarioService.findByUserId(userId);

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
		} else {
			tarea.setOwner(user);
		}

		TareaDto saved = new TareaDto(tareaDao.saveAndFlush(tarea));

		if (project == null) {
			asignarTarea(Arrays.asList(userId), saved.getIdGuid(), userId);
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
	public List<TagDto> asignarTag(String tareaId, Integer tagId, Long authUserId) {

		Tarea tarea = findByIdGuid(tareaId).orElseThrow(() -> new NoSuchElementException("Tarea no encontrada"));

		boolean isOwnerTask = tarea.getOwner().getId() == authUserId;

		boolean hasProject = tarea.getProject() != null;

		boolean isOwnerProject = hasProject ? projectMemberService.isOwner(authUserId, tarea.getProject().getIdGuid())
				: false;

		if (!hasProject && !isOwnerTask) {
			throw new AccessDeniedException("No tienes los permisos para realizar esta accion");
		}

		if (!isOwnerTask && !isOwnerProject) {
			throw new AccessDeniedException("No tienes los permisos para realizar esta accion");
		}

		Tag tag = tagService.getTagActiveById(tagId).orElseThrow(() -> new NoSuchElementException("Tag no encontrado"));

		if (tarea.getTareaTagsList().stream().anyMatch(x -> x.getTag().getId().equals(tag.getId()))) {
			throw new IllegalStateException("Tag ya asociado a la tarea");
		}

		TareaTags tareaTags = new TareaTags(tarea, tag);

		tareaTagsDao.saveAndFlush(tareaTags);

		List<Tag> listTags = tareaDao.getTagsFromTarea(tarea.getIdGuid());

		return listTags.stream().map(x -> new TagDto(x)).toList();
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

			save(tarea);

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

		save(tarea);

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

		if (tarea.getProject() == null && tarea.getOwner().getId() != userAuthId) {
			throw new SecurityException("No tiene los permisos necesarios sobre esta tarea");
		}

		Project projecto = tarea.getProject();

		if (!projectMemberService.canEditTasks(userAuthId, projecto.getIdGuid())) {
			throw new SecurityException("No tiene los permisos necesarios sobre esta tarea");
		}

		tarea.setStatus(Constants.STATUS_INACTIVE);
		tarea.setUsuarios(null);

		save(tarea);

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
	public List<PrioridadTareaDto> findAllPrioridadesTarea() {

		return prioridadDao.findAll().stream().map(p -> new PrioridadTareaDto(p)).toList();
	}

	@Override
	@Transactional
	public void quitarTag(String idTarea, Integer idTag, Long authUserId) {

		Tarea tarea = tareaDao.findById(idTarea).orElseThrow(() -> new NoSuchElementException("Tarea no encontrada"));

		if (tarea.getOwner().getId() != authUserId
				&& !projectMemberService.isOwner(authUserId, tarea.getProject().getIdGuid())) {
			throw new AccessDeniedException("No tienes los permisos para realizar esta accion");
		}

		List<TareaTags> tareaTags = tarea.getTareaTagsList();

		if (tareaTags == null || tareaTags.size() == 0) {
			return;
		}

		int tareaTagId = 0;

		for (TareaTags item : tareaTags) {

			if (item.getTag().getId() == idTag && item.getTarea().getIdGuid().equals(idTarea)) {

				tareaTagsDao.delete(item);

				break;
			}

		}

	}

	@Override
	@Transactional
	public TareaDto save(Tarea tarea) {

		return new TareaDto(tareaDao.save(tarea));
	}

	@Override
	public List<Object[]> countTareasByPrioridad(Long userId) {

		return tareaDao.countTareasByPrioridad(userId);
	}

	@Override
	public List<Object[]> countTareasByTareaStatus(Long userId) {

		return tareaDao.countTareasByTareaStatus(userId);
	}

	@Override
	public int getTareasHoyCountByUserId(Long userId) {

		return tareaDao.getTareasHoyCountByUserId(userId);
	}

	@Override
	public int getTareasVencidasCountByUserId(Long userId) {

		return tareaDao.getTareasVencidasCountByUserId(userId);
	}

	@Override
	public int getTareasPendientesCountByUserId(Long userId) {

		return tareaDao.getTareasPendientesCountByUserId(userId);
	}

	@Override
	public List<TareaStatusDto> findAllTareaStatus() {		
		return tareaStatusDao.findAll().stream().map(ts -> new TareaStatusDto(ts)).toList();
	}

}
