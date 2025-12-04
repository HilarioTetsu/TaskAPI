package com.springboot.app.models.dao;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.springboot.app.models.entities.Comment;

@Repository
public interface ICommentDao extends JpaRepository<Comment, Long>{
	
	
	@NativeQuery(value="SELECT * FROM comentarios c WHERE c.user_id=:userId ",countQuery = "SELECT COUNT(*) FROM comentarios c WHERE c.user_id=:userId ")
	Page<Comment> findAllByUserId(Pageable pageable, @Param("userId") Long userId);

	
	@NativeQuery(value="SELECT * FROM comentarios c WHERE c.user_id=:userId ")
	List<Comment>  findAllByUserId(@Param("userId") Long userId);

	@NativeQuery(value="SELECT * FROM comentarios c WHERE c.tarea_id=:tareaId ",countQuery = "SELECT COUNT(*) FROM comentarios c WHERE c.tarea_id=:tareaId ")
	Page<Comment> findAllByTareaId(Pageable pageable, @Param("tareaId") String tareaId);
	
	
	@NativeQuery(value="SELECT * FROM comentarios c WHERE c.tarea_id=:tareaId and c.status=1")
	List<Comment> findAllByTareaId( @Param("tareaId") String tareaId);
	
}
