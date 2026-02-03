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
import static org.mockito.ArgumentMatchers.anyShort;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.github.javafaker.Faker;
import com.springboot.app.models.dao.IInvitationDao;
import com.springboot.app.models.dtos.InvitationDto;
import com.springboot.app.models.entities.Invitation;
import com.springboot.app.models.entities.Project;
import com.springboot.app.models.entities.Usuario;
import com.springboot.app.testdata.InvitationDtoTestDataBuilder;
import com.springboot.app.testdata.ProjectTestDataBuilder;
import com.springboot.app.testdata.UsuarioTestDataBuilder;
import com.springboot.app.utils.Constants;
import com.springboot.app.utils.ProjectRole;


@ExtendWith(MockitoExtension.class)
class InvitationServiceImplTest {

	@InjectMocks
	private InvitationServiceImpl invitationService;
	
	@Mock
	private IInvitationDao invitationDao;
	@Mock
	private IUsuarioService usuarioService;
	@Mock
	private IProjectService projectService;
	@Mock
	private IProjectMemberService projectMemberService;
	
	private static final Faker faker = new Faker();
	
	@Test
	void inviteUserToProject_debeLanzarNoSuchElementException_siProyectoNoExiste() {
		//Arrange
		
		InvitationDto dto = new InvitationDtoTestDataBuilder().build();
		
		Long authUserId = faker.number().randomNumber();
		
		when(projectService.findByProjectId(dto.getProjectId())).thenReturn(Optional.empty());
		
		//Act
		NoSuchElementException ex = assertThrows(NoSuchElementException.class, () -> invitationService.inviteUserToProject(dto, authUserId));
		
		//Assert
		
		assertTrue(ex.getMessage().toLowerCase().contains("proyecto no encontrado"));
		
		verifyNoInteractions(projectMemberService);
		
		verifyNoInteractions(invitationDao);
		
		verifyNoInteractions(usuarioService);
		
		
	}
	
	@Test
	void inviteUserToProject_debeLanzarSecurityException_siAuthUserNoEsOwner() {
		//Arrange
		
		Long authUserId = faker.number().randomNumber();
			
		
		InvitationDto dto = new InvitationDtoTestDataBuilder()				
				.build();
		
		Project project = new ProjectTestDataBuilder()
				.withIdGuid(dto.getProjectId())				
				.build();
		
		
		
		when(projectService.findByProjectId(dto.getProjectId())).thenReturn(Optional.of(project));
		
		when(projectMemberService.isOwner(authUserId, project.getIdGuid())).thenReturn(false);
		
		//Act
		SecurityException ex = assertThrows(SecurityException.class, () -> invitationService.inviteUserToProject(dto, authUserId));
		
		//Assert
		
		assertTrue(ex.getMessage().toLowerCase().contains("no tienes los permisos para realizar esta acccion"));
		
		verify(projectMemberService,never()).isMemberActive(anyLong(), anyString());
		
		verifyNoInteractions(invitationDao);
		
		verifyNoInteractions(usuarioService);
		
		
	}
	
	@Test
	void inviteUserToProject_debeLanzarIllegalStateException_siUsuarioYaEsMiembro() {
		//Arrange
		
		Long authUserId = faker.number().randomNumber();
			
		Usuario authUser = new UsuarioTestDataBuilder().withId(authUserId).build();
		
		InvitationDto dto = new InvitationDtoTestDataBuilder()				
				.build();
		
		Project project = new ProjectTestDataBuilder()
				.withIdGuid(dto.getProjectId())
				.withOwner(authUser)
				.build();
		
		
		
		when(projectService.findByProjectId(dto.getProjectId())).thenReturn(Optional.of(project));
		
		when(projectMemberService.isOwner(authUserId, project.getIdGuid())).thenReturn(true);
		
		when(projectMemberService.isMemberActive(dto.getUserGuestId(), project.getIdGuid())).thenReturn(true);
		
		//Act
		IllegalStateException ex = assertThrows(IllegalStateException.class, () -> invitationService.inviteUserToProject(dto, authUserId));
		
		//Assert
		
		assertTrue(ex.getMessage().toLowerCase().contains("el usuario a invitar ya es miembro"));
		
		
		verifyNoInteractions(invitationDao);
		
		verifyNoInteractions(usuarioService);
		
		
	}
	
	@Test
	void inviteUserToProject_debeLanzarIllegalStateException_siExisteInvitacionPendiente() {
		// Arrange
		Long authUserId = 10L;
		InvitationDto dto = new InvitationDtoTestDataBuilder().build();
		
		Usuario authUser = new UsuarioTestDataBuilder().withId(authUserId).build();
		
		
		Project project = new ProjectTestDataBuilder()
				.withIdGuid(dto.getProjectId())
				.withOwner(authUser)
				.build();
		

		when(projectService.findByProjectId(dto.getProjectId())).thenReturn(Optional.of(project));
		
		when(projectMemberService.isOwner(authUserId, project.getIdGuid())).thenReturn(true);
		
		when(projectMemberService.isMemberActive(dto.getUserGuestId(), project.getIdGuid())).thenReturn(false);
		
		
		
		when(invitationDao.existsByProjectIdGuidAndGuestIdAndStatus(
				dto.getProjectId(), dto.getUserGuestId(), Constants.STATUS_PENDING))
			.thenReturn(true);

		// Act 
		IllegalStateException ex = assertThrows(IllegalStateException.class, 
				() -> invitationService.inviteUserToProject(dto, authUserId));

		
		//Assert
		assertTrue(ex.getMessage().contains("Existe una invitacion pendiente"));
		
		verify(invitationDao, never()).save(any());
		
	}
	
