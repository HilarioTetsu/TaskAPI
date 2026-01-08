package com.springboot.app.models.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.springboot.app.models.entities.Usuario;

@Repository
public interface IUsuarioDao extends JpaRepository<Usuario, Long>{

	@NativeQuery("select * from usuarios u where (u.email=?1 or u.username=?2) \r\n"
			+ "and u.status =?3 ;")
	Optional<Usuario> findByEmailOrUsernameAndStatusIs(String email,String username,Short status);
	
	List<Usuario> findByStatusIs(Short status);

	boolean existsByIdAndStatusIs(Long id, Short statusActive);

	Optional<Usuario> findByIdAndStatus(Long userId, Short statusActive);

	List<Usuario> findByUsernameContaining(String term);


	boolean existsByEmailIs(String email);

	boolean existsByUsernameIs(String username);

	
	@NativeQuery("select u.username from usuarios u where u.id=?1")
	String findUsernameById(Long Id);

	List<Usuario> findByUsernameContainingOrEmailContaining(String username,String email);

	
	@NativeQuery("""
			
	select
		u.*
	from
		usuarios u
	inner join project_members pm 
	on
		pm.usuario_id = u.id
	where
		u.username like :term
		and pm.project_id = :projectId
		and pm.status = 1
		and u.status = 1 ;
			""")
	List<Usuario> findByUsernameContainingAndProjectId(@Param("term") String term, @Param("projectId") String projectId);
	
}
