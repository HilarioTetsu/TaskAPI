package com.springboot.app.models.dtos;

import java.time.LocalDateTime;
import java.util.List;

import com.springboot.app.models.entities.Comment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentViewDto {

	public CommentViewDto(Comment comment) {
		this.id = comment.getId();
		this.tareaId = comment.getTarea().getIdGuid();
		this.ownerUserId = comment.getAutor().getId();
		this.body = comment.getBody();
		this.mentionsUserIds = comment.getMentions() != null && comment.getMentions().size() > 0
				? comment.getMentions().stream().map(u -> u.getId()).toList()
				: null;
		this.status = comment.getStatus();
		this.fechaCreacion = comment.getFechaCreacion();
	}

	
	private Long id;

	
	
	private String tareaId;

	
	private Long ownerUserId;

	
	private String body;

	
	private List<Long> mentionsUserIds;

	
	private List<String> confirmMediasStorageKeyUrls;

	
	private Short status;

	
	private LocalDateTime fechaCreacion;
	
}
