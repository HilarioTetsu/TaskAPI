package com.springboot.app.models.dtos;

import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(
    name = "DashboardDto",
    description = "Resumen consolidado de la actividad del usuario: proyectos y tareas agrupadas por métricas clave."
)
public class DashboardDto {

    @Schema(
        description = "ID interno del usuario dueño del dashboard.",
        example = "15"
    )
    private Long usuarioId;

    @Schema(
        description = "Nombre de usuario (username) asociado a la información del dashboard.",
        example = "team.master"
    )
    private String username;

    @Schema(
        description = "Número total de proyectos activos en los que participa el usuario (como owner o miembro).",
        example = "8"
    )
    private long proyectosTotalActivos;

    @Schema(
        description = "Cantidad de proyectos en los que el usuario es owner.",
        example = "3"
    )
    private long proyectosComoOwner;

    @Schema(
        description = "Cantidad de tareas pendientes (no completadas) asignadas al usuario.",
        example = "21"
    )
    private long tareasPendientes;

    @Schema(
        description = "Cantidad de tareas vencidas (fecha límite pasada y sin completar).",
        example = "4"
    )
    private long tareasVencidas;

    @Schema(
        description = "Cantidad de tareas cuya fecha límite es hoy.",
        example = "5"
    )
    private long tareasParaHoy;

    @Schema(
        description = "Mapa de conteos de tareas agrupadas por prioridad. Llave = nombre de prioridad, valor = cantidad.",
        example = "{\"ALTA\": 10, \"MEDIA\": 5, \"BAJA\": 3}"
    )
    private Map<String, Integer> tareasPorPrioridad;

    @Schema(
        description = "Mapa de conteos de tareas agrupadas por estatus. Llave = nombre del estatus, valor = cantidad.",
        example = "{\"PENDIENTE\": 12, \"EN_PROGRESO\": 4, \"COMPLETADA\": 7}"
    )
    private Map<String, Integer> tareasPorEstatus;

}
