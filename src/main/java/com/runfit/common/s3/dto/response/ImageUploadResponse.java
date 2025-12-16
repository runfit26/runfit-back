package com.runfit.common.s3.dto.response;

public record ImageUploadResponse(
    String presignedUrl,
    String imageUrl
) {

}
