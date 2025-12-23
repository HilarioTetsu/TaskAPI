package com.springboot.app.models.services;

import static org.junit.jupiter.api.Assertions.assertEquals;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyShort;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
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
import com.springboot.app.models.dao.ITareaDao;
import com.springboot.app.models.dtos.TareaDto;
import com.springboot.app.models.entities.Comment;
import com.springboot.app.models.entities.PrioridadTarea;
import com.springboot.app.models.entities.Project;
import com.springboot.app.models.entities.Tarea;
import com.springboot.app.models.entities.TareaStatus;
import com.springboot.app.models.entities.Usuario;
import com.springboot.app.testdata.PrioridadTareaTestDataBuilder;
import com.springboot.app.testdata.ProjectTestDataBuilder;
import com.springboot.app.testdata.TareaDtoTestDataBuilder;
import com.springboot.app.testdata.TareaStatusTestDataBuilder;
import com.springboot.app.testdata.TareaTestDataBuilder;
import com.springboot.app.testdata.UsuarioTestDataBuilder;
import com.springboot.app.utils.TareaChangeLogHelper;

@ExtendWith(MockitoExtension.class)
class TareaServiceImplTest {

	@InjectMocks
	private TareaServiceImpl tareaService;

	@Mock
	private ITareaDao tareaDao;
	@Mock
	private IUsuarioService usuarioService;
	@Mock
	private IProjectService projectService;

	@Mock
	private CatalogoService catalogoService;

	@Mock
	private IProjectMemberService projectMemberService;
	@Mock
	private ICommentDao commentDao;

	@Mock
	private TareaChangeLogHelper tareaLogHelper;

	private static final Faker faker = new Faker();

	@Test
	void save_debeLanzarNoSuchElementException_siPrioridadNoExiste() {

		// Arrange

		TareaDto dto = new TareaDtoTestDataBuilder().build();

		when(catalogoService.findPrioridadTareaById(dto.getId_prioridad())).thenReturn(Optional.empty());

		// Act
		NoSuchElementException ex = assertThrows(NoSuchElementException.class, () -> tareaService.save(dto, 10L));

		// Assert
		assertTrue(ex.getMessage().toLowerCase().contains("prioridad"));

		verify(catalogoService).findPrioridadTareaById(dto.getId_prioridad());

		verify(catalogoService, never()).findTareaStatusById(anyShort());
	}

	@Test
	void save_debeLanzarNoSuchElementException_siTareaStatusNoExiste() {

		// Arrange

		Long userAuthId = 10L;

		PrioridadTarea prioridad = new PrioridadTareaTestDataBuilder().build();

		TareaDto dto = new TareaDtoTestDataBuilder().withPrioridad(prioridad.getId()).build();

		when(catalogoService.findPrioridadTareaById(dto.getId_prioridad())).thenReturn(Optional.of(prioridad));

		when(catalogoService.findTareaStatusById(dto.getId_tarea_status())).thenReturn(Optional.empty());

		// Act
		NoSuchElementException ex = assertThrows(NoSuchElementException.class,
				() -> tareaService.save(dto, userAuthId));

		// Assert
		assertTrue(ex.getMessage().toLowerCase().contains("status"));

		verify(catalogoService).findPrioridadTareaById(dto.getId_prioridad());

		verify(catalogoService).findTareaStatusById(dto.getId_tarea_status());

		verifyNoInteractions(tareaDao);
	}

	@Test
	void save_debeLanzarNoSuchElementException_siTareaNoExiste() {

		// Arrange

		Long userAuthId = 10L;

		PrioridadTarea prioridad = new PrioridadTareaTestDataBuilder().build();

		TareaStatus status = new TareaStatusTestDataBuilder().build();

		TareaDto dto = new TareaDtoTestDataBuilder().withPrioridad(prioridad.getId()).withTareaStatus(status.getId())
				.build();

		when(catalogoService.findPrioridadTareaById(dto.getId_prioridad())).thenReturn(Optional.of(prioridad));

		when(catalogoService.findTareaStatusById(dto.getId_tarea_status())).thenReturn(Optional.of(status));

		when(tareaDao.findById(dto.getIdGuid(), userAuthId))
				.thenThrow(new NoSuchElementException("Tarea no encontrada"));

		// Act
		NoSuchElementException ex = assertThrows(NoSuchElementException.class,
				() -> tareaService.save(dto, userAuthId));

		// Assert
		assertTrue(ex.getMessage().toLowerCase().contains("tarea"));

		verify(catalogoService).findPrioridadTareaById(dto.getId_prioridad());

		verify(catalogoService).findTareaStatusById(dto.getId_tarea_status());

		verifyNoInteractions(usuarioService);
		verifyNoInteractions(projectService);

	}

