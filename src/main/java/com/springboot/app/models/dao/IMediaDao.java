package com.springboot.app.models.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.springboot.app.models.entities.Media;

@Repository
public interface IMediaDao extends JpaRepository<Media, Long>{

	Optional<Media> findByOwnerIdAndStorageKey(Long ownerId, String storageKey);
}
