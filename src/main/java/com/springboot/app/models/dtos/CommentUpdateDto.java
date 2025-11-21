package com.springboot.app.models.dtos;


import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;


@Data
public class CommentUpdateDto {


	@NotBlank
	private String body;
	
	private List<Long> mentionsUserIds;
	
}