	@Test
	void save_debeLanzarNoSuchElementException_siUsuarioNoExiste() {

		// Arrange

		Long userAuthId = 10L;

		PrioridadTarea prioridad = new PrioridadTareaTestDataBuilder().build();

		TareaStatus status = new TareaStatusTestDataBuilder().build();

		TareaDto dto = new TareaDtoTestDataBuilder().withPrioridad(prioridad.getId()).withTareaStatus(status.getId())
				.build();

		Tarea tarea = new TareaTestDataBuilder().withTitulo(dto.getTitulo()).withDescripcion(dto.getDescripcion())
				.withPrioridadTarea(prioridad).withTareaStatus(status).build();

		when(catalogoService.findPrioridadTareaById(dto.getId_prioridad())).thenReturn(Optional.of(prioridad));

		when(catalogoService.findTareaStatusById(dto.getId_tarea_status())).thenReturn(Optional.of(status));

		when(tareaDao.findById(dto.getIdGuid(), userAuthId)).thenReturn(Optional.of(tarea));

		when(usuarioService.findByUserId(userAuthId)).thenThrow(new NoSuchElementException("Usuario no encontrado"));

		// Act
		NoSuchElementException ex = assertThrows(NoSuchElementException.class,
				() -> tareaService.save(dto, userAuthId));

		// Assert
		assertTrue(ex.getMessage().toLowerCase().contains("usuario"));

		verify(catalogoService).findPrioridadTareaById(dto.getId_prioridad());

		verify(catalogoService).findTareaStatusById(dto.getId_tarea_status());

		verify(tareaDao).findById(dto.getIdGuid(), userAuthId);

		verifyNoInteractions(projectService);

	}

	@Test
	void save_update_debeLanzarSecurityException_siUserAuthNoPuedeModificarTareas() {

		// Arrange

		Long userAuthId = 10L;

		Usuario userAuth = new UsuarioTestDataBuilder().withId(userAuthId).build();

		PrioridadTarea prioridad = new PrioridadTareaTestDataBuilder().build();

		TareaStatus status = new TareaStatusTestDataBuilder().build();

		TareaDto dto = new TareaDtoTestDataBuilder().withPrioridad(prioridad.getId()).withTareaStatus(status.getId())
				.build();

		Project project = new ProjectTestDataBuilder().withIdGuid(dto.getProject_id()).build();

		Tarea tarea = new TareaTestDataBuilder().withId(dto.getIdGuid()).withTitulo(dto.getTitulo())
				.withDescripcion(dto.getDescripcion()).withPrioridadTarea(prioridad).withTareaStatus(status)
				.withProject(project).build();

		when(catalogoService.findPrioridadTareaById(dto.getId_prioridad())).thenReturn(Optional.of(prioridad));

		when(catalogoService.findTareaStatusById(dto.getId_tarea_status())).thenReturn(Optional.of(status));

		when(tareaDao.findById(dto.getIdGuid(), userAuthId)).thenReturn(Optional.of(tarea));

		when(usuarioService.findByUserId(userAuthId)).thenReturn(userAuth);

		when(projectService.findByProjectId(dto.getProject_id())).thenReturn(Optional.of(project));

		when(projectMemberService.canEditTasks(userAuthId, project.getIdGuid())).thenReturn(false);

		// Act
		SecurityException ex = assertThrows(SecurityException.class, () -> tareaService.save(dto, userAuthId));

		// Assert
		assertTrue(ex.getMessage().toLowerCase().contains("no tienes permisos para gestionar tareas en este proyecto"));

		verify(catalogoService).findPrioridadTareaById(dto.getId_prioridad());

		verify(catalogoService).findTareaStatusById(dto.getId_tarea_status());

		verify(tareaDao).findById(eq(dto.getIdGuid()), eq(userAuthId));

		verify(usuarioService).findByUserId(userAuthId);

		verify(projectService).findByProjectId(dto.getProject_id());

		verify(projectMemberService).canEditTasks(eq(userAuthId), eq(project.getIdGuid()));

		verify(tareaDao, never()).saveAndFlush(any(Tarea.class));

	}

