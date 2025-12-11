package com.springboot.app.models.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Schema(
    name = "UploadResponseDto",
    description = "Respuesta al solicitar una subida: contiene la URL prefirmada y la clave de almacenamiento."
)
public class UploadResponseDto {

    @Schema(
        description = "URL prefirmada donde el cliente debe subir el archivo (generalmente un PUT).",
        example = "https://s3.amazonaws.com/bucket/comments/123/attachments/file-abc123.png?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=..."
    )
    private String uploadUrl;

    @Schema(
        description = "Clave de almacenamiento que identificará al archivo en el backend (por ejemplo, key en S3).",
        example = "comments/123/attachments/2025/12/file-abc123.png"
    )
    private String storageKey;

}
