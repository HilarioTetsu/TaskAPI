package com.springboot.app.models.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.springboot.app.models.entities.TareaTags;

public interface ITareaTagsDao extends JpaRepository<TareaTags, Integer>{

}