	@Test
	void save_debeLanzarSecurityException_siUserAuthNoPuedeModificarTareas() {

		// Arrange

		Long userAuthId = 10L;

		Usuario userAuth = new UsuarioTestDataBuilder().withId(userAuthId).build();

		PrioridadTarea prioridad = new PrioridadTareaTestDataBuilder().build();

		TareaStatus status = new TareaStatusTestDataBuilder().build();

		TareaDto dto = new TareaDtoTestDataBuilder().withIdGuid(null).withPrioridad(prioridad.getId())
				.withTareaStatus(status.getId()).build();

		Project project = new ProjectTestDataBuilder().withIdGuid(dto.getProject_id()).build();

		when(catalogoService.findPrioridadTareaById(dto.getId_prioridad())).thenReturn(Optional.of(prioridad));

		when(catalogoService.findTareaStatusById(dto.getId_tarea_status())).thenReturn(Optional.of(status));

		when(usuarioService.findByUserId(userAuthId)).thenReturn(userAuth);

		when(projectService.findByProjectId(dto.getProject_id())).thenReturn(Optional.of(project));

		when(projectMemberService.canEditTasks(userAuthId, project.getIdGuid())).thenReturn(false);

		// Act
		SecurityException ex = assertThrows(SecurityException.class, () -> tareaService.save(dto, userAuthId));

		// Assert
		assertTrue(ex.getMessage().toLowerCase().contains("no tienes permisos para gestionar tareas en este proyecto"));

		verify(catalogoService).findPrioridadTareaById(dto.getId_prioridad());

		verify(catalogoService).findTareaStatusById(dto.getId_tarea_status());

		verify(usuarioService).findByUserId(userAuthId);

		verify(projectService).findByProjectId(dto.getProject_id());

		verify(projectMemberService).canEditTasks(eq(userAuthId), eq(project.getIdGuid()));

		verify(tareaDao, never()).saveAndFlush(any(Tarea.class));

	}

	@Test
	void findByIdGuidAndUserId_debeLanzarNoSuchElementException_siTareaNoExiste() {

		String tareaId = UUID.randomUUID().toString();

		Long userId = faker.number().randomNumber();

		when(tareaDao.findById(tareaId, userId)).thenReturn(Optional.empty());

		NoSuchElementException ex = assertThrows(NoSuchElementException.class,
				() -> tareaService.findByIdGuidAndUserId(tareaId, userId));

		assertTrue(ex.getMessage().toLowerCase().contains("tarea"));

		verify(tareaDao).findById(tareaId, userId);

	}

	@Test
	void findByIdGuidAndUserId_debeEncontrarTarea_siTareaExiste() {

		String tareaId = UUID.randomUUID().toString();

		Long userId = 1L;

		Usuario owner = new UsuarioTestDataBuilder().withId(userId).build();

		Tarea tarea = new TareaTestDataBuilder().withId(tareaId).withOwner(owner).build();

		when(tareaDao.findById(tareaId, userId)).thenReturn(Optional.of(tarea));

		TareaDto result = tareaService.findByIdGuidAndUserId(tareaId, userId);

		assertNotNull(result);

		assertEquals(tareaId, result.getIdGuid());

		verify(tareaDao).findById(tareaId, userId);

	}

	@Test
	void save_debeCrearTarea_yAsignarlaAlOwner_siProjectEsNull() {

		Long userAuthId = 10L;

		TareaDto dto = new TareaDtoTestDataBuilder().withIdGuid(null).withProjectId(null).build();

		PrioridadTarea prioridad = new PrioridadTareaTestDataBuilder().withId(dto.getId_prioridad()).build();

		TareaStatus status = new TareaStatusTestDataBuilder().withId(dto.getId_tarea_status()).build();

		Usuario user = new UsuarioTestDataBuilder().withId(userAuthId).build();

		String generatedTareaId = UUID.randomUUID().toString();

		Tarea tareaSimuladaEnBD = new TareaTestDataBuilder().withId(generatedTareaId).withOwner(user)
				.withTitulo(dto.getTitulo()).withDescripcion(dto.getDescripcion()).withPrioridadTarea(prioridad)
				.withTareaStatus(status).withProject(null).build();

		when(catalogoService.findPrioridadTareaById(dto.getId_prioridad())).thenReturn(Optional.of(prioridad));
		when(catalogoService.findTareaStatusById((short) 1)).thenReturn(Optional.of(status));
		when(usuarioService.findByUserId(userAuthId)).thenReturn(user);
		when(projectService.findByProjectId(null)).thenReturn(Optional.empty());
		when(tareaDao.saveAndFlush(any(Tarea.class))).thenAnswer(inv -> {

			Tarea tarea = inv.getArgument(0);

			tarea.setIdGuid(generatedTareaId);

			return tarea;

		});
		when(tareaDao.findById(generatedTareaId)).thenReturn(Optional.of(tareaSimuladaEnBD));
		when(usuarioService.findAllByIds(anyList())).thenReturn(List.of(user));

		when(tareaDao.save(any(Tarea.class))).thenAnswer(i -> i.getArgument(0));

		ArgumentCaptor<Tarea> tareaCaptor = ArgumentCaptor.forClass(Tarea.class);

		TareaDto result = tareaService.save(dto, userAuthId);

		assertNotNull(result);
		assertNotNull(result.getIdGuid(), "Debe generar GUID al crear");
		assertEquals(dto.getTitulo(), result.getTitulo());
		assertEquals(dto.getDescripcion(), result.getDescripcion());

		verify(tareaDao).saveAndFlush(tareaCaptor.capture());

		Tarea tareaGuardada = tareaCaptor.getValue();

		assertEquals(dto.getTitulo(), tareaGuardada.getTitulo());
		assertEquals(dto.getDescripcion(), tareaGuardada.getDescripcion());
		assertSame(user, tareaGuardada.getOwner(), "Owner debe ser el usuario autenticado");
		assertSame(prioridad, tareaGuardada.getPrioridad());
		assertSame(status, tareaGuardada.getTareaStatus());
		assertNull(tareaGuardada.getProject(), "Project debe quedar null");

		verify(tareaDao).save(tareaCaptor.capture());
		Tarea tareaFinal = tareaCaptor.getValue();

		assertNotNull(tareaFinal.getUsuarios());
		assertEquals(1, tareaFinal.getUsuarios().size());
		assertTrue(tareaFinal.getUsuarios().stream().anyMatch(u -> u.getId().equals(userAuthId)));

		verify(projectMemberService, never()).canEditTasks(anyLong(), anyString());
	}

