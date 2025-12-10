package com.springboot.app.models.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.springboot.app.models.entities.Invitation;

@Repository
public interface IInvitationDao extends JpaRepository<Invitation, String>{

	
	
	
	boolean existsByProjectIdGuidAndGuestIdAndStatus(String projectId, Long userGuestId, Short status);

	boolean existsByIdAndGuestId(String invitationId, Long authUserId);


}
