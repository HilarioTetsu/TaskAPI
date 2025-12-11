package com.springboot.app.models.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(
    name = "UploadRequestDto",
    description = "Datos del archivo que se va a subir, usados para generar una URL prefirmada de carga."
)
public class UploadRequestDto {

    @Schema(
        description = "Nombre de archivo que se desea subir (sugerencia para construir la key).",
        example = "evidencia-tarea-123.png"
    )
    private String fileName;

    @Schema(
        description = "Tipo MIME del archivo que se subirá.",
        example = "image/png"
    )
    private String mime;

    @Schema(
        description = "Tamaño del archivo en bytes (puede utilizarse para validaciones de límite de tamaño).",
        example = "307200"
    )
    long sizeBytes;

}