	@Test
	void save_update_debeActualizarTarea_yAsignarlaAlOwner_siProjectEsNull() {

		Long userAuthId = 10L;

		TareaDto dto = new TareaDtoTestDataBuilder().withProjectId(null).build();

		PrioridadTarea prioridad = new PrioridadTareaTestDataBuilder().withId(dto.getId_prioridad()).build();

		TareaStatus status = new TareaStatusTestDataBuilder().withId(dto.getId_tarea_status()).build();

		Usuario user = new UsuarioTestDataBuilder().withId(userAuthId).build();

		Tarea tarea = new TareaTestDataBuilder().withId(dto.getIdGuid()).withPrioridadTarea(prioridad)
				.withTareaStatus(status).withProject(null).withOwner(user).build();

		Tarea tareaSimuladaEnBD = new TareaTestDataBuilder().withId(dto.getIdGuid()).withOwner(user)
				.withTitulo(dto.getTitulo()).withDescripcion(dto.getDescripcion()).withPrioridadTarea(prioridad)
				.withTareaStatus(status).withProject(null).build();

		when(catalogoService.findPrioridadTareaById(dto.getId_prioridad())).thenReturn(Optional.of(prioridad));
		when(catalogoService.findTareaStatusById(dto.getId_tarea_status())).thenReturn(Optional.of(status));

		when(tareaDao.findById(dto.getIdGuid(), userAuthId)).thenReturn(Optional.of(tarea));

		when(usuarioService.findByUserId(userAuthId)).thenReturn(user);

		when(projectService.findByProjectId(null)).thenReturn(Optional.empty());

		when(tareaLogHelper.generarMensajeCambiosTarea(any(Tarea.class), any(TareaDto.class), any(Usuario.class),
				any(PrioridadTarea.class), any(TareaStatus.class), eq(null)))
				.thenReturn("Campo ha cambiado a void por el usuario x");

		when(commentDao.save(any(Comment.class))).thenAnswer(inv -> {

			Comment saved = inv.getArgument(0);

			saved.setId(faker.number().randomNumber());

			return saved;
		});

		when(tareaDao.saveAndFlush(any(Tarea.class))).thenAnswer(inv -> {

			Tarea tareaActualizada = inv.getArgument(0);

			tareaActualizada.setUsuarioModificacion(user.getUsername());

			return tareaActualizada;
		});

		when(tareaDao.findById(tarea.getIdGuid())).thenReturn(Optional.of(tareaSimuladaEnBD));

		when(usuarioService.findAllByIds(anyList())).thenReturn(List.of(user));

		when(tareaDao.save(any(Tarea.class))).thenAnswer(i -> i.getArgument(0));

		ArgumentCaptor<Tarea> tareaCaptor = ArgumentCaptor.forClass(Tarea.class);

		TareaDto result = tareaService.save(dto, userAuthId);

		assertEquals(dto.getTitulo(), result.getTitulo());
		assertEquals(dto.getDescripcion(), result.getDescripcion());
		assertNull(dto.getProject_id(), "Project debe quedar null");
		assertEquals(dto.getId_prioridad(), result.getId_prioridad());
		assertEquals(dto.getId_tarea_status(), result.getId_tarea_status());

		verify(tareaDao).saveAndFlush(tareaCaptor.capture());

		Tarea tareaGuardada = tareaCaptor.getValue();

		assertEquals(dto.getTitulo(), tareaGuardada.getTitulo());
		assertEquals(dto.getDescripcion(), tareaGuardada.getDescripcion());
		assertEquals(user.getUsername(), tareaGuardada.getUsuarioModificacion());

		assertSame(user, tareaGuardada.getOwner(), "Owner debe ser el usuario autenticado");
		assertSame(prioridad, tareaGuardada.getPrioridad());
		assertSame(status, tareaGuardada.getTareaStatus());
		assertNull(tareaGuardada.getProject(), "Project debe quedar null");

		verify(tareaDao).save(tareaCaptor.capture());

		Tarea tareaFinal = tareaCaptor.getValue();

		assertEquals(1, tareaFinal.getUsuarios().size());
		assertTrue(tareaFinal.getUsuarios().stream().anyMatch(u -> u.getId().equals(userAuthId)));

		verify(projectMemberService, never()).canEditTasks(anyLong(), anyString());
	}

