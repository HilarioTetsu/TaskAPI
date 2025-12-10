package com.springboot.app.models.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.springboot.app.models.entities.Invitation;

@Repository
public interface IInvitationDao extends JpaRepository<Invitation, String>{

	
	String QUERY = "SELECT * FROM invitations i ";
	
	String FILTERS = """ 
			WHERE (:userId IS NULL OR i.user_guest_id=:userId)
			AND (:status IS NULL OR i.status=:status)
			""";
	
	boolean existsByProjectIdGuidAndGuestIdAndStatus(String projectId, Long userGuestId, Short status);

	boolean existsByIdAndGuestId(String invitationId, Long authUserId);

	
	
	@NativeQuery(value = QUERY + FILTERS,
			countQuery = "SELECT COUNT(*) FROM invitations i "+FILTERS)
	Page<Invitation> getAllInvitation(Pageable pageable, @Param("userId") Long userAuthId, @Param("status") Short statusCode);


}
