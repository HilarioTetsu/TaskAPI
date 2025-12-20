package com.springboot.app.testdata;

import java.time.LocalDateTime;

import com.github.javafaker.Faker;
import com.springboot.app.models.entities.Tag;
import com.springboot.app.models.entities.Tarea;
import com.springboot.app.models.entities.TareaTags;
import com.springboot.app.utils.Constants;

public class TareaTagsTestDataBuilder {

    private static final Faker faker = new Faker();


    private Integer id = faker.number().numberBetween(1, Integer.MAX_VALUE);


    private Tarea tarea = new TareaTestDataBuilder().build();
    private Tag tag = new TagTestDataBuilder().build();


    private LocalDateTime fechaCreacion = LocalDateTime.now();
    private String usuarioCreacion = "admin";
    private LocalDateTime fechaModificacion = null;
    private String usuarioModificacion = null;
    private Short status = Constants.STATUS_ACTIVE;


    public TareaTagsTestDataBuilder withId(Integer id) {
        this.id = id;
        return this;
    }

    public TareaTagsTestDataBuilder withTarea(Tarea tarea) {
        this.tarea = tarea;
        return this;
    }

    public TareaTagsTestDataBuilder withTag(Tag tag) {
        this.tag = tag;
        return this;
    }

    public TareaTagsTestDataBuilder withFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
        return this;
    }

    public TareaTagsTestDataBuilder withUsuarioCreacion(String usuarioCreacion) {
        this.usuarioCreacion = usuarioCreacion;
        return this;
    }

    public TareaTagsTestDataBuilder withFechaModificacion(LocalDateTime fechaModificacion) {
        this.fechaModificacion = fechaModificacion;
        return this;
    }

    public TareaTagsTestDataBuilder withUsuarioModificacion(String usuarioModificacion) {
        this.usuarioModificacion = usuarioModificacion;
        return this;
    }

    public TareaTagsTestDataBuilder withStatus(Short status) {
        this.status = status;
        return this;
    }


    public TareaTags build() {
        TareaTags tareaTags = new TareaTags();

        tareaTags.setId(id);
        tareaTags.setTarea(tarea);
        tareaTags.setTag(tag);

        tareaTags.setFechaCreacion(fechaCreacion);
        tareaTags.setUsuarioCreacion(usuarioCreacion);
        tareaTags.setFechaModificacion(fechaModificacion);
        tareaTags.setUsuarioModificacion(usuarioModificacion);
        tareaTags.setStatus(status);

        return tareaTags;
    }
}
