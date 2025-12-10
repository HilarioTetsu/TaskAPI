package com.springboot.app.controllers;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.springboot.app.models.dtos.InvitationDto;
import com.springboot.app.models.services.IInvitationService;
import com.springboot.app.utils.Constants;
import com.springboot.app.utils.CustomUserDetails;
import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;



@RestController
@RequestMapping(Constants.URL_BASE_API_V1+"/invitations")
public class InvitationController {

	private final IInvitationService invitationService;
		
	
	public InvitationController(IInvitationService invitationService) {
		super();
		this.invitationService = invitationService;
	}


	@GetMapping("/statuses")
	public ResponseEntity<Map<Short,String>> getInvitationStatuses(){
		
		
		
		return ResponseEntity.ok(invitationService.getInvitationStatuses());
		
	}
	
	
	@PostMapping
	public ResponseEntity<InvitationDto> inviteUserToProject(@RequestBody @Valid InvitationDto dto,
			@AuthenticationPrincipal CustomUserDetails authUser){
		
		
		
		return ResponseEntity.ok(invitationService.inviteUserToProject(dto,authUser.getUserId()));
		
	}
	

	@PatchMapping("/{invitationId}/{status}")
	public ResponseEntity<Void> confirmInvitation(@PathVariable(required = true) Short status,
			@PathVariable(required = true) String invitationId,
			@AuthenticationPrincipal CustomUserDetails authUser){
		
		invitationService.confirmInvitationToProject(invitationId,status,authUser.getUserId());
		
		return ResponseEntity.noContent().build();
		
	}
	
	
	
}
