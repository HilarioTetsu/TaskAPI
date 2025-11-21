package com.springboot.app.models.dtos;

import org.hibernate.validator.constraints.Length;

import com.springboot.app.models.entities.Tag;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TagDto {

	
	public TagDto(Tag tag) {
		
		this.id=tag.getId();
		this.name=tag.getName();
		this.color=tag.getColor();
		this.status=tag.getStatus();
	}
	
	
	private Integer id;
	
	@NotBlank
	@Length(max = 30)
	private String name;
	
	@NotBlank
	@Length(min = 7,max = 7)
	private String color;
	
	
	private Short status;
}
