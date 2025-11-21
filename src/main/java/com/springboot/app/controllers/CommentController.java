package com.springboot.app.controllers;

import java.util.List;
import java.util.NoSuchElementException;

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

import jakarta.validation.Valid;
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
				
		try {
		
					if (StringUtils.hasText(tareaId)) {
						
						return ResponseEntity.ok().body(commentService.getAllByTareaId(pagina,tamanio,sorts,authUser.getUserId(),tareaId));
						
					}
			
			return ResponseEntity.ok().body(commentService.getAll(pagina,tamanio,sorts,authUser.getUserId()));
		
		} catch (IllegalArgumentException e) {
	        return ResponseEntity.badRequest().body(e.getMessage());	    
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<Object>("Error al obtener la informacion", HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		
	}
	

	@PostMapping("/attachment/presign")
	public ResponseEntity<?> createUploadUrl(@RequestBody List<UploadRequestDto> adjuntos,
			@AuthenticationPrincipal CustomUserDetails authUser){
				
		try {
		
			if (adjuntos.size()>10) throw new IllegalArgumentException("Máximo 10 archivos");
			
			List<UploadResponseDto> urlResponses= mediaService.createUploadUrls(adjuntos,maxSizeBytes,authUser.getUserId());
										
			
			return ResponseEntity.ok().body(urlResponses);
		
		} catch (IllegalArgumentException e) {
	        return ResponseEntity.badRequest().body(e.getMessage());	    
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<Object>("Error al obtener la informacion", HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		
	}
	
	@PostMapping
	public ResponseEntity<?> createCommAndConfirmAttachments(@RequestBody CommentDto dto,
			@AuthenticationPrincipal CustomUserDetails authUser){
				
		try {
		
			if (dto.getConfirmMediaStorageKeyId()==null || dto.getConfirmMediaStorageKeyId().size()==0) {
				return ResponseEntity.ok().body(commentService.saveComment(dto,authUser.getUserId()));
			}
			
			if (!storageService.verifySizeFiles(dto.getConfirmMediaStorageKeyId())) {
				throw new IllegalArgumentException("Archivo excede el límite de 30MB");
			}	
			
			List<Media> mediasSaved = mediaService.updateStatusMedia(dto.getConfirmMediaStorageKeyId(),authUser.getUserId());
			
			return ResponseEntity.status(HttpStatus.CREATED).body(commentService.saveComment(dto,mediasSaved,authUser.getUserId()));
						 
			 
		} catch (IllegalArgumentException e) {
	        return ResponseEntity.badRequest().body(e.getMessage());
	    } catch (SecurityException e) {
	        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
	    } catch (NoSuchElementException e) {
	        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
	    } catch (IllegalStateException e) {
	        return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
	    } catch (Exception e) {
	        e.printStackTrace();
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body("Error en el servidor");
	    }
	}
	
	
	@DeleteMapping("/{commentId}")
	public ResponseEntity<?> deleteComment(@PathVariable @NotNull Long commentId,
			@AuthenticationPrincipal CustomUserDetails authUser){
				
		try {
		
			commentService.deleteComment(commentId,authUser.getUserId());
			
			return ResponseEntity.noContent().build();
		
		} catch (IllegalArgumentException e) {
	        return ResponseEntity.badRequest().body(e.getMessage());
	    } catch (SecurityException e) {
	        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
	    } catch (NoSuchElementException e) {
	        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
	    } catch (IllegalStateException e) {
	        return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
	    } catch (Exception e) {
	        e.printStackTrace();
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body("Error en el servidor");
	    }
	
	}
	
	
	@PatchMapping("/{commentId}")
	public ResponseEntity<?> updateComment(@RequestBody CommentUpdateDto dto,
			@PathVariable Long commentId,
			@AuthenticationPrincipal CustomUserDetails authUser){
				
		try {
		
			
			
			return ResponseEntity.ok().body(commentService.updateComment(commentId,dto,authUser.getUserId()));
		
		} catch (IllegalArgumentException e) {
	        return ResponseEntity.badRequest().body(e.getMessage());
	    } catch (SecurityException e) {
	        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
	    } catch (NoSuchElementException e) {
	        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
	    } catch (IllegalStateException e) {
	        return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
	    } catch (Exception e) {
	        e.printStackTrace();
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body("Error en el servidor");
	    }
		
	}
	
	
}
