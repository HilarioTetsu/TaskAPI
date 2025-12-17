package com.springboot.app.testdata;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;

import com.github.javafaker.Faker;
import com.springboot.app.models.entities.PrioridadTarea;
import com.springboot.app.models.entities.Project;
import com.springboot.app.models.entities.Tarea;
import com.springboot.app.models.entities.TareaStatus;
import com.springboot.app.models.entities.Usuario;
import com.springboot.app.utils.Constants;

public class TareaTestDataBuilder {

    private static final Faker faker = new Faker();

	private String tareaId=UUID.randomUUID().toString();
	
    private String titulo = faker.book().title();
    
    private String descripcion = faker.lorem().sentence();
    
    private Usuario owner = new UsuarioTestDataBuilder()
    						.withId(faker.number().numberBetween(Long.MIN_VALUE, Long.MAX_VALUE)).build();
    
    private TareaStatus tareaStatus= new TareaStatusTestDataBuilder().build();
    
    private PrioridadTarea prioridadTarea = new PrioridadTareaTestDataBuilder().build();
    
    private Project project = new ProjectTestDataBuilder().build();
    
    Date futureDate = faker.date().future(30, java.util.concurrent.TimeUnit.DAYS);
    
    LocalDateTime fechaLimite = futureDate.toInstant() .atZone(ZoneId.systemDefault()) .toLocalDateTime();
    
    public TareaTestDataBuilder withId(String id) {
        this.tareaId = id;
        return this;
    }
    
    public TareaTestDataBuilder withOwner(Usuario user) {
        this.owner = user;
        return this;
    }
    
    public TareaTestDataBuilder withProject(Project project) {
        this.project = project;
        return this;
    }
    
    
    public Tarea build() {
        Tarea tarea = new Tarea();
        
        tarea.setIdGuid(tareaId);
        tarea.setTitulo(titulo);
        tarea.setDescripcion(descripcion);
        tarea.setOwner(owner);
        tarea.setPrioridad(prioridadTarea);
        tarea.setTareaStatus(tareaStatus);
        tarea.setProject(project);
        tarea.setStatus(Constants.STATUS_ACTIVE);
        tarea.setFechaLimite(fechaLimite);
        tarea.setFechaCreacion(LocalDateTime.now());
        
        
        return tarea;
    }
    
}
