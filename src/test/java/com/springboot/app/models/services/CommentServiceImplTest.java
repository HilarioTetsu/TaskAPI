package com.springboot.app.models.services;

import static org.junit.jupiter.api.Assertions.assertEquals;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyShort;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.github.javafaker.Faker;
import com.springboot.app.models.dao.ICommentDao;
import com.springboot.app.models.dtos.CommentDto;
import com.springboot.app.models.entities.Comment;
import com.springboot.app.models.entities.Project;
import com.springboot.app.models.entities.Tarea;
import com.springboot.app.models.entities.Usuario;
import com.springboot.app.testdata.CommentDtoTestDataBuilder;
import com.springboot.app.testdata.ProjectTestDataBuilder;
import com.springboot.app.testdata.TareaTestDataBuilder;
import com.springboot.app.testdata.UsuarioTestDataBuilder;

@ExtendWith(MockitoExtension.class)
class CommentServiceImplTest {

	@InjectMocks
	private CommentServiceImpl commentService;

	@Mock
	private ITareaService tareaService;
	@Mock
	private IUsuarioService usuarioService;
	@Mock
	private IMediaService mediaService;
	@Mock
	private IProjectMemberService projectMemberService;
	@Mock
	private ICommentDao commentDao;

	private static final Faker faker = new Faker();

	@Test
	void saveComment_debeLanzarNoSuchElementException_siTareaNoExiste() {

		// Arrange

		CommentDto commentDto = new CommentDtoTestDataBuilder().build();

		Long authUserId = faker.number().numberBetween(1, Long.MAX_VALUE);

		when(tareaService.findByIdGuid(commentDto.getTareaId())).thenReturn(Optional.empty());

		// Act

		NoSuchElementException ex = assertThrows(NoSuchElementException.class,
				() -> commentService.saveComment(commentDto, authUserId));

		// Assert

		assertTrue(ex.getMessage().toLowerCase().contains("tarea no encontrada"));

		verify(tareaService, never()).isAsignedToThisTask(anyString(), anyLong());
		verify(projectMemberService, never()).canEditTasks(anyLong(), anyString());
		verify(usuarioService, never()).findByUserId(anyLong());
		verify(usuarioService,never()).findAllByIds(anyList());
		verify(commentDao, never()).save(any(Comment.class));

	}

	@Test
	void saveComment_debeLanzarSecurityException_siNoEstaAsignadoYNoPuedeEditarTareas() {

		// Arrange

		CommentDto commentDto = new CommentDtoTestDataBuilder().build();

		Tarea tarea = new TareaTestDataBuilder().withId(commentDto.getTareaId()).build();

		Long authUserId = faker.number().numberBetween(1, Long.MAX_VALUE);

		when(tareaService.findByIdGuid(commentDto.getTareaId())).thenReturn(Optional.of(tarea));

		when(tareaService.isAsignedToThisTask(tarea.getIdGuid(), authUserId)).thenReturn(false);
		
		when(projectMemberService.canEditTasks(authUserId, tarea.getProject().getIdGuid())).thenReturn(false);

		// Act

		SecurityException ex = assertThrows(SecurityException.class,
				() -> commentService.saveComment(commentDto, authUserId));

		// Assert

		assertTrue(ex.getMessage().toLowerCase().contains("no puedes comentar en esta tarea"));

		verify(tareaService).isAsignedToThisTask(eq(tarea.getIdGuid()), eq(authUserId));

		verify(projectMemberService).canEditTasks(eq(authUserId), eq(tarea.getProject().getIdGuid()));

		verify(usuarioService, never()).findByUserId(anyLong());
		
		verify(usuarioService,never()).findAllByIds(anyList());

		verify(commentDao, never()).save(any(Comment.class));

	}


	@Test
	void saveComment_debeLanzarNoSuchElementException_siUsuarioNoExiste() {

		// Arrange

		CommentDto commentDto = new CommentDtoTestDataBuilder().build();

		Tarea tarea = new TareaTestDataBuilder().withId(commentDto.getTareaId()).build();

		Long authUserId = faker.number().numberBetween(1, Long.MAX_VALUE);

		when(tareaService.findByIdGuid(commentDto.getTareaId())).thenReturn(Optional.of(tarea));

		when(tareaService.isAsignedToThisTask(tarea.getIdGuid(), authUserId)).thenReturn(true);

		
		when(usuarioService.findByUserId(authUserId)).thenThrow(new NoSuchElementException("Usuario no encontrado"));

		// Act

		NoSuchElementException ex = assertThrows(NoSuchElementException.class,
				() -> commentService.saveComment(commentDto, authUserId));

		// Assert

		assertTrue(ex.getMessage().toLowerCase().contains("usuario no encontrado"));

		verify(tareaService).isAsignedToThisTask(eq(tarea.getIdGuid()), eq(authUserId));

		verify(projectMemberService, never()).canEditTasks(anyLong(), anyString());
		
		verify(usuarioService,never()).findAllByIds(anyList());

		verify(commentDao, never()).save(any(Comment.class));

	}
	
