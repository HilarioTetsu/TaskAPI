package com.springboot.app.models.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.stereotype.Repository;
import com.springboot.app.models.entities.Tag;

@Repository
public interface ITagDao extends JpaRepository<Tag, Integer>{

	@NativeQuery(value = "SELECT * FROM tags t WHERE t.status=1;",countQuery = "SELECT COUNT(*) FROM tags t WHERE t.status=1 ")
	Page<Tag> getAll(Pageable pageable);
	
	List<Tag> findByNameContaining(String nameTag);
	
	@NativeQuery(value = "SELECT * FROM tags t WHERE (?1 IS NULL OR t.id= ?1) AND (?2 IS NULL OR UPPER(t.name) LIKE ?2) AND t.status = 1; ")
	Optional<Tag> getTagActiveByIdOrName(Integer id,String nameTag);
	
}
