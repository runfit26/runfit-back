package com.runfit.common.s3;

import com.runfit.common.s3.dto.response.ImageUploadResponse;
import java.time.Duration;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Component
@RequiredArgsConstructor
public class S3Uploader {

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucket;

    private final S3Presigner s3Presigner;

    public ImageUploadResponse getUploadInfo(String fileName) {
        String key = generateKey(fileName);

        PutObjectRequest objectRequest = PutObjectRequest.builder()
            .bucket(bucket)
            .key(key)
            .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
            .signatureDuration(Duration.ofMinutes(15))
            .putObjectRequest(objectRequest)
            .build();

        String presignedUrl = s3Presigner.presignPutObject(presignRequest).url().toString();
        String imageUrl = String.format("https://%s.s3.amazonaws.com/%s", bucket, key);

        return new ImageUploadResponse(presignedUrl, imageUrl);
    }

    private String generateKey(String fileName) {
        String extension = fileName.substring(fileName.lastIndexOf(".")).toLowerCase();
        String uuid = UUID.randomUUID().toString();
        return uuid + extension;
    }
}