	@Test
	void save_debeCrearTarea_yVincularAlProyecto_siProjectExiste() {
		// Arrange

		Long userAuthId = 10L;

		String projectId = UUID.randomUUID().toString();

		TareaDto dto = new TareaDtoTestDataBuilder().withIdGuid(null).withProjectId(projectId).build();

		Project project = new ProjectTestDataBuilder().withIdGuid(projectId).build();

		PrioridadTarea prioridad = new PrioridadTareaTestDataBuilder().withId((short) 2).build();

		TareaStatus status = new TareaStatusTestDataBuilder().build();

		Usuario user = new UsuarioTestDataBuilder().withId(userAuthId).build();

		when(catalogoService.findPrioridadTareaById(dto.getId_prioridad())).thenReturn(Optional.of(prioridad));
		when(catalogoService.findTareaStatusById(dto.getId_tarea_status())).thenReturn(Optional.of(status));

		when(usuarioService.findByUserId(userAuthId)).thenReturn(user);
		when(projectService.findByProjectId(projectId)).thenReturn(Optional.of(project));

		when(projectMemberService.canEditTasks(userAuthId, project.getIdGuid())).thenReturn(true);

		when(tareaDao.saveAndFlush(any(Tarea.class))).thenAnswer(inv -> inv.getArgument(0));

		ArgumentCaptor<Tarea> tareaCaptor = ArgumentCaptor.forClass(Tarea.class);

		// Act
		TareaDto result = tareaService.save(dto, userAuthId);

		// Assert
		assertNotNull(result);
		assertNotNull(result.getIdGuid(), "Debe generar GUID al crear");
		assertEquals(dto.getTitulo(), result.getTitulo());
		assertEquals(dto.getDescripcion(), result.getDescripcion());

		verify(tareaDao).saveAndFlush(tareaCaptor.capture());
		Tarea tareaGuardada = tareaCaptor.getValue();

		assertEquals(dto.getTitulo(), tareaGuardada.getTitulo());
		assertEquals(dto.getDescripcion(), tareaGuardada.getDescripcion());
		assertSame(user, tareaGuardada.getOwner(), "Owner debe ser el usuario autenticado");
		assertSame(prioridad, tareaGuardada.getPrioridad());
		assertSame(status, tareaGuardada.getTareaStatus());

		assertSame(project, tareaGuardada.getProject());

		verify(projectMemberService).canEditTasks(eq(userAuthId), eq(project.getIdGuid()));

		verify(tareaDao, never()).save(any(Tarea.class));

	}

