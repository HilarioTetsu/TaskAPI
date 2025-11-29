package com.springboot.app.models.services;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;

import com.springboot.app.models.dtos.TagDto;
import com.springboot.app.models.entities.Tag;


public interface ITagService {

	Page<TagDto> getAll(int pagina,int tamanio);
	
	List<TagDto> findByNameContaining(String nameTag);
	
	TagDto getTagActiveByIdOrName(Integer id,String nameTag);
	
	Tag getTagActiveById(Integer id);
	
	TagDto save(TagDto tagDto, Long authUser);

	void deleteTag(Integer id, Long userId);
	
}
