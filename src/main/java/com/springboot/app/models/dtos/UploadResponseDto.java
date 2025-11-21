package com.springboot.app.models.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UploadResponseDto {

	private String uploadUrl;
	
	private String storageKey;
	
}
