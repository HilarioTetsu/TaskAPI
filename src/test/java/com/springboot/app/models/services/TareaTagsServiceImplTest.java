package com.springboot.app.models.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.github.javafaker.Faker;
import com.springboot.app.models.dao.ITareaTagsDao;
import com.springboot.app.models.dtos.TagDto;
import com.springboot.app.models.entities.Tag;
import com.springboot.app.models.entities.Tarea;
import com.springboot.app.models.entities.TareaTags;
import com.springboot.app.models.entities.Usuario;
import com.springboot.app.testdata.TagTestDataBuilder;
import com.springboot.app.testdata.TareaTestDataBuilder;
import com.springboot.app.testdata.UsuarioTestDataBuilder;

@ExtendWith(MockitoExtension.class)
public class TareaTagsServiceImplTest {

	@InjectMocks
	private TareaTagsServiceImpl tareaTagsService;
	
	@Mock
	private ITareaService tareaService;
	@Mock
	private IProjectMemberService projectMemberService;
	@Mock
	private ITagService tagService;
	@Mock
	private ITareaTagsDao tareaTagsDao;
	@Mock
	private IUsuarioService usuarioService;
	
	private static final Faker faker = new Faker();
	
	
	@Test
	void asignarTag_debeAsignarTag_SiEsDueñoDeLaTareaYTagNoEstaAsociadoyHayProyecto() {
		//Arrange
		
		String tareaId = UUID.randomUUID().toString();
		
		Integer tagId = faker.number().numberBetween(1, Integer.MAX_VALUE);
				
		Long authUserId = faker.number().randomNumber();
		
		Usuario authUser = new UsuarioTestDataBuilder().withId(authUserId).build();
		
		Tarea tarea = new TareaTestDataBuilder().withId(tareaId)
				.withOwner(authUser)
				.build();
		
		Tag tag = new TagTestDataBuilder().withId(tagId).build();
		
		
		
		when( tareaService.findByIdGuid(tareaId) ).thenReturn(Optional.of(tarea));
		
		when(projectMemberService.isOwner(authUserId, tarea.getProject().getIdGuid())).thenReturn(false);
		
		when(tagService.getTagActiveById(tagId)).thenReturn(tag);
		
		when(usuarioService.findUsernameById(authUserId)).thenReturn(authUser.getUsername());
		
		when(tareaTagsDao.saveAndFlush(any(TareaTags.class))).thenAnswer(inv -> {
			
			TareaTags tareaTags = inv.getArgument(0);
			
			tareaTags.setId(faker.number().numberBetween(1, Integer.MAX_VALUE));
			
			tarea.setTareaTagsList(List.of(tareaTags));
			
			return tareaTags;
			
		});
		
		when(tareaService.getTagsFromTarea(tarea.getIdGuid())).thenReturn(List.of(tag));
		
		ArgumentCaptor<TareaTags> tareaTagsCaptor = ArgumentCaptor.forClass(TareaTags.class);
		
		//Act
		
		List<TagDto> result = tareaTagsService.asignarTag(tareaId, tagId, authUserId);
		
		//Assert
		
		assertNotNull(result);
		assertEquals(1, result.size());
		assertTrue(result.stream().anyMatch(dto -> dto.getName().equals(tag.getName())),"Nombre no coincide");
		assertTrue(result.stream().anyMatch(dto -> dto.getColor().equals(tag.getColor())),"Color no coincide");
		
		
		verify(tareaTagsDao).saveAndFlush(tareaTagsCaptor.capture());
		
		TareaTags tareaTagSaved = tareaTagsCaptor.getValue();
		
		assertSame(tag,tareaTagSaved.getTag());
		assertSame(tarea,tareaTagSaved.getTarea());
		assertEquals(authUser.getUsername(), tareaTagSaved.getUsuarioCreacion());
		
		assertEquals(result.size(),tareaTagSaved.getTarea().getTareaTagsList().size());
		
		verify(projectMemberService).isOwner(eq(authUserId), eq(tarea.getProject().getIdGuid()));
		verify(usuarioService).findUsernameById(authUserId);
		verify(tagService).getTagActiveById(tagId);
		
	}
	
	@Test
	void asignarTag_debeAsignarTag_SiEsDueñoDeLaTareaYTagNoEstaAsociadoyProyectoEsNull() {
		//Arrange
		
		String tareaId = UUID.randomUUID().toString();
		
		Integer tagId = faker.number().numberBetween(1, Integer.MAX_VALUE);
				
		Long authUserId = faker.number().randomNumber();
		
		Usuario authUser = new UsuarioTestDataBuilder().withId(authUserId).build();
		
		Tarea tarea = new TareaTestDataBuilder().withId(tareaId)
				.withProject(null)
				.withOwner(authUser)
				.build();
		
		Tag tag = new TagTestDataBuilder().withId(tagId).build();
		
		
		
		when( tareaService.findByIdGuid(tareaId) ).thenReturn(Optional.of(tarea));
				
		
		when(tagService.getTagActiveById(tagId)).thenReturn(tag);
		
		when(usuarioService.findUsernameById(authUserId)).thenReturn(authUser.getUsername());
		
		when(tareaTagsDao.saveAndFlush(any(TareaTags.class))).thenAnswer(inv -> {
			
			TareaTags tareaTags = inv.getArgument(0);
			
			tareaTags.setId(faker.number().numberBetween(1, Integer.MAX_VALUE));
			
			tarea.setTareaTagsList(List.of(tareaTags));
			
			return tareaTags;
			
		});
		
		when(tareaService.getTagsFromTarea(tarea.getIdGuid())).thenReturn(List.of(tag));
		
		ArgumentCaptor<TareaTags> tareaTagsCaptor = ArgumentCaptor.forClass(TareaTags.class);
		
		//Act
		
		List<TagDto> result = tareaTagsService.asignarTag(tareaId, tagId, authUserId);
		
		//Assert
		
		assertNotNull(result);
		assertEquals(1, result.size());
		assertTrue(result.stream().anyMatch(dto -> dto.getName().equals(tag.getName())),"Nombre no coincide");
		assertTrue(result.stream().anyMatch(dto -> dto.getColor().equals(tag.getColor())),"Color no coincide");
		
		
		verify(tareaTagsDao).saveAndFlush(tareaTagsCaptor.capture());
		
		TareaTags tareaTagSaved = tareaTagsCaptor.getValue();
		
		assertSame(tag,tareaTagSaved.getTag());
		assertSame(tarea,tareaTagSaved.getTarea());
		assertEquals(authUser.getUsername(), tareaTagSaved.getUsuarioCreacion());
		
		assertEquals(result.size(),tareaTagSaved.getTarea().getTareaTagsList().size());
		
		verify(projectMemberService,never()).isOwner(anyLong(),anyString() );
		
		verify(usuarioService).findUsernameById(authUserId);
		verify(tagService).getTagActiveById(tagId);
		
	}
	
	
}
