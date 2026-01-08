package com.springboot.app.models.services;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.springboot.app.models.dao.ITagDao;
import com.springboot.app.models.dao.ITareaTagsDao;
import com.springboot.app.models.dtos.TagDto;
import com.springboot.app.models.entities.Tag;
import com.springboot.app.models.entities.Usuario;
import com.springboot.app.utils.Constants;

@Service
public class TagServiceImpl implements ITagService {

	private final ITagDao tagDao;
	
	private final ITareaTagsDao tareaTagsDao;
	
	private final IUsuarioService usuarioService;
	
	

	public TagServiceImpl(ITagDao tagDao, ITareaTagsDao tareaTagsDao, IUsuarioService usuarioService) {
		super();
		this.tagDao = tagDao;
		this.tareaTagsDao = tareaTagsDao;
		this.usuarioService = usuarioService;
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
	@Transactional
	public TagDto save(TagDto tagDto,Long authUserId) {
		
		Tag tag= (tagDto.getId()==null) ?
				new Tag() : 
					tagDao.findById(tagDto.getId())
					.orElseThrow(
							() -> new NoSuchElementException("Tag no encontrado"));
		
		if (tag.getId()==null && tagDao.existsByNameEquals(tagDto.getName())) {
			throw new IllegalStateException("TagName ya existente");
		}
		
		Usuario user =usuarioService.findByUserId(authUserId);
		
		tag.setName(tagDto.getName().toUpperCase());
		tag.setColor(tagDto.getColor());
		tag.setStatus(tagDto.getStatus());
		
		if (tag.getId()==null) {
			tag.setUsuarioCreacion(user.getUsername());
		}else {
			tag.setUsuarioModificacion(user.getUsername());
		}
		
		
		return new TagDto(tagDao.save(tag));
	}


	@Override
	public Tag getTagActiveById(Integer id) {

		Tag tag=tagDao.getTagActiveByIdOrName(id,null).orElseThrow(() -> new NoSuchElementException("Tag no encontrado"));
		

		
		return tag;
	}


	@Override
	@Transactional
	public void deleteTag(Integer id, Long userAuthId) {
		
		Tag tag = getTagActiveById(id);
		
		if (tag.getListTareaTags()!=null && tag.getListTareaTags().size()>0) {
			tareaTagsDao.deleteAll(tag.getListTareaTags());
		}
		
		Usuario user =usuarioService.findByUserId(userAuthId);
		
				
		tag.setStatus(Constants.STATUS_INACTIVE);
		tag.setUsuarioModificacion(user.getUsername());
		
		tagDao.save(tag);		
	}

}
