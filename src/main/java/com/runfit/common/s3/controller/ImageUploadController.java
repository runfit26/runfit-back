package com.runfit.common.s3.controller;

import com.runfit.common.response.ResponseWrapper;
import com.runfit.common.s3.dto.request.ImageUploadRequest;
import com.runfit.common.s3.dto.response.ImageUploadResponse;
import com.runfit.common.s3.service.ImageUploadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
public class ImageUploadController {

    private final ImageUploadService imageUploadService;

    @PostMapping("/presigned-url")
    public ResponseEntity<ResponseWrapper<ImageUploadResponse>> getPresignedUrl(
        @Valid @RequestBody ImageUploadRequest request
    ) {
        ImageUploadResponse response = imageUploadService.upload(request.imageName());
        return ResponseEntity.ok(ResponseWrapper.success(response));
    }
}
