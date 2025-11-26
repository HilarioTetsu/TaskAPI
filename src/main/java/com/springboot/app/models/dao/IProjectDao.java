package com.springboot.app.models.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.stereotype.Repository;

import com.springboot.app.models.entities.Project;

@Repository
public interface IProjectDao extends JpaRepository<Project, String>{

	@NativeQuery("SELECT p.* from project p WHERE p.owner_id=?1 AND p.status=?2 ;")
	List<Project> findByOwnerId(Long userId,Short status);

	@NativeQuery("SELECT p.* FROM project p WHERE p.id_guid=?1 AND p.status=?2 ;")
	Optional<Project> findProjectActiveById(String id,Short status);

	@NativeQuery("SELECT p.* FROM project p WHERE p.id_guid=?1 AND p.owner_id=?2 ;")
	Optional<Project> findByIdAndUserId(String id, Long userId);

	boolean existsByIdGuidAndStatusIs(String idGuid,Short status);

	Optional<Project> findByIdGuidAndStatusIs(String id, Short statusActive);
	
	
	@NativeQuery("""
	select
		count(*) proyectos_activos
	from
		project_members pm
	join project p on
		p.id_guid = pm.project_id
	where
		pm.usuario_id = ?1
		and pm.status = 1
		and p.status = 1
						""")
	int getProjectCountActive(Long userId);
	
	@NativeQuery(""" 
				select
		count(*) proyectos_owner
	from
		project_members pm
	join project p on
		p.id_guid = pm.project_id
	where
		pm.usuario_id = ?1
		and pm.role = 'OWNER'
		and pm.status = 1
		and p.status = 1
		
				""")
	int getProjectCountRoleOwner(Long userId);
}