	@Test
	void saveComment_debeGuardarComentario_siNoContieneMenciones() {

		// Arrange

		Long authUserId = faker.number().numberBetween(1, Long.MAX_VALUE);
		
		Usuario authUser = new UsuarioTestDataBuilder().withId(authUserId).build();
		
		CommentDto commentDto = new CommentDtoTestDataBuilder().build();
		
		Project project = new ProjectTestDataBuilder().build();

		Tarea tarea = new TareaTestDataBuilder().withId(commentDto.getTareaId())
				.withProject(project)
				.build();
		
		



		when(tareaService.findByIdGuid(commentDto.getTareaId())).thenReturn(Optional.of(tarea));

		when(tareaService.isAsignedToThisTask(tarea.getIdGuid(), authUserId)).thenReturn(false);

		when(projectMemberService.canEditTasks(authUserId, tarea.getProject().getIdGuid())).thenReturn(true);
		
		when(usuarioService.findByUserId(authUserId)).thenReturn(authUser);
		
		when(commentDao.save(any(Comment.class))).thenAnswer(inv -> {
			
			Comment toSave = inv.getArgument(0);
			
			toSave.setId(faker.number().numberBetween(1, Long.MAX_VALUE));
			
			return toSave;
			
			
		});
		
		ArgumentCaptor<Comment> commentCaptor= ArgumentCaptor.forClass(Comment.class);

		// Act

		CommentDto commentDtoSaved=commentService.saveComment(commentDto, authUserId);

		// Assert

		assertNotNull(commentDtoSaved);
		assertNotNull(commentDtoSaved.getId());
		assertEquals(commentDto.getBody(), commentDtoSaved.getBody());
		assertEquals(tarea.getIdGuid(), commentDtoSaved.getTareaId());
		assertEquals(authUserId, commentDtoSaved.getOwnerUserId());
		
		verify(commentDao).save(commentCaptor.capture());
		
		Comment commentSaved = commentCaptor.getValue();
		
		assertNotNull(commentSaved);
		assertEquals(commentDto.getBody(), commentSaved.getBody());
		assertSame(authUser, commentSaved.getAutor());
		assertSame(tarea, commentSaved.getTarea());
		assertNull(commentSaved.getMentions());
		

		verify(tareaService).isAsignedToThisTask(eq(tarea.getIdGuid()), eq(authUserId));

		verify(projectMemberService).canEditTasks(eq(authUserId), eq(tarea.getProject().getIdGuid()));

		verify(usuarioService).findByUserId(authUserId);
		
		verify(usuarioService,never()).findAllByIds(anyList());

		

	}
	
	
	@Test
	void saveComment_debeGuardarComentario_siContieneMenciones() {

		// Arrange

		Long authUserId = faker.number().numberBetween(1, Long.MAX_VALUE);
		
		Long mentionUserId = faker.number().numberBetween(1, Long.MAX_VALUE);
		
		Usuario authUser = new UsuarioTestDataBuilder().withId(authUserId).build();
		
		Usuario mentionedUser = new UsuarioTestDataBuilder().withId(mentionUserId).build();
		
		CommentDto commentDto = new CommentDtoTestDataBuilder()
				.withMentionsUserIds(List.of(mentionUserId))
				.build();
		
		Project project = new ProjectTestDataBuilder().build();

		Tarea tarea = new TareaTestDataBuilder().withId(commentDto.getTareaId())
				.withProject(project)
				.build();
		
		



		when(tareaService.findByIdGuid(commentDto.getTareaId())).thenReturn(Optional.of(tarea));

		when(tareaService.isAsignedToThisTask(tarea.getIdGuid(), authUserId)).thenReturn(false);

		when(projectMemberService.canEditTasks(authUserId, tarea.getProject().getIdGuid())).thenReturn(true);
		
		when(usuarioService.findByUserId(authUserId)).thenReturn(authUser);
		
		when(usuarioService.findAllByIds(anyList())).thenReturn(new ArrayList<>(List.of(mentionedUser)));
		
		when(commentDao.save(any(Comment.class))).thenAnswer(inv -> {
			
			Comment toSave = inv.getArgument(0);
			
			toSave.setId(faker.number().numberBetween(1, Long.MAX_VALUE));
			
			return toSave;
			
			
		});
		
		ArgumentCaptor<Comment> commentCaptor= ArgumentCaptor.forClass(Comment.class);

		// Act

		CommentDto commentDtoSaved=commentService.saveComment(commentDto, authUserId);

		// Assert

		assertNotNull(commentDtoSaved);
		assertNotNull(commentDtoSaved.getId());
		assertNotNull(commentDtoSaved.getMentionsUserIds());
		assertEquals(1, commentDtoSaved.getMentionsUserIds().size());
		
		assertEquals(commentDto.getBody(), commentDtoSaved.getBody());
		assertEquals(tarea.getIdGuid(), commentDtoSaved.getTareaId());
		assertEquals(authUserId, commentDtoSaved.getOwnerUserId());
		
		verify(commentDao).save(commentCaptor.capture());
		
		Comment commentSaved = commentCaptor.getValue();
		
		assertNotNull(commentSaved);
		
		assertNotNull(commentSaved.getMentions());
		assertEquals(1, commentSaved.getMentions().size());

		
		assertEquals(commentDto.getBody(), commentSaved.getBody());
		assertSame(authUser, commentSaved.getAutor());
		assertSame(tarea, commentSaved.getTarea());
		
		

		verify(tareaService).isAsignedToThisTask(eq(tarea.getIdGuid()), eq(authUserId));

		verify(projectMemberService).canEditTasks(eq(authUserId), eq(tarea.getProject().getIdGuid()));

		verify(usuarioService).findByUserId(authUserId);

		

	}
	
