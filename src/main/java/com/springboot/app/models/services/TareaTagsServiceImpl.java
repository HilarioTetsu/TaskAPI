package com.springboot.app.models.services;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.springboot.app.models.dao.ITareaTagsDao;
import com.springboot.app.models.dtos.TagDto;
import com.springboot.app.models.entities.Tag;
import com.springboot.app.models.entities.Tarea;
import com.springboot.app.models.entities.TareaTags;

@Service
public class TareaTagsServiceImpl implements ITareaTagsService {

	private final ITareaService tareaService;
	
	private final IProjectMemberService projectMemberService;
	
	private final ITagService tagService;
	
	private final ITareaTagsDao tareaTagsDao;
	
	private final IUsuarioService usuarioService;
	
	


	public TareaTagsServiceImpl(ITareaService tareaService, IProjectMemberService projectMemberService,
			ITagService tagService, ITareaTagsDao tareaTagsDao, IUsuarioService usuarioService) {
		super();
		this.tareaService = tareaService;
		this.projectMemberService = projectMemberService;
		this.tagService = tagService;
		this.tareaTagsDao = tareaTagsDao;
		this.usuarioService = usuarioService;
	}

	@Override
	@Transactional
	public List<TagDto> asignarTag(String tareaId, Integer tagId, Long authUserId) {

		Tarea tarea = tareaService.findByIdGuid(tareaId).orElseThrow(() -> new NoSuchElementException("Tarea no encontrada"));

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

		Tag tag = tagService.getTagActiveById(tagId);

		if (tarea.getTareaTagsList()!=null && tarea.getTareaTagsList().stream().anyMatch(x -> x.getTag().getId().equals(tag.getId()))) {
			throw new IllegalStateException("Tag ya asociado a la tarea");
		}
		
		

		TareaTags tareaTags = new TareaTags(tarea, tag);
		
		tareaTags.setUsuarioCreacion(usuarioService.findUsernameById(authUserId));

		tareaTagsDao.saveAndFlush(tareaTags);

		List<Tag> listTags = tareaService.getTagsFromTarea(tarea.getIdGuid());

		return listTags.stream().map(x -> new TagDto(x)).toList();
	}
	
	@Override
	@Transactional
	public void quitarTag(String idTarea, Integer idTag, Long authUserId) {

		Tarea tarea = tareaService.findByIdGuid(idTarea).orElseThrow(() -> new NoSuchElementException("Tarea no encontrada"));

		if (tarea.getOwner().getId() != authUserId
				&& !projectMemberService.isOwner(authUserId, tarea.getProject().getIdGuid())) {
			throw new AccessDeniedException("No tienes los permisos para realizar esta accion");
		}

		List<TareaTags> tareaTags = tarea.getTareaTagsList();

		if (tareaTags == null || tareaTags.size() == 0) {
			return;
		}

		

		for (TareaTags item : tareaTags) {

			if (item.getTag().getId() == idTag && item.getTarea().getIdGuid().equals(idTarea)) {

				tareaTagsDao.delete(item);

				break;
			}

		}

	}

}