	@Test
	void save_update_debeActualizarTarea_yGuardarComment_siProjectExiste() {

		Long userAuthId = 10L;

		TareaDto tareaDto = new TareaDtoTestDataBuilder().build();

		Usuario authUser = new UsuarioTestDataBuilder().withId(userAuthId).build();

		Project project = new ProjectTestDataBuilder().withIdGuid(tareaDto.getProject_id()).build();

		Usuario ownerProject = project.getOwner();

		PrioridadTarea prioridad = new PrioridadTareaTestDataBuilder().withId(tareaDto.getId_prioridad()).build();

		TareaStatus tareaStatus = new TareaStatusTestDataBuilder().withId(tareaDto.getId_tarea_status()).build();

		Usuario asignado1 = authUser;

		Usuario asignado2 = new UsuarioTestDataBuilder().withId(55L).build();

		Tarea tarea = new TareaTestDataBuilder().withId(tareaDto.getIdGuid()).withPrioridadTarea(prioridad)
				.withTareaStatus(tareaStatus).withUsuarios(List.of(asignado1, asignado2)).withProject(project).build();

		Usuario tareaOwner = tarea.getOwner();

		when(catalogoService.findPrioridadTareaById(tareaDto.getId_prioridad())).thenReturn(Optional.of(prioridad));

		when(catalogoService.findTareaStatusById(tareaDto.getId_tarea_status())).thenReturn(Optional.of(tareaStatus));

		when(tareaDao.findById(tareaDto.getIdGuid(), userAuthId)).thenReturn(Optional.of(tarea));

		when(usuarioService.findByUserId(userAuthId)).thenReturn(authUser);

		when(projectService.findByProjectId(tareaDto.getProject_id())).thenReturn(Optional.of(project));

		when(projectMemberService.canEditTasks(userAuthId, project.getIdGuid())).thenReturn(true);

		when(tareaLogHelper.generarMensajeCambiosTarea(any(Tarea.class), any(TareaDto.class), any(Usuario.class),
				any(PrioridadTarea.class), any(TareaStatus.class), any(Project.class)))
				.thenReturn("Campo ha cambiado a void por el usuario x");

		// despues de remover las menciones al usuario auth
		List<Long> expectedMentionsIds = List.of(asignado2.getId(), ownerProject.getId(), tareaOwner.getId());

		when(usuarioService.findAllByIds(
				argThat(list -> list.containsAll(expectedMentionsIds) && list.size() == expectedMentionsIds.size())))
				.thenReturn(List.of(asignado2, ownerProject, tareaOwner));

		when(commentDao.save(any(Comment.class))).thenAnswer(inv -> {

			Comment saved = inv.getArgument(0);

			saved.setId(faker.number().randomNumber());

			return saved;
		});

		when(tareaDao.saveAndFlush(any(Tarea.class))).thenAnswer(inv -> inv.getArgument(0));

		ArgumentCaptor<Tarea> tareaCaptor = ArgumentCaptor.forClass(Tarea.class);

		ArgumentCaptor<Comment> commentCaptor = ArgumentCaptor.forClass(Comment.class);

		// Act

		TareaDto result = tareaService.save(tareaDto, userAuthId);

		// Assert

		assertNotNull(result);

		assertEquals(tareaDto.getTitulo(), result.getTitulo());
		assertEquals(tareaDto.getDescripcion(), result.getDescripcion());
		assertEquals(tareaDto.getProject_id(), result.getProject_id());
		assertEquals(tareaDto.getId_prioridad(), result.getId_prioridad());
		assertEquals(tareaDto.getId_tarea_status(), result.getId_tarea_status());

		verify(commentDao).save(commentCaptor.capture());

		Comment savedComment = commentCaptor.getValue();

		assertEquals("Campo ha cambiado a void por el usuario x", savedComment.getBody());
		assertSame(authUser, savedComment.getAutor());
		assertSame(tarea, savedComment.getTarea());
		assertNotNull(savedComment.getMentions());

		assertTrue(savedComment.getMentions().stream().map(u -> u.getId()).toList().containsAll(expectedMentionsIds));

		assertFalse(savedComment.getMentions().stream().anyMatch(u -> u.getId().equals(userAuthId)),
				"El autor del cambio no debe ser mencionado");

		verify(tareaDao).saveAndFlush(tareaCaptor.capture());

		Tarea savedTarea = tareaCaptor.getValue();

		assertEquals(tareaDto.getTitulo(), savedTarea.getTitulo());
		assertEquals(tareaDto.getDescripcion(), savedTarea.getDescripcion());
		assertSame(prioridad, savedTarea.getPrioridad());
		assertSame(tareaStatus, savedTarea.getTareaStatus());
		assertSame(project, savedTarea.getProject());
		assertEquals(authUser.getUsername(), savedTarea.getUsuarioModificacion());

		verify(tareaDao, never()).save(any(Tarea.class));

		verify(projectMemberService).canEditTasks(eq(userAuthId), eq(project.getIdGuid()));

	}

	@Test
	void getAllActives_debeRetornarPaginaDtos_yMapearCorrectamente_cuandoHayFiltros() {
		// Arrange
		int pagina = 0;
		int tamanio = 10;
		String sorts = "fechaLimite,desc;"; //

		List<Short> statusIds = List.of((short) 1, (short) 2);
		List<Short> prioridadIds = List.of((short) 3);

		LocalDate fechaDesde = LocalDate.now();
		LocalDate fechaHasta = LocalDate.now().plusDays(5);
		Long ownerId = 10L;

		Tarea tarea = new TareaTestDataBuilder().build();
		List<Tarea> listaTareas = List.of(tarea);

		Page<Tarea> pageEntities = new PageImpl<>(listaTareas);

		when(tareaDao.getAllActives(any(Pageable.class), eq(statusIds), eq(prioridadIds), eq(fechaDesde),
				eq(fechaHasta), anyString(), // busquedaDesc
				anyString(), // busquedaTitulo
				eq(true), // aplicarPrioridad (esperamos true porque la lista no es vacía)
				eq(true), // aplicarTareaStatus (esperamos true)
				eq(ownerId))).thenReturn(pageEntities);

		// Act
		Page<TareaDto> result = tareaService.getAllActives(pagina, tamanio, statusIds, prioridadIds, fechaDesde,
				fechaHasta, "desc", "titulo", sorts, ownerId);

		// Assert
		assertNotNull(result);
		assertEquals(1, result.getTotalElements());

		TareaDto dtoResultado = result.getContent().get(0);
		assertEquals(tarea.getIdGuid(), dtoResultado.getIdGuid());

		ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

		verify(tareaDao).getAllActives(pageableCaptor.capture(), eq(statusIds), eq(prioridadIds), any(), any(), any(),
				any(), eq(true), eq(true), eq(ownerId));

		assertEquals(pagina, pageableCaptor.getValue().getPageNumber());
		assertEquals(tamanio, pageableCaptor.getValue().getPageSize());
		assertEquals(1, pageableCaptor.getValue().getSort().toList().size());
	}

