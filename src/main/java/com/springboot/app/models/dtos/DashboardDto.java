package com.springboot.app.models.dtos;

import java.util.Map;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DashboardDto {

	
    private Long usuarioId;
    private String username;

    
    private long proyectosTotalActivos;
    private long proyectosComoOwner;
    
    
    private long tareasPendientes;      
    private long tareasVencidas;      
    private long tareasParaHoy;    

    
    private Map<String, Integer> tareasPorPrioridad;
    
    private Map<String, Integer> tareasPorEstatus;
	
	
}
