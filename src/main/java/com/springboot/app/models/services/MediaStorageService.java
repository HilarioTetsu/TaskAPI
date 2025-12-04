package com.springboot.app.models.services;

import java.net.URL;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Service
public class MediaStorageService {

	private final S3Client s3;
	private final S3Presigner presigner;

	@Value("${app.s3.bucket}")
	private String bucket;
	@Value("${app.s3.presignMinutes}")
	private int presignMinutes;
	@Value("${app.s3.maxSizeBytes}")
	private long maxSizeBytes;

	public MediaStorageService(S3Client s3, S3Presigner presigner) {
		super();
		this.s3 = s3;
		this.presigner = presigner;
	}

	public String buildStorageKey(Long ownerId, String ext) {
		LocalDate today = LocalDate.now();
		return "users/%d/%04d/%02d/%02d/%s.%s".formatted(ownerId, today.getYear(), today.getMonthValue(),
				today.getDayOfMonth(), UUID.randomUUID(), ext.toLowerCase());
	}

	public URL createPresignedPutUrl(String storageKey, String contentType) {
		var put = PutObjectRequest.builder().bucket(bucket).key(storageKey).contentType(contentType).build();

		var presign = PutObjectPresignRequest.builder().signatureDuration(Duration.ofMinutes(presignMinutes))
				.putObjectRequest(put).build();

		PresignedPutObjectRequest req = presigner.presignPutObject(presign);
		return req.url();
	}

	public URL createPresignedGetUrl(String storageKey, int minutes) {
		var get = GetObjectRequest.builder().bucket(bucket).key(storageKey).build();

		var presign = GetObjectPresignRequest.builder().signatureDuration(Duration.ofMinutes(minutes))
				.getObjectRequest(get).build();

		PresignedGetObjectRequest req = presigner.presignGetObject(presign);
		return req.url();
	}

	public List<String> createPresignedGetUrls(List<String> storageKey) {

		PresignedGetObjectRequest req;
		
		List<String> urls = new ArrayList<>();

		for (String key : storageKey) {

			var get = GetObjectRequest.builder().bucket(bucket).key(key).build();
		
			var presign = GetObjectPresignRequest.builder().signatureDuration(Duration.ofMinutes(presignMinutes))
					.getObjectRequest(get).build();
			
			req = presigner.presignGetObject(presign);
			
			urls.add(req.url().toString());
			
		}

		
		return urls;
	}

	public HeadObjectResponse headObject(String storageKey) {
		return s3.headObject(HeadObjectRequest.builder().bucket(bucket).key(storageKey).build());
	}

	public void backendUpload(String storageKey, byte[] bytes, String contentType) {
		s3.putObject(PutObjectRequest.builder().bucket(bucket).key(storageKey).contentType(contentType).build(),
				RequestBody.fromBytes(bytes));
	}

	public void delete(String storageKey) {
		s3.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(storageKey).build());
	}

	public void deleteAll(List<String> storageKeys) {

		storageKeys.stream().forEach(item -> {
			s3.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(item).build());
		});

	}

	public boolean verifySizeFiles(List<String> confirmMediaStorageKeyId) {

		for (String key : confirmMediaStorageKeyId) {

			var head = headObject(key);

			if (head.contentLength() > maxSizeBytes) {

				return false;

			}

		}

		return true;
	}

}
