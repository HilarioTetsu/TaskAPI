package com.springboot.app;

import java.net.URI;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
public class S3Config {

  @Bean
  S3Client s3Client(
      @Value("${app.s3.endpoint}") String endpoint,
      @Value("${app.s3.region}") String region,
      @Value("${app.s3.accessKey}") String accessKey,
      @Value("${app.s3.secretKey}") String secretKey
  ) {
    return S3Client.builder()
        .endpointOverride(URI.create(endpoint))
        .region(Region.of(region))
        .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
        .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build()) 
        .httpClientBuilder(UrlConnectionHttpClient.builder())
        .overrideConfiguration(ClientOverrideConfiguration.builder()
            .apiCallTimeout(Duration.ofSeconds(30)).build())
        .build();
  }

  @Bean
  S3Presigner s3Presigner(
      @Value("${app.s3.endpoint}") String endpoint,
      @Value("${app.s3.region}") String region,
      @Value("${app.s3.accessKey}") String accessKey,
      @Value("${app.s3.secretKey}") String secretKey
  ) {
    return S3Presigner.builder()
        .endpointOverride(URI.create(endpoint))
        .region(Region.of(region))
        .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
        .serviceConfiguration(
                S3Configuration.builder()
                    .pathStyleAccessEnabled(true)  
                    .build()
            )
        .build();
  }
}
