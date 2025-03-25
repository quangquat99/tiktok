package com.vba.tiktok.service;

import org.springframework.web.multipart.MultipartFile;

public interface TikTokUploadService {

    String uploadVideo(MultipartFile file, String accessToken);
}
