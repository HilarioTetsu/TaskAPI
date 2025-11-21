package com.springboot.app.models.services;

import java.util.List;

import org.springframework.data.domain.Page;

import com.springboot.app.models.dtos.CommentDto;
import com.springboot.app.models.dtos.CommentUpdateDto;
import com.springboot.app.models.entities.Media;

import jakarta.validation.Valid;





public interface ICommentService {

	CommentDto saveComment(CommentDto dto, Long authUserId);

	CommentDto saveComment(CommentDto dto, List<Media> mediasSaved, Long authUserId);

	Page<CommentDto> getAll(Integer pagina, Integer tamanio, String sorts, Long userId);

	Page<CommentDto> getAllByTareaId(Integer pagina, Integer tamanio, String sorts, Long userId, String tareaId);

	void deleteComment(Long commentId, Long userId);

	CommentDto updateComment(Long commentId, CommentUpdateDto dto, Long userId);

}
