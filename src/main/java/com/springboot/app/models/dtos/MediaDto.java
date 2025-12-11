package com.springboot.app.models.dtos;

import com.springboot.app.models.entities.Media;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(
    name = "MediaDto",
    description = "Información de un archivo multimedia almacenado en el sistema (por ejemplo, comentario adjunto)."
)
public class MediaDto {

    public MediaDto(Media media) {
        this.id = media.getId();
        this.ownerId = media.getOwnerId();
        this.sizeBytes = media.getSizeBytes();
        this.storageKey = media.getStorageKey();
    }

    @Schema(
        description = "ID interno del registro de media.",
        example = "42"
    )
    private Long id;

    @Schema(
        description = "ID del usuario dueño del archivo.",
        example = "15"
    )
    private Long ownerId;

    @Schema(
        description = "Clave interna de almacenamiento (por ejemplo, key en S3).",
        example = "comments/123/attachments/2025/12/file-abc123.png"
    )
    private String storageKey;

    @Schema(
        description = "Nombre original del archivo cargado por el usuario.",
        example = "captura-pantalla.png"
    )
    private String originalName;

    @Schema(
        description = "Tipo MIME del archivo.",
        example = "image/png"
    )
    private String mimeType;

    @Schema(
        description = "Tamaño del archivo en bytes.",
        example = "204800"
    )
    private Long sizeBytes;

    @Schema(
        description = "Checksum SHA-256 del contenido del archivo (para verificación de integridad).",
        example = "b1946ac92492d2347c6235b4d2611184..."
    )
    private String checksumSha256;

    @Schema(
        description = "Estatus lógico del registro de media (1 = activo, 0 = eliminado).",
        example = "1"
    )
    private Short status;
}
