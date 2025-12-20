package com.springboot.app.testdata;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import com.github.javafaker.Faker;
import com.springboot.app.models.entities.Tag;
import com.springboot.app.utils.Constants;

public class TagTestDataBuilder {

    private static final Faker faker = new Faker();


    private Integer id = faker.number().numberBetween(1, Integer.MAX_VALUE);


    private String name = faker.lorem().word();
    private String color = String.format("#%06X", faker.number().numberBetween(0, 0xFFFFFF));


    private List<?> listTareaTags = Collections.emptyList();

 
    private LocalDateTime fechaCreacion = LocalDateTime.now();
    private String usuarioCreacion = faker.name().username();
    private LocalDateTime fechaModificacion = null;
    private String usuarioModificacion = null;
    private Short status = Constants.STATUS_ACTIVE;


    public TagTestDataBuilder withId(Integer id) {
        this.id = id;
        return this;
    }

    public TagTestDataBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public TagTestDataBuilder withColor(String color) {
        this.color = color;
        return this;
    }

    public TagTestDataBuilder withUsuarioCreacion(String usuarioCreacion) {
        this.usuarioCreacion = usuarioCreacion;
        return this;
    }

    public TagTestDataBuilder withStatus(Short status) {
        this.status = status;
        return this;
    }

    public TagTestDataBuilder withFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
        return this;
    }

    public TagTestDataBuilder withFechaModificacion(LocalDateTime fechaModificacion) {
        this.fechaModificacion = fechaModificacion;
        return this;
    }

    public TagTestDataBuilder withUsuarioModificacion(String usuarioModificacion) {
        this.usuarioModificacion = usuarioModificacion;
        return this;
    }


    public Tag build() {
        Tag tag = new Tag();

        tag.setId(id);
        tag.setName(name);
        tag.setColor(color);
        tag.setFechaCreacion(fechaCreacion);
        tag.setUsuarioCreacion(usuarioCreacion);
        tag.setFechaModificacion(fechaModificacion);
        tag.setUsuarioModificacion(usuarioModificacion);
        tag.setStatus(status);

        return tag;
    }
}
