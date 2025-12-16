package com.runfit.common.s3.service;

import com.runfit.common.s3.S3Uploader;
import com.runfit.common.s3.dto.response.ImageUploadResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ImageUploadService {

    private final S3Uploader s3Uploader;

    public ImageUploadResponse upload(String fileName) {
        return s3Uploader.getUploadInfo(fileName);
    }

}
