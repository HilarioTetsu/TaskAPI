package com.springboot.app.testdata;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import com.github.javafaker.Faker;
import com.springboot.app.models.entities.Comment;
import com.springboot.app.models.entities.Media;
import com.springboot.app.models.entities.Tarea;
import com.springboot.app.models.entities.Usuario;
import com.springboot.app.utils.Constants;

public class CommentTestDataBuilder {

    private static final Faker faker = new Faker();

    private Long id = faker.number().randomNumber();
    
    private String body = faker.lorem().paragraph();
    
    // Por defecto creamos una tarea y un autor válidos para evitar NullPointer
    private Tarea tarea = new TareaTestDataBuilder().build();
    
    private Usuario autor = new UsuarioTestDataBuilder().build();
    
    private List<Usuario> mentions = null;
    
    private List<Media> adjuntos = null;
    
    private LocalDateTime fechaCreacion = LocalDateTime.now();
    
    private LocalDateTime fechaModificacion = null;
    
    private Short status = Constants.STATUS_ACTIVE;

    public CommentTestDataBuilder withId(Long id) {
        this.id = id;
        return this;
    }

    public CommentTestDataBuilder withBody(String body) {
        this.body = body;
        return this;
    }

    public CommentTestDataBuilder withTarea(Tarea tarea) {
        this.tarea = tarea;
        return this;
    }

    public CommentTestDataBuilder withAutor(Usuario autor) {
        this.autor = autor;
        return this;
    }

    public CommentTestDataBuilder withMentions(List<Usuario> mentions) {
        this.mentions = mentions;
        return this;
    }

    public CommentTestDataBuilder withAdjuntos(List<Media> adjuntos) {
        this.adjuntos = adjuntos;
        return this;
    }

    public CommentTestDataBuilder withFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
        return this;
    }

    public CommentTestDataBuilder withFechaModificacion(LocalDateTime fechaModificacion) {
        this.fechaModificacion = fechaModificacion;
        return this;
    }

    public CommentTestDataBuilder withStatus(Short status) {
        this.status = status;
        return this;
    }

    public Comment build() {
        Comment comment = new Comment();
        
        comment.setId(id);
        comment.setBody(body);
        comment.setTarea(tarea);
        comment.setAutor(autor);
        comment.setMentions(mentions);
        comment.setAdjuntos(adjuntos);
        comment.setFechaCreacion(fechaCreacion);
        comment.setFechaModificacion(fechaModificacion);
        comment.setStatus(status);
        
        return comment;
    }
}