package com.springboot.app.models.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.springboot.app.models.entities.TareaTags;

@Repository
public interface ITareaTagsDao extends JpaRepository<TareaTags, Integer>{

}