	@Test
	void getAllActives_debeLanzarIllegalArgumentException_cuandoSortsSeaInvalido() {
		// Arrange
		int pagina = 0;
		int tamanio = 10;
		String sorts = "ordenamiento_invalido"; //

		List<Short> statusIds = List.of((short) 1, (short) 2);
		List<Short> prioridadIds = List.of((short) 3);

		LocalDate fechaDesde = LocalDate.now();
		LocalDate fechaHasta = LocalDate.now().plusDays(5);
		Long ownerId = 10L;

		// Act
		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
				() -> tareaService.getAllActives(pagina, tamanio, statusIds, prioridadIds, fechaDesde, fechaHasta,
						"desc", "titulo", sorts, ownerId));

		// Asserts
		assertTrue(ex.getMessage().toLowerCase().contains("formato invalido"));

		verify(tareaDao, never()).getAllActives(any(Pageable.class), anyList(), anyList(), any(), any(), anyString(),
				anyString(), anyBoolean(), anyBoolean(), anyLong());

	}

	@Test
	void asignarTarea_debeAsignarTarea_cuandoProjectoEsNull() {

		// Arrange

		Long authUserId = 10L;

		List<Long> userIds = List.of(authUserId);

		String tareaId = UUID.randomUUID().toString();

		Usuario authUser = new UsuarioTestDataBuilder().withId(authUserId).build();

		Tarea tarea = new TareaTestDataBuilder().withProject(null).withId(tareaId).withOwner(authUser).build();

		when(tareaDao.findById(tareaId)).thenReturn(Optional.of(tarea));
		when(usuarioService.findAllByIds(userIds)).thenReturn(List.of(authUser));
		when(tareaDao.save(any(Tarea.class))).thenAnswer(inv -> inv.getArgument(0));

		ArgumentCaptor<Tarea> tareaCaptor = ArgumentCaptor.forClass(Tarea.class);

		// Act

		tareaService.asignarTarea(userIds, tareaId, authUserId);

		// Arrange

		verify(tareaDao).save(tareaCaptor.capture());

		Tarea tareaUpdated = tareaCaptor.getValue();

		assertNotNull(tareaUpdated);
		assertEquals(tareaId, tareaUpdated.getIdGuid());
		assertSame(authUser, tareaUpdated.getOwner());
		assertNull(tareaUpdated.getProject());
		assertNotNull(tareaUpdated.getUsuarios());
		assertEquals(1, tareaUpdated.getUsuarios().size());
		assertTrue(tareaUpdated.getUsuarios().stream().anyMatch(u -> u.getId().equals(authUserId)));

		verify(projectMemberService, never()).isOwner(anyLong(), anyString());

	}

	@Test
	void asignarTarea_debeAsignarTarea_cuandoProjectoExiste() {

		// Arrange

		Long authUserId = 10L;

		Usuario authUser = new UsuarioTestDataBuilder().withId(authUserId).build();

		Usuario asignado1 = new UsuarioTestDataBuilder().build();

		Usuario asignado2 = new UsuarioTestDataBuilder().build();

		List<Long> userIds = List.of(asignado1.getId(), asignado2.getId());

		String tareaId = UUID.randomUUID().toString();

		Project project = new ProjectTestDataBuilder().withOwner(authUser).build();

		Tarea tarea = new TareaTestDataBuilder().withId(tareaId).withProject(project).build();

		when(tareaDao.findById(tareaId)).thenReturn(Optional.of(tarea));

		when(projectMemberService.isOwner(authUserId, tarea.getProject().getIdGuid())).thenReturn(true);

		when(usuarioService.findAllByIds(userIds)).thenReturn(List.of(asignado1, asignado2));

		when(projectMemberService.isMember(asignado1.getId(), tarea.getProject().getIdGuid())).thenReturn(true);

		when(projectMemberService.isMember(asignado2.getId(), tarea.getProject().getIdGuid())).thenReturn(true);

		when(tareaDao.save(any(Tarea.class))).thenAnswer(inv -> inv.getArgument(0));

		ArgumentCaptor<Tarea> tareaCaptor = ArgumentCaptor.forClass(Tarea.class);

		// Act

		tareaService.asignarTarea(userIds, tareaId, authUserId);

		// Arrange

		verify(tareaDao).save(tareaCaptor.capture());

		Tarea tareaUpdated = tareaCaptor.getValue();

		assertNotNull(tareaUpdated);
		assertEquals(tareaId, tareaUpdated.getIdGuid());
		assertNotNull(tareaUpdated.getProject());
		assertNotNull(tareaUpdated.getUsuarios());
		assertEquals(2, tareaUpdated.getUsuarios().size());

		assertTrue(tareaUpdated.getUsuarios().stream().map(u -> u.getId()).toList().containsAll(userIds));

		verify(projectMemberService).isOwner(eq(authUserId), eq(tarea.getProject().getIdGuid()));

		verify(projectMemberService, times(userIds.size())).isMember(anyLong(), anyString());

	}

	@Test
	void asignarTarea_debeLanzarNoSuchException_cuandoTareaNoExiste() {

		// Arrange

		Long authUserId = 10L;

		List<Long> userIds = List.of(authUserId);

		String tareaId = UUID.randomUUID().toString();

		when(tareaDao.findById(tareaId)).thenReturn(Optional.empty());

		// Act

		NoSuchElementException ex = assertThrows(NoSuchElementException.class,
				() -> tareaService.asignarTarea(userIds, tareaId, authUserId));

		// Arrange

		assertTrue(ex.getMessage().toLowerCase().contains("tarea no encontrada"));

		verify(tareaDao, never()).save(any(Tarea.class));

	}
	
	@Test
	void asignarTarea_debeLanzarIllegalStateException_cuandoSeAsignanMultiplesUsuariosATareaPersonal() {

		// Arrange

		Long authUserId = 10L;

		List<Long> userIds = List.of(authUserId,faker.number().numberBetween(1, Long.MAX_VALUE));

		String tareaId = UUID.randomUUID().toString();
		
		Usuario authUser = new UsuarioTestDataBuilder().withId(authUserId).build();
		
		Tarea tarea = new TareaTestDataBuilder().withProject(null).withId(tareaId).withOwner(authUser).build();

		when(tareaDao.findById(tareaId)).thenReturn(Optional.of(tarea));

		// Act

		IllegalStateException ex = assertThrows(IllegalStateException.class,
				() -> tareaService.asignarTarea(userIds, tareaId, authUserId));

		// Arrange

		assertTrue(ex.getMessage().toLowerCase().contains("no puede asignarse a mas de un usuario"));

		verify(tareaDao, never()).save(any(Tarea.class));

	}
	
	@Test
	void asignarTarea_debeLanzarSecurityException_cuandoSeAsignaUnaTareaPersonalAUnUsuarioNoDuenioDeLaTarea() {

		// Arrange

		Long authUserId = 10L;

		List<Long> userIds = List.of(authUserId);

		String tareaId = UUID.randomUUID().toString();
				
		
		Tarea tarea = new TareaTestDataBuilder().withProject(null).withId(tareaId).build();

		when(tareaDao.findById(tareaId)).thenReturn(Optional.of(tarea));

		// Act

		SecurityException ex = assertThrows(SecurityException.class,
				() -> tareaService.asignarTarea(userIds, tareaId, authUserId));

		// Arrange

		assertTrue(ex.getMessage().toLowerCase().contains("no tienes los permisos necesarios sobre esta tarea"));

		verify(tareaDao, never()).save(any(Tarea.class));

	}
	
	@Test
	void asignarTarea_debeLanzarSecurityException_cuandoNoEresDueñoDelProyecto() {

		// Arrange

		Long authUserId = 10L;

		List<Long> userIds = List.of(authUserId);

		String tareaId = UUID.randomUUID().toString();
						
		Tarea tarea = new TareaTestDataBuilder().withId(tareaId).build();

		when(tareaDao.findById(tareaId)).thenReturn(Optional.of(tarea));
		when(projectMemberService.isOwner(authUserId, tarea.getProject().getIdGuid())).thenReturn(false);

		// Act

		SecurityException ex = assertThrows(SecurityException.class,
				() -> tareaService.asignarTarea(userIds, tareaId, authUserId));

		// Arrange

		assertTrue(ex.getMessage().toLowerCase().contains("no tienes los permisos necesarios"));
		
		verify(projectMemberService).isOwner(eq(authUserId), eq(tarea.getProject().getIdGuid()));
		
		verify(tareaDao, never()).save(any(Tarea.class));

	}
	
	@Test
	void getTareasAsignadasByProjectId_debeRetornarTareas_cuandoUsuarioTieneTareasAsignadas() {

		// Arrange

		Long authUserId = 10L;

		String projectId=UUID.randomUUID().toString();
		
		when(projectService.existsProjectActive(projectId)).thenReturn(false);

		// Act
		NoSuchElementException ex = assertThrows(NoSuchElementException.class,
				() -> tareaService.getTareasAsignadasByProjectId(projectId, authUserId));
		

		// Arrange
		
		assertTrue(ex.getMessage().toLowerCase().contains("proyecto inexistente"));
		
		verify(projectMemberService,never()).isMember(anyLong(), anyString());
		
		verify(usuarioService,never()).findByUserId(anyLong());


	}

}
