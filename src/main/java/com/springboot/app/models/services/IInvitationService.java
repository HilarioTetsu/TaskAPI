package com.springboot.app.models.services;

import java.util.Map;

import org.springframework.data.domain.Page;

import com.springboot.app.models.dtos.InvitationDto;
import com.springboot.app.models.dtos.InvitationViewDto;
import com.springboot.app.models.entities.Invitation;

public interface IInvitationService {

	InvitationDto inviteUserToProject(InvitationDto invitation, Long authUserId);
	
	void confirmInvitationToProject(String invitationId, Short status, Long authUserId);

	Map<Short,String> getInvitationStatuses();
	
	Invitation findById(String invitationId);

	Page<InvitationViewDto> getAllInvitations(Long userAuthId, String status,Integer pagina, Integer tamanio, String sorts);
	
}
