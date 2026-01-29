package com.springboot.app.models.services;

import static org.junit.jupiter.api.Assertions.assertEquals;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.github.javafaker.Faker;
import com.springboot.app.models.dao.ICommentDao;
import com.springboot.app.models.dtos.CommentDto;
import com.springboot.app.models.dtos.CommentUpdateDto;
import com.springboot.app.models.dtos.CommentViewDto;
import com.springboot.app.models.entities.Comment;
import com.springboot.app.models.entities.Media;
import com.springboot.app.models.entities.Project;
import com.springboot.app.models.entities.Tarea;
import com.springboot.app.models.entities.Usuario;
import com.springboot.app.testdata.CommentDtoTestDataBuilder;
import com.springboot.app.testdata.CommentTestDataBuilder;
import com.springboot.app.testdata.CommentUpdateDtoTestDataBuilder;
import com.springboot.app.testdata.MediaTestDataBuilder;
import com.springboot.app.testdata.ProjectTestDataBuilder;
import com.springboot.app.testdata.TareaTestDataBuilder;
import com.springboot.app.testdata.UsuarioTestDataBuilder;
import com.springboot.app.utils.Constants;

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
	void saveComment_debeGuardarComentario_siAuthUserEnMenciones() {

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
	
	@Test
    void saveComment_debeLanzarSecurityException_cuandoUsuarioNoEstaAsignadoATarea() {
        // Arrange
        Long authUserId = faker.number().randomNumber();
        CommentDto dto = new CommentDtoTestDataBuilder().build();
        List<Media> medias = Collections.emptyList();

        when(tareaService.isAsignedToThisTask(dto.getTareaId(), authUserId)).thenReturn(false);

        // Act & Assert
        assertThrows(SecurityException.class, () -> {
            commentService.saveComment(dto, medias, authUserId);
        });

        verifyNoInteractions(commentDao);
    }
	
	@Test
    void saveComment_debeLanzarNoSuchElementException_CuandoTareaNoExiste() {
        // Arrange
        Long authUserId = faker.number().randomNumber();
        CommentDto dto = new CommentDtoTestDataBuilder().build();
        List<Media> medias = Collections.emptyList();

        when(tareaService.isAsignedToThisTask(dto.getTareaId(), authUserId)).thenReturn(true);
        when(usuarioService.findByUserId(authUserId)).thenReturn(new Usuario());
        when(tareaService.findByIdGuid(dto.getTareaId())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> {
            commentService.saveComment(dto, medias, authUserId);
        });

        verifyNoInteractions(commentDao);
    }
	
	@Test
    void saveComment_debeGuardarComentario_cuandoDatosSonValidos() {
        // Arrange
        Long authUserId = 100L;
        String tareaId = UUID.randomUUID().toString();
        
        CommentDto dto = new CommentDtoTestDataBuilder()
                .withTareaId(tareaId)
                .withMentionsUserIds(null)
                .build();
        
      
        Media foto1 = new MediaTestDataBuilder()
        		.withOwnerId(authUserId)
        		.build();
        
        Media foto2 = new MediaTestDataBuilder()
        		.withOwnerId(authUserId)
        		.build();
        
        List<Media> medias = List.of(foto1, foto2);
       

        Usuario autor = new UsuarioTestDataBuilder().withId(authUserId).build();
        Tarea tarea = new TareaTestDataBuilder().withId(tareaId).build();

        when(tareaService.isAsignedToThisTask(tareaId, authUserId)).thenReturn(true);
        when(usuarioService.findByUserId(authUserId)).thenReturn(autor);
        when(tareaService.findByIdGuid(tareaId)).thenReturn(Optional.of(tarea));
        
        when(commentDao.save(any(Comment.class))).thenAnswer(inv -> {
            Comment c = inv.getArgument(0);
            c.setId(1L); 
            return c;
        });

        ArgumentCaptor<Comment> commentCaptor = ArgumentCaptor.forClass(Comment.class);

        // Act
        CommentDto result = commentService.saveComment(dto, medias, authUserId);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getConfirmMediasStorageKeyId());
        assertEquals(authUserId, result.getOwnerUserId());
        assertEquals(tareaId, result.getTareaId());
        assertEquals(2, result.getConfirmMediasStorageKeyId().size());
        
        verify(commentDao).save(commentCaptor.capture());
        Comment commentGuardado = commentCaptor.getValue();

        assertSame(autor, commentGuardado.getAutor());
        assertSame(tarea, commentGuardado.getTarea());
        
        
        assertNotNull(commentGuardado.getAdjuntos());
        assertEquals(2, commentGuardado.getAdjuntos().size());
        assertSame(foto1, commentGuardado.getAdjuntos().get(0));
    }
	
	@Test
    void saveComment_debeFiltrarAutoMenciones_cuandoUsuarioSeMencionaASiMismo() {
        // Arrange
        Long authUserId = 50L;
        Long otherUserId = 60L;

        Usuario autor = new UsuarioTestDataBuilder().withId(authUserId).build();
        Usuario otroUsuario = new UsuarioTestDataBuilder().withId(otherUserId).build();

        CommentDto dto = new CommentDtoTestDataBuilder()
                .withMentionsUserIds(List.of(authUserId, otherUserId))
                .build();

        List<Usuario> usuariosEncontrados = new ArrayList<>(List.of(autor, otroUsuario));

        when(tareaService.isAsignedToThisTask(anyString(), eq(authUserId))).thenReturn(true);
        when(usuarioService.findByUserId(authUserId)).thenReturn(autor);
        when(tareaService.findByIdGuid(dto.getTareaId())).thenReturn(Optional.of(new Tarea()));
        when(usuarioService.findAllByIds(dto.getMentionsUserIds())).thenReturn(usuariosEncontrados);

        when(commentDao.save(any(Comment.class))).thenAnswer(i -> i.getArgument(0));

        ArgumentCaptor<Comment> commentCaptor = ArgumentCaptor.forClass(Comment.class);

        // Act
        commentService.saveComment(dto, Collections.emptyList(), authUserId);

        // Assert
        verify(commentDao).save(commentCaptor.capture());
        Comment commentGuardado = commentCaptor.getValue();

        assertEquals(1, commentGuardado.getMentions().size());
        assertTrue(commentGuardado.getMentions().stream().anyMatch(m -> m.getId().equals(otherUserId)));
    }
	
	
	@Test
	void getAll_debeRetornarPaginaVacia_siUsuarioNoTieneComentarios() {
		// Arrange
		int pagina = 0;
		int tamanio = 10;
		Long authUserId=faker.number().randomNumber();
		String sorts = "fecha_creacion,desc;";
		

		Page<Comment> pageComments = Page.empty();
		
		when(commentDao.findAllByUserId(any(Pageable.class), eq(authUserId))).thenReturn(pageComments);
						
		//Act
		
		Page<CommentViewDto> result = commentService.getAll(pagina, tamanio, sorts, authUserId);
		
		
		//Assert
		assertNotNull(result);
		assertTrue(result.isEmpty());
		assertEquals(0, result.getTotalElements());
		
		
		verifyNoInteractions(mediaService);
		
	}
	
	
	
	@Test
	void getAll_debeRetornarComentarios_siIgnoraAdjuntosSinStatusReady() {
		// Arrange
		int pagina = 0;
		int tamanio = 10;
		Long authUserId=faker.number().randomNumber();
		String sorts = "fecha_creacion,desc;";
		
		
		Usuario authUser = new UsuarioTestDataBuilder().withId(authUserId).build();
		
		 Media foto1 = new MediaTestDataBuilder()
	        		.withOwnerId(authUserId)
	        		.withStatus(Constants.STATUS_PENDING)
	        		.build();
		 		
        Comment comment = new CommentTestDataBuilder()
        				.withAutor(authUser)
        				.withAdjuntos(new ArrayList<>(List.of(foto1)))
        				.build();
        
        
      
       

		Page<Comment> pageComments = new PageImpl<>(new ArrayList<>(List.of(comment)));
		
		when(commentDao.findAllByUserId(any(Pageable.class), eq(authUserId))).thenReturn(pageComments);
						
		
		
		ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
		
		//Act
		
		Page<CommentViewDto> result = commentService.getAll(pagina, tamanio, sorts, authUserId);
		
		
		//Assert
		assertNotNull(result);
		assertEquals(1, result.getTotalElements());
		
		CommentViewDto dto = result.getContent().get(0);
	
		
		assertEquals(comment.getId(), dto.getId());
		assertEquals(comment.getBody(), dto.getBody());
		assertEquals(comment.getAutor().getId(), dto.getOwnerUserId());
		assertNull(dto.getConfirmMediasStorageKeyUrls());
		

		verify(commentDao).findAllByUserId(pageableCaptor.capture(), eq(authUserId));
		
		Pageable pageableUsed = pageableCaptor.getValue();
		
		assertEquals(pagina, pageableUsed.getPageNumber());
		assertEquals(tamanio, pageableUsed.getPageSize());
		assertEquals(1, pageableUsed.getSort().toList().size());
		
		verify(mediaService,never()).createPresignedGetUrls(anyList());

		
	}
	
	
	@Test
	void getAll_debeRetornarComentarios_cuandoSonComentariosMultiples() {
		// Arrange
		int pagina = 0;
		int tamanio = 10;
		Long authUserId=faker.number().randomNumber();
		String sorts = "fecha_creacion,desc;";
		
		
		Usuario authUser = new UsuarioTestDataBuilder().withId(authUserId).build();
		
		Media foto1 = new MediaTestDataBuilder()
	        		.withOwnerId(authUserId)
	        		.withStatus(Constants.STATUS_READY)
	        		.build();
		 		
        Comment comment = new CommentTestDataBuilder()
        				.withAutor(authUser)
        				.withAdjuntos(new ArrayList<>(List.of(foto1)))
        				.build();
        
        Comment comment2 = new CommentTestDataBuilder()
				.withAutor(authUser)				
				.build();
        
      
       

		Page<Comment> pageComments = new PageImpl<>(new ArrayList<>(List.of(comment,comment2)));
		
		when(commentDao.findAllByUserId(any(Pageable.class), eq(authUserId))).thenReturn(pageComments);
						
		when(mediaService.createPresignedGetUrls(anyList())).thenReturn(List.of(faker.internet().url()));
		
		ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
		
		//Act
		
		Page<CommentViewDto> result = commentService.getAll(pagina, tamanio, sorts, authUserId);
		
		
		//Assert
		assertNotNull(result);
		assertEquals(2, result.getTotalElements());
		
		CommentViewDto dto1 = result.getContent().get(0);
		CommentViewDto dto2 = result.getContent().get(1);
		
		assertEquals(comment.getId(), dto1.getId());
		assertEquals(comment.getBody(), dto1.getBody());
		assertEquals(comment.getAutor().getId(), dto1.getOwnerUserId());
		assertEquals(comment.getAdjuntos().size(), dto1.getConfirmMediasStorageKeyUrls().size());
		
		assertEquals(comment2.getId(), dto2.getId());
		assertEquals(comment2.getBody(), dto2.getBody());
		assertEquals(comment2.getAutor().getId(), dto2.getOwnerUserId());
		assertNull(comment2.getAdjuntos());
		assertNull(dto2.getConfirmMediasStorageKeyUrls());
		

		
		verify(commentDao).findAllByUserId(pageableCaptor.capture(), eq(authUserId));
		
		Pageable pageableUsed = pageableCaptor.getValue();
		
		assertEquals(pagina, pageableUsed.getPageNumber());
		assertEquals(tamanio, pageableUsed.getPageSize());
		assertEquals(1, pageableUsed.getSort().toList().size());
		
		verify(mediaService).createPresignedGetUrls(anyList());

		
	}
	
	@Test
	void getAllByTareaId_debeLanzarNoSuchElementException_siTareaNoExiste() {
		
		//Arrange
		int pagina = 0;
		
		int tamanio = 10;
		
		Long authUserId=faker.number().randomNumber();
		
		String tareaId=UUID.randomUUID().toString();
		
		String sorts = "fecha_creacion,desc;";
		
		when(tareaService.findByIdGuid(tareaId)).thenReturn(Optional.empty());
		
		//Act
		
		NoSuchElementException ex = assertThrows(NoSuchElementException.class, () -> commentService.getAllByTareaId(pagina, tamanio, sorts, authUserId, tareaId));
		
		assertTrue(ex.getMessage().toLowerCase().contains("tarea no encontrada"));
		
		verify(tareaService,never()).isAsignedToThisTask(anyString(), anyLong());
		verifyNoInteractions(projectMemberService);
		verifyNoInteractions(mediaService);
		verifyNoInteractions(commentDao);
		
	}
	
	@Test
	void getAllByTareaId_debeLanzarSecurityException_siNoCumpleValidaciones() {
		
		//Arrange
		int pagina = 0;
		
		int tamanio = 10;
		
		Long authUserId=faker.number().randomNumber();
		
		String tareaId=UUID.randomUUID().toString();
		
		String sorts = "fecha_creacion,desc;";
		
		
		Tarea tarea = new TareaTestDataBuilder().withId(tareaId).build();
		
		when(tareaService.findByIdGuid(tareaId)).thenReturn(Optional.of(tarea));
		when(tareaService.isAsignedToThisTask(tareaId, authUserId)).thenReturn(false);
		when(projectMemberService.isMember(authUserId, tarea.getProject().getIdGuid())).thenReturn(false);
		
		//Act
		
		SecurityException ex = assertThrows(SecurityException.class, () -> commentService.getAllByTareaId(pagina, tamanio, sorts, authUserId, tareaId));
		
		assertTrue(ex.getMessage().toLowerCase().contains("no tienes los permisos necesarios para esta tarea"));
		
		verify(tareaService).isAsignedToThisTask(tareaId, authUserId);
		
		verify(projectMemberService).isMember(authUserId, tarea.getProject().getIdGuid());
		
		verifyNoInteractions(mediaService);
		verifyNoInteractions(commentDao);
		
	}
	
	@Test
	void getAllByTareaId_debeRetornarComentarios_siCumpleValidaciones() {

		// Arrange
		int pagina = 0;

		int tamanio = 10;

		Long authUserId = faker.number().randomNumber();

		String tareaId = UUID.randomUUID().toString();

		String sorts = "fecha_creacion,desc;";

		Usuario authUser = new UsuarioTestDataBuilder().withId(authUserId).build();

		Tarea tarea = new TareaTestDataBuilder().withId(tareaId).withOwner(authUser).build();

		Media foto1 = new MediaTestDataBuilder().withOwnerId(authUserId).withStatus(Constants.STATUS_READY).build();

		Comment comment = new CommentTestDataBuilder().withAutor(authUser).withAdjuntos(new ArrayList<>(List.of(foto1)))
				.build();

		Comment comment2 = new CommentTestDataBuilder().withAutor(authUser).build();

		Page<Comment> pageComments = new PageImpl<>(new ArrayList<>(List.of(comment, comment2)));

		when(tareaService.findByIdGuid(tareaId)).thenReturn(Optional.of(tarea));
		
		when(tareaService.isAsignedToThisTask(tareaId, authUserId)).thenReturn(false);
		
		when(projectMemberService.isMember(authUserId, tarea.getProject().getIdGuid())).thenReturn(true);
		
		when(commentDao.findAllByTareaId(any(Pageable.class), eq(tareaId))).thenReturn(pageComments);
		
		when(mediaService.createPresignedGetUrls(anyList())).thenReturn(List.of(faker.internet().url()));
		
		ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

		// Act

		Page<CommentViewDto> result = commentService.getAllByTareaId(pagina, tamanio, sorts, authUserId, tareaId);

		

		verify(tareaService).isAsignedToThisTask(tareaId, authUserId);

		verify(projectMemberService).isMember(authUserId, tarea.getProject().getIdGuid());
		
		
		assertNotNull(result);
		assertEquals(2, result.getTotalElements());

		CommentViewDto dto1 = result.getContent().get(0);
		CommentViewDto dto2 = result.getContent().get(1);

		assertEquals(comment.getId(), dto1.getId());
		assertEquals(comment.getBody(), dto1.getBody());
		assertEquals(comment.getAutor().getId(), dto1.getOwnerUserId());
		assertEquals(comment.getAdjuntos().size(), dto1.getConfirmMediasStorageKeyUrls().size());

		assertEquals(comment2.getId(), dto2.getId());
		assertEquals(comment2.getBody(), dto2.getBody());
		assertEquals(comment2.getAutor().getId(), dto2.getOwnerUserId());
		assertNull(comment2.getAdjuntos());
		assertNull(dto2.getConfirmMediasStorageKeyUrls());

		verify(commentDao).findAllByTareaId(pageableCaptor.capture(), eq(tareaId));

		Pageable pageableUsed = pageableCaptor.getValue();

		assertEquals(pagina, pageableUsed.getPageNumber());
		assertEquals(tamanio, pageableUsed.getPageSize());
		assertEquals(1, pageableUsed.getSort().toList().size());

		verify(mediaService).createPresignedGetUrls(anyList());

		
	}
	
	
	@Test
	void getAllByTareaId_debeRetornarPaginaVacia_siUsuarioNoTieneComentarios() {

		// Arrange
		int pagina = 0;

		int tamanio = 10;

		Long authUserId = faker.number().randomNumber();

		String tareaId = UUID.randomUUID().toString();

		String sorts = "fecha_creacion,desc;";

		Usuario authUser = new UsuarioTestDataBuilder().withId(authUserId).build();

		Tarea tarea = new TareaTestDataBuilder().withId(tareaId).withOwner(authUser).build();

		

		Page<Comment> pageComments = Page.empty();

		when(tareaService.findByIdGuid(tareaId)).thenReturn(Optional.of(tarea));
		
		when(tareaService.isAsignedToThisTask(tareaId, authUserId)).thenReturn(false);
		
		when(projectMemberService.isMember(authUserId, tarea.getProject().getIdGuid())).thenReturn(true);
		
		when(commentDao.findAllByTareaId(any(Pageable.class), eq(tareaId))).thenReturn(pageComments);
				
		

		// Act

		Page<CommentViewDto> result = commentService.getAllByTareaId(pagina, tamanio, sorts, authUserId, tareaId);

		

		verify(tareaService).isAsignedToThisTask(tareaId, authUserId);

		verify(projectMemberService).isMember(authUserId, tarea.getProject().getIdGuid());
		
		
		assertNotNull(result);
		assertTrue(result.isEmpty());
		assertEquals(0, result.getTotalElements());


		verifyNoInteractions(mediaService);

		
	}
	
	@Test
	void deleteComment_debeLanzarNoSuchElementException_siCommentNoExiste() {

		//Arrange		
		Long commentId=faker.number().randomNumber();
		Long authUserId=faker.number().randomNumber();
		
		when(commentDao.findById(commentId)).thenReturn(Optional.empty());
		
		//Act
		
		NoSuchElementException ex = assertThrows(NoSuchElementException.class, () -> commentService.deleteComment(commentId, authUserId));
		
		//Assert
		
		assertTrue(ex.getMessage().toLowerCase().contains("comentario no encontrado"));
		
		verifyNoInteractions(projectMemberService);
		
		verifyNoInteractions(mediaService);
		
		verify(commentDao,never()).save(any(Comment.class));
		
	}
	
	@Test
	void deleteComment_debeLanzarSecurityException_siNoTienePermisos() {

		//Arrange		
		Long commentId=faker.number().randomNumber();
		Long authUserId=faker.number().randomNumber();
		
		Usuario authUser= new UsuarioTestDataBuilder().build();
		
		Comment comment=new CommentTestDataBuilder().withId(commentId)
				.withAutor(authUser)
				.build();
		
		when(commentDao.findById(commentId)).thenReturn(Optional.of(comment));
		
		when(projectMemberService.isOwner(anyLong(), anyString())).thenReturn(false);
		
		//Act
		
		SecurityException ex = assertThrows(SecurityException.class, () -> commentService.deleteComment(commentId, authUserId));
		
		//Assert
		
		assertTrue(ex.getMessage().toLowerCase().contains("no tienes los permisos necesarios para este comentario"));
		
		verify(projectMemberService).isOwner(anyLong(), anyString());
		
		verifyNoInteractions(mediaService);
		
		verify(commentDao,never()).save(any(Comment.class));
		
	}
	
	@Test
	void deleteComment_debeBorrarComentario_siNoContieneAdjuntos() {

		//Arrange		
		Long commentId=faker.number().randomNumber();
		Long authUserId=faker.number().randomNumber();
		
		Usuario authUser= new UsuarioTestDataBuilder()
				.withId(authUserId)
				.build();
		
		Comment comment=new CommentTestDataBuilder().withId(commentId)
				.withAutor(authUser)
				.build();
		
		when(commentDao.findById(commentId)).thenReturn(Optional.of(comment));
		
		ArgumentCaptor<Comment> commentCaptor = ArgumentCaptor.forClass(Comment.class);
		
		//Act
		
		 commentService.deleteComment(commentId, authUserId);
		
		
		//Assert		
		
		verifyNoInteractions(projectMemberService);
		
		verifyNoInteractions(mediaService);
		
		verify(commentDao).findById(commentId);
		
		verify(commentDao).save(commentCaptor.capture());
		
		Comment commentDeleted = commentCaptor.getValue();
		
		assertNotNull(commentDeleted);
		assertEquals(commentId, commentDeleted.getId());
		assertSame(authUser, commentDeleted.getAutor());
		assertEquals(Constants.STATUS_INACTIVE, commentDeleted.getStatus());
		
	}
	
	@Test
	void deleteComment_debeBorrarComentario_siContieneAdjuntos() {

		//Arrange		
		Long commentId=faker.number().randomNumber();
		Long authUserId=faker.number().randomNumber();
		

		
		Media foto1 = new MediaTestDataBuilder().withOwnerId(authUserId).withStatus(Constants.STATUS_READY).build();

		Comment comment = new CommentTestDataBuilder()
				.withId(commentId)				
				.withAdjuntos(new ArrayList<>(List.of(foto1)))
				.build();

		
		when(commentDao.findById(commentId)).thenReturn(Optional.of(comment));
		
		when(projectMemberService.isOwner(anyLong(), anyString())).thenReturn(true);
		
		when(mediaService.saveAll(anyList())).thenAnswer(i -> i.getArgument(0));
		
		ArgumentCaptor<Comment> commentCaptor = ArgumentCaptor.forClass(Comment.class);
		
		ArgumentCaptor<List<Media>> mediaCaptor = ArgumentCaptor.forClass(List.class);
		
		//Act
		
		 commentService.deleteComment(commentId, authUserId);
		
		
		//Assert		
		
		verify(projectMemberService).isOwner(anyLong(), anyString());
				
		
		verify(commentDao).findById(commentId);
		
		verify(mediaService).saveAll(mediaCaptor.capture());
		
		verify(commentDao).save(commentCaptor.capture());
		
		Comment commentDeleted = commentCaptor.getValue();
		
		assertNotNull(commentDeleted);
		assertEquals(commentId, commentDeleted.getId());		
		assertEquals(Constants.STATUS_INACTIVE, commentDeleted.getStatus());
		
		
		List<Media> mediaListDeleted = mediaCaptor.getValue();
        assertNotNull(mediaListDeleted);
        assertSame(commentDeleted.getAdjuntos(), mediaListDeleted);
        assertEquals(1, mediaListDeleted.size());
        assertEquals(Constants.STATUS_INACTIVE, mediaListDeleted.get(0).getStatus());
		
	}
	
	@Test
	void updateComment_debeLanzarNoSuchException_siComentarioNoExiste() {
		//Arrange
		
		Long commentId = faker.number().randomNumber();
		
		Long authUserId=faker.number().randomNumber();
		
		when(commentDao.findById(commentId)).thenReturn(Optional.empty());
		
		//Act
		
		NoSuchElementException ex = assertThrows(NoSuchElementException.class, () -> commentService.updateComment(commentId, null, authUserId));
		
		//Assert
		assertTrue(ex.getMessage().toLowerCase().contains("comentario no encontrado"));
		
		verifyNoInteractions(usuarioService);
				
		
		verify(commentDao,never()).save(any(Comment.class));
		
		
	}
	
	@Test
	void updateComment_debeLanzarSecurityException_siauthUserNoTienePermisos() {
		//Arrange
		
		Long commentId=faker.number().randomNumber();
		Long authUserId=faker.number().randomNumber();
		
		
		Comment comment=new CommentTestDataBuilder().withId(commentId)				
				.build();
		
		
		when(commentDao.findById(commentId)).thenReturn(Optional.of(comment));
		
		//Act
		
		SecurityException ex = assertThrows(SecurityException.class, () -> commentService.updateComment(commentId, null, authUserId));
		
		//Assert
		assertTrue(ex.getMessage().toLowerCase().contains("no tienes los permisos necesarios"));
		
		verifyNoInteractions(usuarioService);
				
		
		verify(commentDao,never()).save(any(Comment.class));
		
		
	}
	
	@Test
	void updateComment_debeActualizar_siDtoNoContieneMenciones() {
		//Arrange
		
		Long commentId=faker.number().randomNumber();
		Long authUserId=faker.number().randomNumber();
		
		Usuario authUser = new UsuarioTestDataBuilder().withId(authUserId).build();
		
		Comment comment=new CommentTestDataBuilder().withId(commentId)
				.withAutor(authUser)
				.build();
		
		CommentUpdateDto dtoUpdate=new CommentUpdateDtoTestDataBuilder().build();
		
		when(commentDao.findById(commentId)).thenReturn(Optional.of(comment));
		
		when(commentDao.save(any(Comment.class))).thenAnswer(inv -> inv.getArgument(0));
		
		//Act
		
		CommentDto result= commentService.updateComment(commentId, dtoUpdate, authUserId);
		
		
		//Assert
		
		assertNotNull(result);
		assertNull(result.getMentionsUserIds());
		assertEquals(dtoUpdate.getBody(), result.getBody());
		assertEquals(commentId, result.getId());
		
		verify(commentDao).findById(commentId);
		
		verify(commentDao).save(any(Comment.class));
		
		verifyNoInteractions(usuarioService);
		
	}
	
	@Test
	void updateComment_debeActualizarMenciones_siDtoContieneMencionesValidas() {
		// Arrange
		Long authUserId = faker.number().randomNumber();
		Long mentionedUserId = faker.number().randomNumber();
		Long commentId = faker.number().randomNumber();

		Usuario authUser = new UsuarioTestDataBuilder().withId(authUserId).build();
		Usuario mentionedUser = new UsuarioTestDataBuilder().withId(mentionedUserId).build();

		Comment comment = new CommentTestDataBuilder()
				.withId(commentId)
				.withAutor(authUser)
				.withMentions(null) 
				.build();

		CommentUpdateDto dtoUpdate = new CommentUpdateDtoTestDataBuilder()
				.withMentionsUserIds(List.of(mentionedUserId))
				.build();

		
		when(commentDao.findById(commentId)).thenReturn(Optional.of(comment));
		
		
		when(usuarioService.findAllByIds(dtoUpdate.getMentionsUserIds()))
				.thenReturn(new ArrayList<>(List.of(mentionedUser)));

		when(commentDao.save(any(Comment.class))).thenAnswer(inv -> inv.getArgument(0));

		ArgumentCaptor<Comment> commentCaptor = ArgumentCaptor.forClass(Comment.class);

		// Act
		CommentDto result = commentService.updateComment(commentId, dtoUpdate, authUserId);

		// Assert
		assertNotNull(result);
		assertNotNull(result.getMentionsUserIds());
		assertEquals(1, result.getMentionsUserIds().size());
		assertEquals(mentionedUserId, result.getMentionsUserIds().get(0));
		assertEquals(dtoUpdate.getBody(), result.getBody());
		
		verify(commentDao).save(commentCaptor.capture());
		
		assertEquals(1, commentCaptor.getValue().getMentions().size());
		assertEquals(mentionedUser, commentCaptor.getValue().getMentions().get(0));
	}

	@Test
	void updateComment_debeFiltrarAutoMencion_siUsuarioSeMencionaASiMismoEnUpdate() {
		// Arrange
		Long authUserId = 50L;   
		Long otherUserId = 60L;   

		Usuario authUser = new UsuarioTestDataBuilder().withId(authUserId).build();
		Usuario otherUser = new UsuarioTestDataBuilder().withId(otherUserId).build();

		Comment comment = new CommentTestDataBuilder()
				.withId(faker.number().randomNumber())
				.withAutor(authUser)
				.build();

		
		CommentUpdateDto dtoUpdate = new CommentUpdateDtoTestDataBuilder()
				.withMentionsUserIds(List.of(authUserId, otherUserId)) 
				.build();

		when(commentDao.findById(anyLong())).thenReturn(Optional.of(comment));

		
		when(usuarioService.findAllByIds(dtoUpdate.getMentionsUserIds()))
				.thenReturn(new ArrayList<>(List.of(authUser, otherUser)));

		when(commentDao.save(any(Comment.class))).thenAnswer(inv -> inv.getArgument(0));

		// Act
		CommentDto result = commentService.updateComment(comment.getId(), dtoUpdate, authUserId);

		// Assert
		assertNotNull(result.getMentionsUserIds());
		assertEquals(1, result.getMentionsUserIds().size());
		assertEquals(otherUserId, result.getMentionsUserIds().get(0));
		
		
		verify(usuarioService).findAllByIds(anyList());
	}
	

}
