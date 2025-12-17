package com.springboot.app.models.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.springboot.app.models.entities.PrioridadTarea;
import com.springboot.app.models.entities.Project;
import com.springboot.app.models.entities.Tarea;
import com.springboot.app.models.entities.TareaStatus;
import com.springboot.app.models.dao.IPrioridadTareaDao;
import com.springboot.app.models.dao.ITareaDao;
import com.springboot.app.models.dao.ITareaStatusDao;
import com.springboot.app.models.dtos.TareaDto;
import com.springboot.app.models.entities.Usuario;
import com.springboot.app.testdata.PrioridadTareaTestDataBuilder;
import com.springboot.app.testdata.ProjectTestDataBuilder;
import com.springboot.app.testdata.TareaDtoTestDataBuilder;
import com.springboot.app.testdata.TareaStatusTestDataBuilder;
import com.springboot.app.testdata.TareaTestDataBuilder;
import com.springboot.app.testdata.UsuarioTestDataBuilder;

@ExtendWith(MockitoExtension.class)
class TareaServiceImplTest {

	@InjectMocks
	private TareaServiceImpl tareaService;

	@Mock
	private ITareaDao tareaDao;
	@Mock
	private IPrioridadTareaDao prioridadDao;
	@Mock
	private ITareaStatusDao tareaStatusDao;

	@Mock
	private IUsuarioService usuarioService;
	@Mock
	private IProjectService projectService;
	@Mock
	private IProjectMemberService projectMemberService;
	@Mock
	private ICommentService commentService;

	@Test
	void save_debeLanzarNoSuchElementException_siPrioridadNoExiste() {

		TareaDto dto = new TareaDto();
		dto.setId_prioridad(Short.MAX_VALUE); // un ID que no existe
		dto.setId_tarea_status((short) 1); // cualquier valor
		dto.setProject_id(null); // para no entrar a validación de proyecto
		dto.setIdGuid(null); // indica "crear"

		when(prioridadDao.findById(Short.MAX_VALUE)).thenReturn(Optional.empty());

		NoSuchElementException ex = assertThrows(NoSuchElementException.class, () -> tareaService.save(dto, 10L));

		assertTrue(ex.getMessage().toLowerCase().contains("prioridad"));
		verify(prioridadDao).findById(Short.MAX_VALUE);

		verifyNoInteractions(tareaStatusDao);
	}

	@Test
	void findByIdGuidAndUserId_debeLanzarNoSuchElementException_siTareaNoExiste() {

		String tareaId = UUID.randomUUID().toString();

		Long userId = Long.MAX_VALUE;

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

		TareaDto dto = new TareaDtoTestDataBuilder().withIdGuid(null).build();

		PrioridadTarea prioridad = new PrioridadTareaTestDataBuilder().withId(dto.getId_prioridad()).build();

		TareaStatus status = new TareaStatusTestDataBuilder().withId(dto.getId_tarea_status()).build();

		Usuario user = new UsuarioTestDataBuilder().withId(userAuthId).build();

		when(prioridadDao.findById(dto.getId_prioridad())).thenReturn(Optional.of(prioridad));
		when(tareaStatusDao.findById((short) 1)).thenReturn(Optional.of(status));
		when(usuarioService.findByUserId(userAuthId)).thenReturn(user);
		when(projectService.findByProjectId(null)).thenReturn(Optional.empty());

		ArgumentCaptor<Tarea> tareaCaptor = ArgumentCaptor.forClass(Tarea.class);

		when(tareaDao.saveAndFlush(any(Tarea.class))).thenAnswer(inv -> inv.getArgument(0));

		TareaServiceImpl spyService = spy(tareaService);

		doNothing().when(spyService).asignarTarea(anyList(), anyString(), eq(userAuthId));

		TareaDto result = spyService.save(dto, userAuthId);

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

		verify(spyService).asignarTarea(argThat(list -> list.size() == 1 && list.contains(userAuthId)),
				eq(tareaGuardada.getIdGuid()), eq(userAuthId));

		verify(projectMemberService, never()).canEditTasks(anyLong(), anyString());
	}

	@Test
	void save_debeCrearTarea_yVincularAlProyecto_siProjectExiste() {

		Long userAuthId = 10L;

		String projectId = UUID.randomUUID().toString();

		TareaDto dto = new TareaDtoTestDataBuilder().withIdGuid(null).withProjectId(projectId).build();

		Project project = new ProjectTestDataBuilder().withIdGuid(projectId).build();

		PrioridadTarea prioridad = new PrioridadTareaTestDataBuilder().withId((short) 2).build();

		TareaStatus status = new TareaStatusTestDataBuilder().build();

		Usuario user = new UsuarioTestDataBuilder().withId(userAuthId).build();

		when(prioridadDao.findById(dto.getId_prioridad())).thenReturn(Optional.of(prioridad));
		when(tareaStatusDao.findById(dto.getId_tarea_status())).thenReturn(Optional.of(status));

		when(usuarioService.findByUserId(userAuthId)).thenReturn(user);
		when(projectService.findByProjectId(projectId)).thenReturn(Optional.of(project));

		when(projectMemberService.canEditTasks(userAuthId, project.getIdGuid())).thenReturn(true);

		ArgumentCaptor<Tarea> tareaCaptor = ArgumentCaptor.forClass(Tarea.class);

		when(tareaDao.saveAndFlush(any(Tarea.class))).thenAnswer(inv -> inv.getArgument(0));

		TareaServiceImpl spyService = spy(tareaService);

		TareaDto result = spyService.save(dto, userAuthId);

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

		verify(spyService, never()).asignarTarea(anyList(), anyString(), anyLong());

	}

}
