package com.springboot.app.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.springboot.app.models.dtos.CommentDto;
import com.springboot.app.models.dtos.CommentUpdateDto;
import com.springboot.app.models.dtos.UploadRequestDto;
import com.springboot.app.models.dtos.UploadResponseDto;
import com.springboot.app.models.entities.Media;
import com.springboot.app.models.services.ICommentService;
import com.springboot.app.models.services.IMediaService;
import com.springboot.app.models.services.MediaStorageService;
import com.springboot.app.utils.Constants;
import com.springboot.app.utils.CustomUserDetails;

import jakarta.validation.constraints.NotNull;

@RestController
@RequestMapping(Constants.URL_BASE_API_V1+"/comment")
@Validated
public class CommentController {

	private final ICommentService commentService;
	
	private final MediaStorageService storageService;
	
	private final IMediaService mediaService;
	
	@Value("${app.s3.maxSizeBytes}") long maxSizeBytes;



	public CommentController(ICommentService commentService, MediaStorageService storageService,
			IMediaService mediaService) {
		super();
		this.commentService = commentService;
		this.storageService = storageService;
		this.mediaService = mediaService;
	}


	
	@GetMapping
	public ResponseEntity<?> getAllComments(@RequestParam(required = false, defaultValue = "0") Integer pagina,
			@RequestParam(required = false, defaultValue = "5") Integer tamanio,
			@RequestParam(required = false, defaultValue = "fecha_creacion,desc;") String sorts,
			@RequestParam(required = false) String tareaId,
			@AuthenticationPrincipal CustomUserDetails authUser){
				
	
					if (StringUtils.hasText(tareaId)) {
						
						return ResponseEntity.ok().body(commentService.getAllByTareaId(pagina,tamanio,sorts,authUser.getUserId(),tareaId));
						
					}
			
			return ResponseEntity.ok().body(commentService.getAll(pagina,tamanio,sorts,authUser.getUserId()));
		

	}
	

	@PostMapping("/attachment/presign")
	public ResponseEntity<List<UploadResponseDto>> createUploadUrl(@RequestBody List<UploadRequestDto> adjuntos,
			@AuthenticationPrincipal CustomUserDetails authUser){
				
	
			if (adjuntos.size()>10) throw new IllegalArgumentException("Máximo 10 archivos");
			
			List<UploadResponseDto> urlResponses= mediaService.createUploadUrls(adjuntos,maxSizeBytes,authUser.getUserId());
										
			
			return ResponseEntity.ok().body(urlResponses);
		

		
	}
	
	@PostMapping
	public ResponseEntity<CommentDto> createCommAndConfirmAttachments(@RequestBody CommentDto dto,
			@AuthenticationPrincipal CustomUserDetails authUser){
				
	
			if (dto.getConfirmMediaStorageKeyId()==null || dto.getConfirmMediaStorageKeyId().size()==0) {
				return ResponseEntity.ok().body(commentService.saveComment(dto,authUser.getUserId()));
			}
			
			if (!storageService.verifySizeFiles(dto.getConfirmMediaStorageKeyId())) {
				throw new IllegalArgumentException("Archivo excede el límite de 30MB");
			}	
			
			List<Media> mediasSaved = mediaService.updateStatusMedia(dto.getConfirmMediaStorageKeyId(),authUser.getUserId());
			
			return ResponseEntity.status(HttpStatus.CREATED).body(commentService.saveComment(dto,mediasSaved,authUser.getUserId()));
						 			 

	}
	
	
	@DeleteMapping("/{commentId}")
	public ResponseEntity<Void> deleteComment(@PathVariable @NotNull Long commentId,
			@AuthenticationPrincipal CustomUserDetails authUser){

		
		
			commentService.deleteComment(commentId,authUser.getUserId());
			
			return ResponseEntity.noContent().build();
			
			
	
	}
	
	
	@PatchMapping("/{commentId}")
	public ResponseEntity<CommentDto> updateComment(@RequestBody CommentUpdateDto dto,
			@PathVariable Long commentId,
			@AuthenticationPrincipal CustomUserDetails authUser){
				

			return ResponseEntity.ok().body(commentService.updateComment(commentId,dto,authUser.getUserId()));
		

		
	}
	
	
}
