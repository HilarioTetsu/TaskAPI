package com.springboot.app.models.dtos;

import java.time.LocalDateTime;
import java.util.List;

import com.springboot.app.models.entities.Comment;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentDto {

	
	public CommentDto(Comment comment) {
	this.id=comment.getId();
	this.tareaId=comment.getTarea().getIdGuid();
	this.ownerUserId=comment.getAutor().getId();
	this.body=comment.getBody();
	this.mentionsUserIds= comment.getMentions()!=null && comment.getMentions().size()>0 
			? comment.getMentions()
					.stream().
					map(u -> u.getId())
					.toList():
						null;	
	this.confirmMediaStorageKeyId=comment.getAdjuntos()!=null && comment.getAdjuntos().size()>0
			? comment.getAdjuntos()
					.stream()
					.map(adj -> adj.getStorageKey())
					.toList():
						null;
	this.status=comment.getStatus();
	
	this.fechaCreacion=comment.getFechaCreacion();
	}
	
	
	private Long id;
	
	@NotBlank
	private String tareaId;
		
	private Long ownerUserId;
	
	@NotBlank
	private String body;
	
	private List<Long> mentionsUserIds;
	
	private List<String> confirmMediaStorageKeyId;
	
	private Short status;
	
	private LocalDateTime fechaCreacion;
}
