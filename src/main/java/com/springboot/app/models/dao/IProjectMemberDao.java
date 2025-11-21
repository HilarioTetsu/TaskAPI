package com.springboot.app.models.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.springboot.app.models.entities.ProjectMember;
import com.springboot.app.utils.ProjectRole;

@Repository
public interface IProjectMemberDao extends JpaRepository<ProjectMember, Long>{

		

	boolean existsByUsuarioIdAndProjectIdGuidAndRoleIsAndStatusIs(Long userId, String projectId, ProjectRole owner,
			Short statusActive);
	
	@Query("""
	        SELECT CASE WHEN COUNT(pm) > 0 THEN true ELSE false END
	        FROM ProjectMember pm
	        WHERE pm.usuario.id = :usuarioId
	          AND pm.project.id = :projectId
	          AND pm.role IN (:roles)
	          AND pm.status = :statusActivo
	    """)
	    boolean existsByUsuarioAndProjectWithRoles(
	        @Param("usuarioId") Long usuarioId,
	        @Param("projectId") String projectId,
	        @Param("roles") List<ProjectRole> roles,
	        @Param("statusActivo") Short statusActivo
	    );

	boolean existsByUsuarioIdAndProjectIdGuidAndStatusIs(Long usuarioId, String projectId, Short statusActive);
	
	Optional<ProjectMember> findByIdAndStatusIs(Long id,Short status);
	
	Optional<ProjectMember> findByUsuarioIdAndProjectIdGuidAndStatusIs(Long usuarioId, String projectId, Short statusActive);

	Optional<ProjectMember> findByUsuarioIdAndProjectIdGuid(Long userId, String projectId);

	List<ProjectMember> findByProjectIdGuid(String projectId);

	boolean existsByUsuarioIdAndProjectIdGuid(Long usuarioId, String projectId);

	
	@NativeQuery("SELECT COUNT(*) FROM project_members pm WHERE pm.role ='OWNER' ")
	int getCountOwners(String projectId);

}

