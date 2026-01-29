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

import java.util.NoSuchElementException;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.github.javafaker.Faker;
import com.springboot.app.models.dao.IInvitationDao;
import com.springboot.app.models.dtos.InvitationDto;
import com.springboot.app.testdata.InvitationDtoTestDataBuilder;


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
	
}
