package com.springboot.app.models.services;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.springboot.app.models.dao.IMediaDao;
import com.springboot.app.models.dtos.UploadRequestDto;
import com.springboot.app.models.dtos.UploadResponseDto;
import com.springboot.app.models.entities.Media;
import com.springboot.app.utils.Constants;
import com.springboot.app.utils.Utils;

@Service
public class MediaServiceImpl implements IMediaService {

	
	private final IMediaDao mediaDao;
	private final MediaStorageService storageService;
	


	public MediaServiceImpl(IMediaDao mediaDao, MediaStorageService storageService) {
		super();
		this.mediaDao = mediaDao;
		this.storageService = storageService;
	}



	@Override
	public Optional<Media> findByOwnerIdAndStorageKey(Long ownerId, String storageKey) {		
		return mediaDao.findByOwnerIdAndStorageKey(ownerId, storageKey);
	}



	@Override
	@Transactional
	public List<UploadResponseDto> createUploadUrls(List<UploadRequestDto> adjuntos, long maxSizeBytes,Long authUserId) {
		
		List<UploadResponseDto> urlResponses=new ArrayList<>();
		
		List<Media> listMedia=new ArrayList<>();
		
		for (UploadRequestDto item : adjuntos) {
			
			if(item.getSizeBytes()> maxSizeBytes)  throw new IllegalArgumentException("Archivo excede tamaño máximo: " + item.getFileName());
			
			
			String storageKey=storageService.buildStorageKey(authUserId, Utils.extensionFromName(item.getFileName()));
						
			
			URL url= storageService.createPresignedPutUrl(storageKey, item.getMime());
			
			urlResponses.add(new UploadResponseDto(url.toString(), storageKey));
			
			Media media = new Media();

			media.setOriginalName(item.getFileName());
			media.setMimeType(item.getMime());
			media.setOwnerId(authUserId);
			media.setStorageKey(storageKey);
			
			
			listMedia.add(media);
			
			
		}
		
		mediaDao.saveAll(listMedia);
		
		
		
		return urlResponses;
	}



	@Override
	@Transactional
	public List<Media> updateStatusMedia(List<String> confirmMediaStorageKeyId, Long authUserId) {
		
		List<Media> savedMedias = new ArrayList<>();
		
		for (String key : confirmMediaStorageKeyId) {
			
			Media media = findByOwnerIdAndStorageKey(authUserId, key).orElseThrow(() -> new NoSuchElementException(key));
			
			if (media.getOwnerId()!=authUserId) {
				throw new SecurityException("No tiene los permisos necesarios sobre el fichero");
			}
			
			media.setStatus(Constants.STATUS_READY);			
			media.setSizeBytes(storageService.headObject(key).contentLength());
			
			savedMedias.add(media);
			
		}
		
		return mediaDao.saveAll(savedMedias);
		
	}



	@Override
	public List<Media> saveAll(List<Media> mediaInactive) {
		
		return mediaDao.saveAll(mediaInactive);
	}



	@Override
	public List<String> createPresignedGetUrls(List<String> storageKeys) {
		
		return storageService.createPresignedGetUrls(storageKeys);
	}

}