	@Test
	void inviteUserToProject_debeLanzarNoSuchElementException_siAuthUserNoExiste() {
		// Arrange
		Long authUserId = 10L;
		InvitationDto dto = new InvitationDtoTestDataBuilder().build();
		
		Usuario authUser = new UsuarioTestDataBuilder().withId(authUserId).build();
		
		
		Project project = new ProjectTestDataBuilder()
				.withIdGuid(dto.getProjectId())
				.withOwner(authUser)
				.build();
		

		when(projectService.findByProjectId(dto.getProjectId())).thenReturn(Optional.of(project));
		
		when(projectMemberService.isOwner(authUserId, project.getIdGuid())).thenReturn(true);
		
		when(projectMemberService.isMemberActive(dto.getUserGuestId(), project.getIdGuid())).thenReturn(false);
		
		
		
		when(invitationDao.existsByProjectIdGuidAndGuestIdAndStatus(
				dto.getProjectId(), dto.getUserGuestId(), Constants.STATUS_PENDING))
			.thenReturn(false);
		
		when(usuarioService.findByUserId(authUserId)).thenThrow(new NoSuchElementException("Usuario no encontrado"));

		// Act 
		NoSuchElementException ex = assertThrows(NoSuchElementException.class, 
				() -> invitationService.inviteUserToProject(dto, authUserId));

		
		//Assert
		assertTrue(ex.getMessage().toLowerCase().contains("usuario no encontrado"));
		
		verify(usuarioService,never()).findByUserId(dto.getUserGuestId());
		
		verify(invitationDao, never()).save(any());
		
	}
	
	@Test
	void inviteUserToProject_debeLanzarNoSuchElementException_siGuestUserNoExiste() {
		// Arrange
		Long authUserId =  faker.number().randomNumber();
		
		
		InvitationDto dto = new InvitationDtoTestDataBuilder()
				.withUserHostId(authUserId)							
				.build();
		
		Usuario authUser = new UsuarioTestDataBuilder().withId(authUserId).build();
		
		
		Project project = new ProjectTestDataBuilder()
				.withIdGuid(dto.getProjectId())
				.withOwner(authUser)
				.build();
		

		when(projectService.findByProjectId(dto.getProjectId())).thenReturn(Optional.of(project));
		
		when(projectMemberService.isOwner(authUserId, project.getIdGuid())).thenReturn(true);
		
		when(projectMemberService.isMemberActive(dto.getUserGuestId(), project.getIdGuid())).thenReturn(false);
		
		
		
		when(invitationDao.existsByProjectIdGuidAndGuestIdAndStatus(
				dto.getProjectId(), dto.getUserGuestId(), Constants.STATUS_PENDING))
			.thenReturn(false);
		
		when(usuarioService.findByUserId(authUserId)).thenReturn(authUser);
		
		when(usuarioService.findByUserId(dto.getUserGuestId())).thenThrow(new NoSuchElementException("Usuario no encontrado"));

		// Act 
		NoSuchElementException ex = assertThrows(NoSuchElementException.class, 
				() -> invitationService.inviteUserToProject(dto, authUserId));

		
		//Assert
		assertTrue(ex.getMessage().toLowerCase().contains("usuario no encontrado"));
		
		
		verify(usuarioService).findByUserId(authUserId);
		verify(usuarioService).findByUserId(dto.getUserGuestId());
		
		verify(invitationDao, never()).save(any());
		
	}
	
	
	
	@Test
	void inviteUserToProject_debeGuardarInvitacion_cuandoDatosSonValidos() {
		// Arrange
		Long authUserId = faker.number().randomNumber();
		Long guestId = faker.number().randomNumber();
		
		InvitationDto dto = new InvitationDtoTestDataBuilder()
				.withUserHostId(authUserId)
				.withUserGuestId(guestId)
				.withRole(ProjectRole.EDITOR)
				.build();
		
		Project project = new ProjectTestDataBuilder().withIdGuid(dto.getProjectId()).build();
		
		Usuario owner = new UsuarioTestDataBuilder().withId(authUserId).build();
		Usuario guest = new UsuarioTestDataBuilder().withId(guestId).build();

		
		when(projectService.findByProjectId(dto.getProjectId())).thenReturn(Optional.of(project));
		when(projectMemberService.isOwner(authUserId, project.getIdGuid())).thenReturn(true);
		when(projectMemberService.isMemberActive(guestId, project.getIdGuid())).thenReturn(false);
		when(invitationDao.existsByProjectIdGuidAndGuestIdAndStatus(anyString(), anyLong(), anyShort())).thenReturn(false);
		
		
		when(usuarioService.findByUserId(authUserId)).thenReturn(owner);
		when(usuarioService.findByUserId(guestId)).thenReturn(guest);
		
		
		when(invitationDao.save(any(Invitation.class))).thenAnswer(inv -> {
			Invitation i = inv.getArgument(0);
			
			i.setFechaCreacion(LocalDateTime.now());
			i.setStatus(Constants.STATUS_PENDING);
			return i;
		});

		ArgumentCaptor<Invitation> captor = ArgumentCaptor.forClass(Invitation.class);

		// Act
		InvitationDto result = invitationService.inviteUserToProject(dto, authUserId);

		// Assert
		assertNotNull(result);
		assertEquals(project.getIdGuid(), result.getProjectId());
		
		verify(invitationDao).save(captor.capture());
		
		Invitation savedInvitation = captor.getValue();
		
		assertSame(owner, savedInvitation.getHost());
		assertSame(guest, savedInvitation.getGuest());
		assertSame(project, savedInvitation.getProject());
		assertEquals(ProjectRole.EDITOR, savedInvitation.getRole());
		assertNotNull(savedInvitation.getId()); 
	}
	
}
