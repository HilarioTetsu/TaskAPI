package com.springboot.app.testdata;

import java.time.Instant;
import java.util.UUID;

import com.github.javafaker.Faker;
import com.springboot.app.models.entities.Media;
import com.springboot.app.utils.Constants;

public class MediaTestDataBuilder {

    private static final Faker faker = new Faker();

    private Long id = faker.number().randomNumber();
    
    private Long ownerId = faker.number().randomNumber();
    
    private String storageKey = UUID.randomUUID().toString() + ".png";
    
    private String originalName = faker.file().fileName();
    
    private String mimeType = "image/png";
    
    private Long sizeBytes = faker.number().numberBetween(1000L, 5000000L);
    
    private String checksumSha256 = null;
    
   
    private Short status = Constants.STATUS_ACTIVE;
    
    private Instant createdAt = Instant.now();

    public MediaTestDataBuilder withId(Long id) {
        this.id = id;
        return this;
    }

    public MediaTestDataBuilder withOwnerId(Long ownerId) {
        this.ownerId = ownerId;
        return this;
    }

    public MediaTestDataBuilder withStorageKey(String storageKey) {
        this.storageKey = storageKey;
        return this;
    }

    public MediaTestDataBuilder withOriginalName(String originalName) {
        this.originalName = originalName;
        return this;
    }

    public MediaTestDataBuilder withMimeType(String mimeType) {
        this.mimeType = mimeType;
        return this;
    }

    public MediaTestDataBuilder withSizeBytes(Long sizeBytes) {
        this.sizeBytes = sizeBytes;
        return this;
    }

    public MediaTestDataBuilder withChecksumSha256(String checksumSha256) {
        this.checksumSha256 = checksumSha256;
        return this;
    }

    public MediaTestDataBuilder withStatus(Short status) {
        this.status = status;
        return this;
    }

    public MediaTestDataBuilder withCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public Media build() {
        Media media = new Media();
        
        media.setId(id);
        media.setOwnerId(ownerId);
        media.setStorageKey(storageKey);
        media.setOriginalName(originalName);
        media.setMimeType(mimeType);
        media.setSizeBytes(sizeBytes);
        media.setChecksumSha256(checksumSha256);
        media.setStatus(status);
        media.setCreatedAt(createdAt);
        
        return media;
    }
}