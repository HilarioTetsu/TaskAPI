package com.springboot.app.testdata;

import java.time.LocalDateTime;
import java.util.UUID;

import com.github.javafaker.Faker;
import com.springboot.app.models.entities.Invitation;
import com.springboot.app.models.entities.Project;
import com.springboot.app.models.entities.Usuario;
import com.springboot.app.utils.Constants;
import com.springboot.app.utils.ProjectRole;

public class InvitationTestDataBuilder {

    private static final Faker faker = new Faker();

    // UUID aleatorio (Length 36)
    private String id = UUID.randomUUID().toString();

    private Usuario host = new UsuarioTestDataBuilder().build();
    
    private Usuario guest = new UsuarioTestDataBuilder().build();
    
    private Project project = new ProjectTestDataBuilder().build();
    
    private ProjectRole role = ProjectRole.VIEWER;
    
    // Por defecto las invitaciones suelen nacer en PENDING
    private Short status = Constants.STATUS_PENDING;
    
    private LocalDateTime fechaCreacion = LocalDateTime.now();
    
    private LocalDateTime fechaModificacion = null;

    public InvitationTestDataBuilder withId(String id) {
        this.id = id;
        return this;
    }

    public InvitationTestDataBuilder withHost(Usuario host) {
        this.host = host;
        return this;
    }

    public InvitationTestDataBuilder withGuest(Usuario guest) {
        this.guest = guest;
        return this;
    }

    public InvitationTestDataBuilder withProject(Project project) {
        this.project = project;
        return this;
    }

    public InvitationTestDataBuilder withRole(ProjectRole role) {
        this.role = role;
        return this;
    }

    public InvitationTestDataBuilder withStatus(Short status) {
        this.status = status;
        return this;
    }

    public InvitationTestDataBuilder withFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
        return this;
    }

    public InvitationTestDataBuilder withFechaModificacion(LocalDateTime fechaModificacion) {
        this.fechaModificacion = fechaModificacion;
        return this;
    }

    public Invitation build() {
        Invitation invitation = new Invitation();
        
        invitation.setId(id);
        invitation.setHost(host);
        invitation.setGuest(guest);
        invitation.setProject(project);
        invitation.setRole(role);
        invitation.setStatus(status);
        invitation.setFechaCreacion(fechaCreacion);
        invitation.setFechaModificacion(fechaModificacion);
        
        return invitation;
    }
}