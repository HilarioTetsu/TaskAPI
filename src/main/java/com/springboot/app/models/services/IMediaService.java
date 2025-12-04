package com.springboot.app.models.services;

import java.util.List;
import java.util.Optional;

import com.springboot.app.models.dtos.UploadRequestDto;
import com.springboot.app.models.dtos.UploadResponseDto;
import com.springboot.app.models.entities.Media;

public interface IMediaService {

	Optional<Media> findByOwnerIdAndStorageKey(Long ownerId, String storageKey);

	List<UploadResponseDto> createUploadUrls(List<UploadRequestDto> adjuntos, long maxSizeBytes, Long authUser);

	List<Media> updateStatusMedia(List<String> confirmMediaStorageKeyId, Long authUserId);

	List<Media> saveAll(List<Media> mediaInactive);
	
	
	List<String> createPresignedGetUrls(List<String> storageKeys);
	
}
