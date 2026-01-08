package com.springboot.app.testdata;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import com.github.javafaker.Faker;
import com.springboot.app.models.dtos.CommentDto;
import com.springboot.app.utils.Constants;

public class CommentDtoTestDataBuilder {

    private static final Faker faker = new Faker();

    private Long id = faker.number().randomNumber();
    
    private String tareaId = UUID.randomUUID().toString();
    
    private Long ownerUserId = faker.number().randomNumber();
    
    private String body = faker.lorem().paragraph();
    
    private List<Long> mentionsUserIds = Collections.emptyList();
    
    private List<String> confirmMediaStorageKeyId = Collections.emptyList();
    
    private List<String> confirmMediaStorageKeyUrls = Collections.emptyList();
    
    private Short status = Constants.STATUS_ACTIVE;
    
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    public CommentDtoTestDataBuilder withId(Long id) {
        this.id = id;
        return this;
    }

    public CommentDtoTestDataBuilder withTareaId(String tareaId) {
        this.tareaId = tareaId;
        return this;
    }

    public CommentDtoTestDataBuilder withOwnerUserId(Long ownerUserId) {
        this.ownerUserId = ownerUserId;
        return this;
    }

    public CommentDtoTestDataBuilder withBody(String body) {
        this.body = body;
        return this;
    }

    public CommentDtoTestDataBuilder withMentionsUserIds(List<Long> mentionsUserIds) {
        this.mentionsUserIds = mentionsUserIds;
        return this;
    }

    public CommentDtoTestDataBuilder withConfirmMediaStorageKeyId(List<String> confirmMediaStorageKeyId) {
        this.confirmMediaStorageKeyId = confirmMediaStorageKeyId;
        return this;
    }

    public CommentDtoTestDataBuilder withConfirmMediaStorageKeyUrls(List<String> confirmMediaStorageKeyUrls) {
        this.confirmMediaStorageKeyUrls = confirmMediaStorageKeyUrls;
        return this;
    }

    public CommentDtoTestDataBuilder withStatus(Short status) {
        this.status = status;
        return this;
    }

    public CommentDtoTestDataBuilder withFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
        return this;
    }

    public CommentDto build() {
        CommentDto dto = new CommentDto();
        
        dto.setId(id);
        dto.setTareaId(tareaId);
        dto.setOwnerUserId(ownerUserId);
        dto.setBody(body);
        dto.setMentionsUserIds(mentionsUserIds);
        dto.setConfirmMediasStorageKeyId(confirmMediaStorageKeyId);       
        dto.setStatus(status);
        dto.setFechaCreacion(fechaCreacion);
        
        return dto;
    }
}