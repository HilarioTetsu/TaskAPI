package com.springboot.app.testdata;

import java.time.LocalDateTime;
import java.util.UUID;

import com.github.javafaker.Faker;
import com.springboot.app.models.dtos.InvitationDto;
import com.springboot.app.utils.Constants;
import com.springboot.app.utils.ProjectRole;

public class InvitationDtoTestDataBuilder {

    private static final Faker faker = new Faker();

    private String id = UUID.randomUUID().toString();
    
    private Long userHostId = faker.number().randomNumber();
    
    private Long userGuestId = faker.number().randomNumber();
    
    private String projectId = UUID.randomUUID().toString();
    
    private ProjectRole role = ProjectRole.VIEWER;
    
    private Short status = Constants.STATUS_PENDING;
    
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    public InvitationDtoTestDataBuilder withId(String id) {
        this.id = id;
        return this;
    }

    public InvitationDtoTestDataBuilder withUserHostId(Long userHostId) {
        this.userHostId = userHostId;
        return this;
    }

    public InvitationDtoTestDataBuilder withUserGuestId(Long userGuestId) {
        this.userGuestId = userGuestId;
        return this;
    }

    public InvitationDtoTestDataBuilder withProjectId(String projectId) {
        this.projectId = projectId;
        return this;
    }

    public InvitationDtoTestDataBuilder withRole(ProjectRole role) {
        this.role = role;
        return this;
    }

    public InvitationDtoTestDataBuilder withStatus(Short status) {
        this.status = status;
        return this;
    }

    public InvitationDtoTestDataBuilder withFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
        return this;
    }

    public InvitationDto build() {
        InvitationDto dto = new InvitationDto();
        dto.setId(id);
        dto.setUserHostId(userHostId);
        dto.setUserGuestId(userGuestId);
        dto.setProjectId(projectId);
        dto.setRole(role);
        dto.setStatus(status);
        dto.setFechaCreacion(fechaCreacion);
        return dto;
    }
}