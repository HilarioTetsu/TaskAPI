package com.springboot.app.models.services;

import java.util.Map;

import com.springboot.app.models.dtos.InvitationDto;
import com.springboot.app.models.entities.Invitation;

public interface IInvitationService {

	InvitationDto inviteUserToProject(InvitationDto invitation, Long authUserId);
	
	void confirmInvitationToProject(String invitationId, Short status, Long authUserId);

	Map<Short,String> getInvitationStatuses();
	
	Invitation findById(String invitationId);
	
}