	@Test
	void saveComment_debeGuardarComentario_siauthUserEnMenciones() {

		// Arrange

		Long authUserId = faker.number().numberBetween(1, Long.MAX_VALUE);
		
		Long mentionUserId = faker.number().numberBetween(1, Long.MAX_VALUE);
		
		Usuario authUser = new UsuarioTestDataBuilder().withId(authUserId).build();
		
		Usuario mentionedUser = new UsuarioTestDataBuilder().withId(mentionUserId).build();
		
		CommentDto commentDto = new CommentDtoTestDataBuilder()
				.withMentionsUserIds(List.of(mentionUserId,authUserId))
				.build();
		
		Project project = new ProjectTestDataBuilder().build();

		Tarea tarea = new TareaTestDataBuilder().withId(commentDto.getTareaId())
				.withProject(project)
				.build();
		
		



		when(tareaService.findByIdGuid(commentDto.getTareaId())).thenReturn(Optional.of(tarea));

		when(tareaService.isAsignedToThisTask(tarea.getIdGuid(), authUserId)).thenReturn(false);

		when(projectMemberService.canEditTasks(authUserId, tarea.getProject().getIdGuid())).thenReturn(true);
		
		when(usuarioService.findByUserId(authUserId)).thenReturn(authUser);
		
		when(usuarioService.findAllByIds(anyList())).thenReturn(new ArrayList<>(List.of(mentionedUser,authUser)));
		
		when(commentDao.save(any(Comment.class))).thenAnswer(inv -> {
			
			Comment toSave = inv.getArgument(0);
			
			toSave.setId(faker.number().numberBetween(1, Long.MAX_VALUE));
			
			return toSave;
			
			
		});
		
		ArgumentCaptor<Comment> commentCaptor= ArgumentCaptor.forClass(Comment.class);

		// Act

		CommentDto commentDtoSaved=commentService.saveComment(commentDto, authUserId);

		// Assert

		assertNotNull(commentDtoSaved);
		assertNotNull(commentDtoSaved.getId());
		assertNotNull(commentDtoSaved.getMentionsUserIds());
		assertEquals(1, commentDtoSaved.getMentionsUserIds().size());
		
		assertEquals(commentDto.getBody(), commentDtoSaved.getBody());
		assertEquals(tarea.getIdGuid(), commentDtoSaved.getTareaId());
		assertEquals(authUserId, commentDtoSaved.getOwnerUserId());
		
		verify(commentDao).save(commentCaptor.capture());
		
		Comment commentSaved = commentCaptor.getValue();
		
		assertNotNull(commentSaved);
		
		assertNotNull(commentSaved.getMentions());
		assertEquals(1, commentSaved.getMentions().size());

		
		assertEquals(commentDto.getBody(), commentSaved.getBody());
		assertSame(authUser, commentSaved.getAutor());
		assertSame(tarea, commentSaved.getTarea());
		
		

		verify(tareaService).isAsignedToThisTask(eq(tarea.getIdGuid()), eq(authUserId));

		verify(projectMemberService).canEditTasks(eq(authUserId), eq(tarea.getProject().getIdGuid()));

		verify(usuarioService).findByUserId(authUserId);

		

	}

}
