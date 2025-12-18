package com.springboot.app.testdata;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import com.github.javafaker.Faker;
import com.springboot.app.models.entities.Project;
import com.springboot.app.models.entities.Usuario;
import com.springboot.app.utils.Constants;

public class ProjectTestDataBuilder {

    private static final Faker faker = new Faker();

    
    private String idGuid = UUID.randomUUID().toString(); 
    private String name = faker.company().name();
    private String descripcion = faker.lorem().paragraph();

    private Usuario owner = new UsuarioTestDataBuilder()
			.withId(faker.number().numberBetween(0, Long.MAX_VALUE)).build();

  

    
    private Short status = Constants.STATUS_ACTIVE;
    private LocalDateTime fechaCreacion = LocalDateTime.now();
    private LocalDateTime fechaModificacion = null;
    private String usuarioModificacion = null;

    
    public ProjectTestDataBuilder withIdGuid(String idGuid) {
        this.idGuid = idGuid;
        return this;
    }

    public ProjectTestDataBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public ProjectTestDataBuilder withDescripcion(String descripcion) {
        this.descripcion = descripcion;
        return this;
    }

    public ProjectTestDataBuilder withOwner(Usuario owner) {
        this.owner = owner;
        return this;
    }

    public ProjectTestDataBuilder withStatus(Short status) {
        this.status = status;
        return this;
    }

    public ProjectTestDataBuilder withFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
        return this;
    }

    public ProjectTestDataBuilder withFechaModificacion(LocalDateTime fechaModificacion) {
        this.fechaModificacion = fechaModificacion;
        return this;
    }

    public ProjectTestDataBuilder withUsuarioModificacion(String usuarioModificacion) {
        this.usuarioModificacion = usuarioModificacion;
        return this;
    }



    public Project build() {
        Project project = new Project();

        project.setIdGuid(idGuid);
        project.setName(name);
        project.setDescripcion(descripcion);
        project.setOwner(owner);

        project.setStatus(status);
        project.setFechaCreacion(fechaCreacion);
        project.setFechaModificacion(fechaModificacion);
        project.setUsuarioModificacion(usuarioModificacion);

        return project;
    }
}
