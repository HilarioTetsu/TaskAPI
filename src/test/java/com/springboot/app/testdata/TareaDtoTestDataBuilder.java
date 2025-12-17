package com.springboot.app.testdata;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.github.javafaker.Faker;
import com.springboot.app.models.dtos.TagDto;
import com.springboot.app.models.dtos.TareaDto;
import com.springboot.app.utils.Constants;

public class TareaDtoTestDataBuilder {

    private static final Faker faker = new Faker();

    
    private String idGuid = UUID.randomUUID().toString(); 
    private String titulo = faker.book().title();
    private String descripcion = faker.lorem().paragraph();


    private Short idTareaStatus = 1;
    private Short idPrioridad = 2;
    private String projectId = null;


    private List<TagDto> listTag = null;
    private LocalDateTime fechaLimite = LocalDateTime.now().plusDays(5);


    private Short status = Constants.STATUS_ACTIVE;

    
    
    public TareaDtoTestDataBuilder withIdGuid(String idGuid) {
        this.idGuid = idGuid;
        return this;
    }

    public TareaDtoTestDataBuilder withRandomIdGuid() {
        this.idGuid = UUID.randomUUID().toString();
        return this;
    }

    public TareaDtoTestDataBuilder withTitulo(String titulo) {
        this.titulo = titulo;
        return this;
    }

    public TareaDtoTestDataBuilder withDescripcion(String descripcion) {
        this.descripcion = descripcion;
        return this;
    }

    public TareaDtoTestDataBuilder withTareaStatus(Short idTareaStatus) {
        this.idTareaStatus = idTareaStatus;
        return this;
    }

    public TareaDtoTestDataBuilder withPrioridad(Short idPrioridad) {
        this.idPrioridad = idPrioridad;
        return this;
    }

    public TareaDtoTestDataBuilder withProjectId(String projectId) {
        this.projectId = projectId;
        return this;
    }

    public TareaDtoTestDataBuilder withRandomProjectId() {
        this.projectId = UUID.randomUUID().toString();
        return this;
    }

    public TareaDtoTestDataBuilder withTags(List<TagDto> listTag) {
        this.listTag = listTag;
        return this;
    }

    public TareaDtoTestDataBuilder withFechaLimite(LocalDateTime fechaLimite) {
        this.fechaLimite = fechaLimite;
        return this;
    }

    public TareaDtoTestDataBuilder withStatus(Short status) {
        this.status = status;
        return this;
    }


    public TareaDto build() {
        TareaDto dto = new TareaDto();

        dto.setIdGuid(idGuid);
        dto.setTitulo(titulo);
        dto.setDescripcion(descripcion);
        dto.setId_tarea_status(idTareaStatus);
        dto.setId_prioridad(idPrioridad);
        dto.setProject_id(projectId);
        dto.setListTag(listTag);
        dto.setFechaLimite(fechaLimite);
        dto.setStatus(status);

        return dto;
    }
}
