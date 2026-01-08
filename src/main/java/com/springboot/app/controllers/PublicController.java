package com.springboot.app.controllers;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RequestMapping("/public")
@Tag(name = "Public / Health", description = "Endpoints de monitoreo y verificación de estado.")
@RestController
public class PublicController {

	
	@Operation(
	        summary = "Verificar estado del servidor (Health Check)",
	        description = """
	                Retorna el estado actual del servicio y la hora del servidor.
	                Útil para:
	                - Validar conexión a internet/red.
	                - Verificar sincronización de reloj (importante para JWT).
	                - Evitar 'falsos positivos' de caché gracias al timestamp cambiante.
	                """
	    )
	    @ApiResponses({
	        @ApiResponse(
	            responseCode = "200",
	            description = "Servidor operativo.",
	            content = @Content(
	                mediaType = "application/json",
	                schema = @Schema(example = "{\"status\": \"UP\", \"timestamp\": \"2023-10-27T10:30:45.123\", \"service\": \"TaskAPI\"}")
	            )
	        )
	    })
	    @GetMapping("/health")
	    public ResponseEntity<Map<String, Object>> healthCheck() {
	        return ResponseEntity.ok(Map.of(
	            "status", "UP",
	            "service", "TaskAPI",
	            "timestamp", LocalDateTime.now()
	        ));
	    }
	
}
