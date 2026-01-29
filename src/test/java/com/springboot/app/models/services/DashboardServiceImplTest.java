package com.springboot.app.models.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.springboot.app.models.dtos.DashboardDto;
import com.springboot.app.models.entities.Usuario;
import com.springboot.app.testdata.UsuarioTestDataBuilder;

import static org.junit.jupiter.api.Assertions.assertEquals;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@ExtendWith(MockitoExtension.class)
class DashboardServiceImplTest {

	@Mock
    private IUsuarioService usuarioService;

    @Mock
    private StatsService statsService;

    @InjectMocks
    private DashboardServiceImpl dashboardService;

    @Test
    void getDashboardInfo_debeRetornarDtoCompleto_cuandoExistenEstadisticas() {
        // Arrange
        Long userId = 1L;
        Usuario usuario = new UsuarioTestDataBuilder().withId(userId).build();

       
        List<Object[]> statsStatus = new ArrayList<>();
        statsStatus.add(new Object[] { "PENDING", 5L });
        statsStatus.add(new Object[] { "DONE", 3L });

        
        List<Object[]> statsPriority = new ArrayList<>();
        statsPriority.add(new Object[] { "HIGH", 2L });
        statsPriority.add(new Object[] { "LOW", 8L });

        
        when(usuarioService.findByUserId(userId)).thenReturn(usuario);
        
        
        when(statsService.getProjectCountRoleOwner(userId)).thenReturn(2);
        when(statsService.getProjectCountActive(userId)).thenReturn(4);
        when(statsService.getTareasHoyCountByUserId(userId)).thenReturn(1);
        when(statsService.getTareasPendientesCountByUserId(userId)).thenReturn(10);
        when(statsService.getTareasVencidasCountByUserId(userId)).thenReturn(0);

        
        when(statsService.countTareasByTareaStatus(userId)).thenReturn(statsStatus);
        when(statsService.countTareasByPrioridad(userId)).thenReturn(statsPriority);

        // Act
        DashboardDto result = dashboardService.getDashboardInfo(userId);

        // Assert
        assertNotNull(result);
        assertEquals(userId, result.getUsuarioId());
        assertEquals(usuario.getUsername(), result.getUsername());
        
       
        assertEquals(2, result.getProyectosComoOwner());
        assertEquals(4, result.getProyectosTotalActivos());
        assertEquals(1, result.getTareasParaHoy());
        assertEquals(10, result.getTareasPendientes());
        assertEquals(0, result.getTareasVencidas());

        
        Map<String, Integer> mapStatus = result.getTareasPorEstatus();
        assertNotNull(mapStatus);
        assertEquals(2, mapStatus.size());
        assertEquals(5, mapStatus.get("PENDING"));
        assertEquals(3, mapStatus.get("DONE"));

        Map<String, Integer> mapPriority = result.getTareasPorPrioridad();
        assertNotNull(mapPriority);
        assertEquals(2, mapPriority.get("HIGH")); 
        assertEquals(8, mapPriority.get("LOW"));

        verify(statsService).countTareasByTareaStatus(userId);
        verify(statsService).countTareasByPrioridad(userId);
    }
    
    @Test
    void getDashboardInfo_debeRetornarCeros_cuandoNoHayEstadisticas() {
        // Arrange
        Long userId = 1L;
        Usuario usuario = new UsuarioTestDataBuilder().withId(userId).build();

       

        
        when(usuarioService.findByUserId(userId)).thenReturn(usuario);
        
        
        when(statsService.getProjectCountRoleOwner(userId)).thenReturn(0);
        when(statsService.getProjectCountActive(userId)).thenReturn(0);
        when(statsService.getTareasHoyCountByUserId(userId)).thenReturn(0);
        when(statsService.getTareasPendientesCountByUserId(userId)).thenReturn(0);
        when(statsService.getTareasVencidasCountByUserId(userId)).thenReturn(0);

        
        when(statsService.countTareasByTareaStatus(userId)).thenReturn(Collections.emptyList());
        when(statsService.countTareasByPrioridad(userId)).thenReturn(Collections.emptyList());

        // Act
        DashboardDto result = dashboardService.getDashboardInfo(userId);

        // Assert
        assertNotNull(result);
        assertEquals(userId, result.getUsuarioId());
        assertEquals(usuario.getUsername(), result.getUsername());
        
       
        assertEquals(0, result.getProyectosComoOwner());
        assertEquals(0, result.getProyectosTotalActivos());
        assertEquals(0, result.getTareasParaHoy());
        assertEquals(0, result.getTareasPendientes());
        assertEquals(0, result.getTareasVencidas());

        
        Map<String, Integer> mapStatus = result.getTareasPorEstatus();
        
        assertNotNull(mapStatus);
        assertEquals(0, mapStatus.size());


        Map<String, Integer> mapPriority = result.getTareasPorPrioridad();
        assertNotNull(mapPriority);
        assertEquals(0, mapPriority.size());

        verify(statsService).countTareasByTareaStatus(userId);
        verify(statsService).countTareasByPrioridad(userId);
    }
    
    @Test
    void getDashboardInfo_debeLanzarExcepcion_cuandoUsuarioNoExiste() {
        // Arrange
        Long userId = 99L;
        when(usuarioService.findByUserId(userId)).thenThrow(new NoSuchElementException("Usuario no encontrado"));

        // Act 
        assertThrows(NoSuchElementException.class, () -> dashboardService.getDashboardInfo(userId));

        // Assert
        verifyNoInteractions(statsService);
        
    }
	
}
