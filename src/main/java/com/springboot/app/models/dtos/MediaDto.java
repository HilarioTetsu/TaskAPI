package com.springboot.app.models.dtos;

import com.springboot.app.models.entities.Media;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MediaDto {

	public MediaDto(Media media) {
		this.id=media.getId();		
		this.ownerId=media.getOwnerId();
		this.sizeBytes=media.getSizeBytes();
		this.storageKey=media.getStorageKey();
		
	}
	
	
    private Long id;

    
    private Long ownerId;

    
    private String storageKey;

    
    private String originalName;

    
    private String mimeType;

    
    private Long sizeBytes;

    
    private String checksumSha256;


    
    private Short status; 
	
}
