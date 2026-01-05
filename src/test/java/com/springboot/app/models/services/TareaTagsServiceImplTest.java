package com.springboot.app.models.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import com.github.javafaker.Faker;
import com.springboot.app.models.dao.ITareaTagsDao;
import com.springboot.app.models.dtos.TagDto;
import com.springboot.app.models.entities.Project;
import com.springboot.app.models.entities.Tag;
import com.springboot.app.models.entities.Tarea;
import com.springboot.app.models.entities.TareaTags;
import com.springboot.app.models.entities.Usuario;
import com.springboot.app.testdata.ProjectTestDataBuilder;
import com.springboot.app.testdata.TagTestDataBuilder;
import com.springboot.app.testdata.TareaTagsTestDataBuilder;
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
	void asignarTag_debeAsignarTag_SiEsDueñoDeLaTareaYTagNoEstaAsociadoYHayProyecto() {
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
	void asignarTag_debeAsignarTag_SiEsDueñoDeLaTareaYTagNoEstaAsociadoYProyectoEsNull() {
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
	
	
	@Test
	void asignarTag_debeAsignarTag_SiEsDueñoDelProyectoYTagNoEstaAsociado() {
		//Arrange
		
		String tareaId = UUID.randomUUID().toString();
		
		Integer tagId = faker.number().numberBetween(1, Integer.MAX_VALUE);
				
		Long authUserId = faker.number().randomNumber();
		
		Usuario authUser = new UsuarioTestDataBuilder().withId(authUserId).build();
		
		Project project = new ProjectTestDataBuilder().withOwner(authUser).build();
		
		Tarea tarea = new TareaTestDataBuilder().withId(tareaId)
				.withProject(project)
				.build();
		
		Tag tag = new TagTestDataBuilder().withId(tagId).build();
		
		
		
		when( tareaService.findByIdGuid(tareaId) ).thenReturn(Optional.of(tarea));
		
		when(projectMemberService.isOwner(authUserId, tarea.getProject().getIdGuid())).thenReturn(true);
		
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
	void asignarTag_debeLanzarNoSuchException_SiTareaNoExiste() {
		//Arrange
		
		String tareaId = UUID.randomUUID().toString();
		
		Integer tagId = faker.number().numberBetween(1, Integer.MAX_VALUE);
				
		Long authUserId = faker.number().randomNumber();
		
		
		
		
		
		when( tareaService.findByIdGuid(tareaId) ).thenReturn(Optional.empty());
		
		
		
		//Act
		
		NoSuchElementException ex = assertThrows(NoSuchElementException.class, () -> tareaTagsService.asignarTag(tareaId, tagId, authUserId));
		
		//Assert
		
		assertTrue(ex.getMessage().toLowerCase().contains("tarea no encontrada"));
		
		verify(projectMemberService,never()).isOwner(anyLong(), anyString());
		
		verify(tagService,never()).getTagActiveById(anyInt());
		
		verify(usuarioService,never()).findUsernameById(anyLong());
		
		verify(tareaTagsDao,never()).saveAndFlush(any(TareaTags.class));
		
		verify(tareaService,never()).getTagsFromTarea(anyString());
		
	}
	
	@Test
	void asignarTag_debeLanzarAccessDeniedException_SiProyectoEsNullYNoEsDueñoDeLaTarea() {
		//Arrange
		
		String tareaId = UUID.randomUUID().toString();
		
		Integer tagId = faker.number().numberBetween(1, Integer.MAX_VALUE);
				
		Long authUserId = faker.number().randomNumber();
				
		
		Tarea tarea = new TareaTestDataBuilder().withId(tareaId)
				.withProject(null)				
				.build();
		
		
		
		when( tareaService.findByIdGuid(tareaId) ).thenReturn(Optional.of(tarea));
		
		
		
		//Act
		
		AccessDeniedException ex = assertThrows(AccessDeniedException.class, () -> tareaTagsService.asignarTag(tareaId, tagId, authUserId));
		
		//Assert
		
		assertTrue(ex.getMessage().toLowerCase().contains("no tienes los permisos para realizar esta accion"));
		
		verify(projectMemberService,never()).isOwner(anyLong(), anyString());
		
		verify(tagService,never()).getTagActiveById(anyInt());
		
		verify(usuarioService,never()).findUsernameById(anyLong());
		
		verify(tareaTagsDao,never()).saveAndFlush(any(TareaTags.class));
		
		verify(tareaService,never()).getTagsFromTarea(anyString());
		
	}
	
	@Test
	void asignarTag_debeLanzarAccessDeniedException_SiNoEsDueñoDeLaTareaNiProyecto() {
		//Arrange
		
		String tareaId = UUID.randomUUID().toString();
		
		Integer tagId = faker.number().numberBetween(1, Integer.MAX_VALUE);
				
		Long authUserId = faker.number().randomNumber();
		
		Usuario authUser = new UsuarioTestDataBuilder().withId(authUserId).build();
				
		
		Tarea tarea = new TareaTestDataBuilder().withId(tareaId)					
				.build();
		
		
		
		when( tareaService.findByIdGuid(tareaId) ).thenReturn(Optional.of(tarea));
		
		when(projectMemberService.isOwner(authUserId, tarea.getProject().getIdGuid())).thenReturn(false);
		
		
		//Act
		
		AccessDeniedException ex = assertThrows(AccessDeniedException.class, () -> tareaTagsService.asignarTag(tareaId, tagId, authUserId));
		
		//Assert
		
		assertTrue(ex.getMessage().toLowerCase().contains("no tienes los permisos para realizar esta accion"));
		
		verify(projectMemberService).isOwner(eq(authUserId), eq(tarea.getProject().getIdGuid()));
		
		verify(tagService,never()).getTagActiveById(anyInt());
		
		verify(usuarioService,never()).findUsernameById(anyLong());
		
		verify(tareaTagsDao,never()).saveAndFlush(any(TareaTags.class));
		
		verify(tareaService,never()).getTagsFromTarea(anyString());
		
	}
	
	@Test
	void asignarTag_debeLanzarIllegalStateException_SiTagYaEstaAsociadoALaTarea() {
		//Arrange
		
		String tareaId = UUID.randomUUID().toString();
		
		Integer tagId = faker.number().numberBetween(1, Integer.MAX_VALUE);
				
		Long authUserId = faker.number().randomNumber();
		
		Usuario authUser = new UsuarioTestDataBuilder().withId(authUserId).build();
		
		Project project = new ProjectTestDataBuilder().withOwner(authUser).build();
		
		Tarea tarea = new TareaTestDataBuilder().withId(tareaId)
				.withProject(project)
				.build();
		
		Tag tag = new TagTestDataBuilder().withId(tagId).build();
		
		
		TareaTags tareaTag = new TareaTagsTestDataBuilder().withTag(tag).withTarea(tarea).build();
		
		tarea.setTareaTagsList(List.of(tareaTag));
		
		
		when( tareaService.findByIdGuid(tareaId) ).thenReturn(Optional.of(tarea));
		
		when(projectMemberService.isOwner(authUserId, tarea.getProject().getIdGuid())).thenReturn(true);
		
		when(tagService.getTagActiveById(tagId)).thenReturn(tag);
		
		
		//Act
		
		IllegalStateException ex = assertThrows(IllegalStateException.class, () -> tareaTagsService.asignarTag(tareaId, tagId, authUserId));
		
		//Assert
		
		assertTrue(ex.getMessage().toLowerCase().contains("tag ya asociado a la tarea"));
		
		verify(projectMemberService).isOwner(eq(authUserId), eq(tarea.getProject().getIdGuid()));
		
		verify(tagService).getTagActiveById(tagId);
		
		verify(usuarioService,never()).findUsernameById(anyLong());
		
		verify(tareaTagsDao,never()).saveAndFlush(any(TareaTags.class));
		
		verify(tareaService,never()).getTagsFromTarea(anyString());
		
	}
	
	
}
