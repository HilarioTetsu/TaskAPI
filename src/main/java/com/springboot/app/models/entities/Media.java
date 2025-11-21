package com.springboot.app.models.entities;



import jakarta.persistence.*;
import lombok.Data;
import java.time.Instant;

import com.springboot.app.utils.Constants;

@Data
@Entity
@Table(name = "media")
public class Media {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    @Column(name = "storage_key", nullable = false, length = 512, unique = true)
    private String storageKey;

    @Column(name = "original_name")
    private String originalName;

    @Column(name = "mime_type")
    private String mimeType;

    @Column(name = "size_bytes",nullable = true)
    private Long sizeBytes;

    @Column(name = "checksum_sha256",nullable = true)
    private String checksumSha256;


    @Column(name = "status", nullable = false)
    private Short status; 

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    
    
    
	@PrePersist
	public void prePersist() {
		this.createdAt = Instant.now();
		this.status=Constants.STATUS_PENDING;
	}
}
