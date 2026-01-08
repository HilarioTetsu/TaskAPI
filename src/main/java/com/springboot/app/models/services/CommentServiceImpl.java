package com.springboot.app.models.services;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.springboot.app.models.dao.ICommentDao;
import com.springboot.app.models.dtos.CommentDto;
import com.springboot.app.models.dtos.CommentUpdateDto;
import com.springboot.app.models.entities.Comment;
import com.springboot.app.models.entities.Media;
import com.springboot.app.models.entities.Tarea;
import com.springboot.app.models.entities.Usuario;
import com.springboot.app.utils.Constants;
import com.springboot.app.utils.Utils;

@Service
public class CommentServiceImpl implements ICommentService {

	private final ICommentDao commentDao;

	private final ITareaService tareaService;

	private final IUsuarioService usuarioService;

	private final IMediaService mediaService;

	private final IProjectMemberService projectMemberService;

	public CommentServiceImpl(ICommentDao commentDao, ITareaService tareaService, IUsuarioService usuarioService,
			IMediaService mediaService, IProjectMemberService projectMemberService) {
		super();
		this.commentDao = commentDao;
		this.tareaService = tareaService;
		this.usuarioService = usuarioService;
		this.mediaService = mediaService;
		this.projectMemberService = projectMemberService;
	}

	@Override
	@Transactional
	public CommentDto saveComment(CommentDto dto, Long authUserId) {

		Tarea tarea = tareaService.findByIdGuid(dto.getTareaId())
				.orElseThrow(() -> new NoSuchElementException("Tarea no encontrada"));

		if (!tareaService.isAsignedToThisTask(tarea.getIdGuid(), authUserId)) {
			
			if (!projectMemberService.canEditTasks(authUserId, tarea.getProject().getIdGuid())) {
				throw new SecurityException("No puedes comentar en esta tarea");
			}					
		}

		Usuario autor = usuarioService.findByUserId(authUserId);

		List<Usuario> mentions = dto.getMentionsUserIds() != null && dto.getMentionsUserIds().size() > 0
				? usuarioService.findAllByIds(dto.getMentionsUserIds())
				: null;

		if (mentions != null) {
			mentions.removeIf(u -> u.getId() == authUserId);
		}

		Comment comment = new Comment();

		comment.setAutor(autor);
		comment.setBody(dto.getBody());
		comment.setMentions(mentions);
		comment.setTarea(tarea);

		return new CommentDto(commentDao.save(comment));
	}

	@Override
	@Transactional
	public CommentDto saveComment(CommentDto dto, List<Media> mediasSaved, Long authUserId) {

		if (!tareaService.isAsignedToThisTask(dto.getTareaId(), authUserId)) {
			throw new SecurityException("No puedes comentar en esta tarea");
		}

		Usuario autor = usuarioService.findByUserId(authUserId);

		Tarea tarea = tareaService.findByIdGuid(dto.getTareaId())
				.orElseThrow(() -> new NoSuchElementException("Tarea no encontrado"));

		List<Usuario> mentions = dto.getMentionsUserIds() != null && dto.getMentionsUserIds().size() > 0
				? usuarioService.findAllByIds(dto.getMentionsUserIds())
				: null;

		if (mentions != null) {
			mentions.removeIf(u -> u.getId() == authUserId);
		}

		Comment comment = new Comment();

		comment.setAutor(autor);
		comment.setBody(dto.getBody());
		comment.setMentions(mentions);
		comment.setTarea(tarea);
		comment.setAdjuntos(mediasSaved);

		return new CommentDto(commentDao.save(comment));
	}

	@Override
	@Transactional(readOnly = true)
	public Page<CommentDto> getAll(Integer pagina, Integer tamanio, String sorts, Long userId) {

		Pageable pageable = PageRequest.of(pagina, tamanio, Utils.parseSortParams(sorts));
		
			Page<CommentDto> pageDtos = commentDao.findAllByUserId(pageable, userId).map(comm -> {
			
			List<Media> medias = comm.getAdjuntos().stream()
					.filter(media -> media.getStatus().equals(Constants.STATUS_READY))
						.toList();
									
			List<String> urls = mediaService.createPresignedGetUrls(medias.stream().map(m -> m.getStorageKey()).toList());
			
			CommentDto dto = new CommentDto(comm);
			
			dto.setConfirmMediaStorageKeyUrls(urls);
			
			return dto;
			
		});
		
		
		

		return pageDtos;
	}

	@Override
	public Page<CommentDto> getAllByTareaId(Integer pagina, Integer tamanio, String sorts, Long userId,
			String tareaId) {

		Tarea tarea = tareaService.findByIdGuid(tareaId)
				.orElseThrow(() -> new NoSuchElementException("Tarea no encontrada"));

		boolean esOwner = Objects.equals(tarea.getOwner().getId(), userId);

		boolean esUsuarioAsignado = tareaService.isAsignedToThisTask(tareaId, userId);

		boolean esMiembroProyecto = projectMemberService.isMember(userId, tarea.getProject().getIdGuid());

		if (!(esOwner || esUsuarioAsignado || esMiembroProyecto)) {
			throw new SecurityException("No tienes los permisos necesarios para esta tarea");
		}

		Pageable pageable = PageRequest.of(pagina, tamanio, Utils.parseSortParams(sorts));


		Page<CommentDto> pageDtos = commentDao.findAllByTareaId(pageable, tareaId).map(comm -> {
			
			List<Media> medias = comm.getAdjuntos().stream()
					.filter(media -> media.getStatus().equals(Constants.STATUS_READY))
						.toList();
									
			List<String> urls = mediaService.createPresignedGetUrls(medias.stream().map(m -> m.getStorageKey()).toList());
			
			CommentDto dto = new CommentDto(comm);
			
			dto.setConfirmMediaStorageKeyUrls(urls);
			
			return dto;
			
		});
		
		
		

		return pageDtos;
	}

	@Override
	@Transactional
	public void deleteComment(Long commentId, Long userId) {

		Comment comment = commentDao.findById(commentId)
				.orElseThrow(() -> new NoSuchElementException("Comentario no encontrado"));

		if (comment.getAutor().getId() != userId
				&& !projectMemberService.isOwner(userId, comment.getTarea().getProject().getIdGuid())) {
			throw new SecurityException("No tienes los permisos necesarios para este comentario");
		}

		comment.setStatus(Constants.STATUS_INACTIVE);

		if (comment.getAdjuntos() != null && comment.getAdjuntos().size() > 0) {

			List<Media> mediaInactive = comment.getAdjuntos();

			mediaInactive.stream().forEach(adj -> adj.setStatus(Constants.STATUS_INACTIVE));

			comment.setAdjuntos(mediaService.saveAll(mediaInactive));

		}

		commentDao.save(comment);

	}

	@Override
	@Transactional
	public CommentDto updateComment(Long commentId, CommentUpdateDto dto, Long userId) {

		Comment comment = commentDao.findById(commentId)
				.orElseThrow(() -> new NoSuchElementException("Comentario no encontrado"));

		if (comment.getAutor().getId() != userId) {
			throw new SecurityException("No tienes los permisos necesarios para este commentario");
		}

		List<Usuario> mentions = dto.getMentionsUserIds() != null && dto.getMentionsUserIds().size() > 0
				? usuarioService.findAllByIds(dto.getMentionsUserIds())
				: null;

		if (mentions != null) {
			mentions.removeIf(u -> u.getId() == userId);
		}

		comment.setBody(dto.getBody());
		comment.setMentions(mentions);

		commentDao.save(comment);

		return new CommentDto(commentDao.save(comment));
	}
	
	
	


}
