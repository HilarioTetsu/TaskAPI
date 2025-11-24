package com.springboot.app.models.services;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.springboot.app.models.dao.ITagDao;
import com.springboot.app.models.dtos.TagDto;
import com.springboot.app.models.entities.Tag;

@Service
public class TagServiceImpl implements ITagService {

	private final ITagDao tagDao;
	
		
	public TagServiceImpl(ITagDao tagDao) {
		this.tagDao = tagDao;
	}


	@Override
	public List<TagDto> findByNameContaining(String nameTag) {
		
		return tagDao.findByNameContaining(nameTag).stream().map(tag -> new TagDto(tag)).toList();
	}

	@Override
	public TagDto getTagActiveByIdOrName(Integer id,String nameTag) {
		
		Tag tag=tagDao.getTagActiveByIdOrName(id,nameTag.toUpperCase()).orElseThrow(() -> new NoSuchElementException("Tag no encontrado"));
		

		return new TagDto(tag);
	}

	@Override
	public Page<TagDto> getAll(int pagina, int tamanio) {
		
		Pageable pageable = PageRequest.of(pagina, tamanio);
		
		return tagDao.getAll(pageable).map(tag -> new TagDto(tag));
	}


	@Override
	public TagDto save(TagDto tagDto) {
		
		Tag tag= (tagDto.getId()==null) ?
				new Tag() : 
					tagDao.findById(tagDto.getId())
					.orElseThrow(
							() -> new NoSuchElementException("Tag no encontrado"));
		
		tag.setName(tagDto.getName());
		tag.setColor(tagDto.getColor());
		tag.setStatus(tagDto.getStatus());
		
		
		return new TagDto(tagDao.save(tag));
	}


	@Override
	public Optional<Tag> getTagActiveById(Integer id) {

		Optional<Tag> tag=tagDao.getTagActiveByIdOrName(id,null);
		
		if (tag.isEmpty()) {
			return Optional.empty();
		}
		
		return tag;
	}

}
