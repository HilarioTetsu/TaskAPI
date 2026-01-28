package com.springboot.app.testdata;

import java.util.Collections;
import java.util.List;

import com.github.javafaker.Faker;
import com.springboot.app.models.dtos.CommentUpdateDto;

public class CommentUpdateDtoTestDataBuilder {

    private static final Faker faker = new Faker();

    private String body = faker.lorem().sentence();
    
    private List<Long> mentionsUserIds = Collections.emptyList();

    public CommentUpdateDtoTestDataBuilder withBody(String body) {
        this.body = body;
        return this;
    }

    public CommentUpdateDtoTestDataBuilder withMentionsUserIds(List<Long> mentionsUserIds) {
        this.mentionsUserIds = mentionsUserIds;
        return this;
    }

    public CommentUpdateDto build() {
        CommentUpdateDto dto = new CommentUpdateDto();
        dto.setBody(body);
        dto.setMentionsUserIds(mentionsUserIds);
        return dto;
    }
}