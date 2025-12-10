package com.springboot.app.models.services;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.springboot.app.models.dao.IInvitationDao;
import com.springboot.app.models.dtos.InvitationDto;
import com.springboot.app.models.dtos.InvitationViewDto;
import com.springboot.app.models.dtos.ProjectMemberDto;
import com.springboot.app.models.entities.Invitation;
import com.springboot.app.models.entities.Project;
import com.springboot.app.models.entities.Usuario;
import com.springboot.app.utils.Constants;
import com.springboot.app.utils.ProjectRole;
import com.springboot.app.utils.Utils;

import io.jsonwebtoken.lang.Strings;

@Service
public class IInvitationServiceImpl implements IInvitationService {

	private final IInvitationDao invitationDao;
	private final IUsuarioService usuarioService;
	private final IProjectService projectService;
	private final IProjectMemberService projectMemberService;
	


	public IInvitationServiceImpl(IInvitationDao invitationDao, IUsuarioService usuarioService,
			IProjectService projectService, IProjectMemberService projectMemberService) {
		super();
		this.invitationDao = invitationDao;
		this.usuarioService = usuarioService;
		this.projectService = projectService;
		this.projectMemberService = projectMemberService;
	}

	@Override
	@Transactional
	public InvitationDto inviteUserToProject(InvitationDto invitationDto, Long authUserId) {
		
		Project project = projectService.findByProjectId(invitationDto.getProjectId()).orElseThrow(() -> new NoSuchElementException("Proyecto no encontrado"));
		
		if (!projectMemberService.isOwner(authUserId, project.getIdGuid())) {
			throw new SecurityException("No tienes los permisos para realizar esta acccion");
		}						
		
		if (projectMemberService.isMember(invitationDto.getUserGuestId(), project.getIdGuid())) {
			throw new IllegalStateException("El usuario a invitar ya es miembro");
		}
		
		if (!ProjectRole.existeRol(invitationDto.getRole().toString())) {
			throw new IllegalArgumentException("Rol invalido");
		}
		
		if (invitationDao.existsByProjectIdGuidAndGuestIdAndStatus(invitationDto.getProjectId(),invitationDto.getUserGuestId(),Constants.STATUS_PENDING)) {
			throw new IllegalStateException("Existe una invitacion pendiente de ese usuario");
		}
		
		
		Usuario owner=usuarioService.findByUserId(authUserId);
		
		Usuario guest=usuarioService.findByUserId(invitationDto.getUserGuestId());
		
		Invitation invitation =new Invitation();
		
		invitation.setId(UUID.randomUUID().toString());
		invitation.setHost(owner);
		invitation.setGuest(guest);
		invitation.setProject(project);
		invitation.setRole(invitationDto.getRole());
		
		
		
		return new InvitationDto(invitationDao.save(invitation));
	}

	@Override
	@Transactional
	public void confirmInvitationToProject(String invitationId, Short status, Long authUserId) {
						
		if (!invitationDao.existsByIdAndGuestId(invitationId,authUserId)) {
			throw new SecurityException("No tienes los permisos para aceptar la invitacion");
		}
		
		List<Short> statuses= Arrays.asList(Constants.STATUS_ACCEPTED,Constants.STATUS_REJECTED,Constants.STATUS_INACTIVE);
		
		Invitation invitation = findById(invitationId);
		
		if (statuses.contains(invitation.getStatus())) {
			throw new IllegalStateException("Esta invitacion ya ha sido confirmada o eliminada");
		}
		
		
		if (status.equals(Constants.STATUS_REJECTED)) {
			
			invitation.setStatus(Constants.STATUS_REJECTED);
			
			invitationDao.save(invitation);
			
			return;
		}
		
		if (status.equals(Constants.STATUS_ACCEPTED)) {
			
			invitation.setStatus(Constants.STATUS_ACCEPTED);
			
			invitationDao.save(invitation);
			
			ProjectMemberDto projectMemberDto = new ProjectMemberDto();
						
			projectMemberDto.setRole(invitation.getRole());
			projectMemberDto.setUsuarioId(invitation.getGuest().getId());
			
			projectMemberService.save(invitation.getProject().getIdGuid(), projectMemberDto, invitation.getHost().getId());
			
			
		}
		
		
	}

	@Override
	public Invitation findById(String invitationId) {
		
		return invitationDao.findById(invitationId).orElseThrow(() -> new NoSuchElementException("Invitacion no encontrada"));
	}

	@Override
	public Map<Short, String> getInvitationStatuses() {
		
		return Map.of(Constants.STATUS_INACTIVE,"INACTIVO",
					Constants.STATUS_PENDING,"PENDIENTE",
					Constants.STATUS_ACCEPTED,"ACEPTADA",
					Constants.STATUS_REJECTED,"RECHAZADA");
	}

	@Override
	public Page<InvitationViewDto> getAllInvitations(Long userAuthId, String status,Integer pagina, Integer tamanio,String sorts) {
		
		Short statusCode=null;
		
		if (Strings.hasText(status)) {
			 statusCode = getInvitationStatuses().entrySet()
						.stream()
						.filter(s -> s.getValue().equals(status))
						.map(e -> e.getKey()).findFirst()
						.orElseThrow(() -> new IllegalArgumentException("Status no valido"));
		}
		

		
		
		Pageable pageable=PageRequest.of(pagina, tamanio, Utils.parseSortParams(sorts));
		
		
		Page<Invitation> invitations= invitationDao.getAllInvitation(pageable,userAuthId,statusCode);
		
		return invitations.map(i -> new InvitationViewDto(i));
	}

}
