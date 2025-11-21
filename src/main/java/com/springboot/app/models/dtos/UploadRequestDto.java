package com.springboot.app.models.dtos;

import lombok.Data;

@Data
public class UploadRequestDto {

	private String fileName;
	
	private String mime;
	
	long sizeBytes;
	
}
