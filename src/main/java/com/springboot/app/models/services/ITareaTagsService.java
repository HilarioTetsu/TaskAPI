package com.springboot.app.models.services;

import java.util.List;

import com.springboot.app.models.dtos.TagDto;

public interface ITareaTagsService {

	
	List<TagDto> asignarTag(String tareaId, Integer tagId, Long authUserId);
	
	void quitarTag(String idTarea, Integer idTag, Long authUserId);
	
	
}
