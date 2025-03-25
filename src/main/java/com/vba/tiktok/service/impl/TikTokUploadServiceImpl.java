package com.vba.tiktok.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vba.tiktok.service.TikTokUploadService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

@Service
public class TikTokUploadServiceImpl implements TikTokUploadService {

    public String uploadVideo(MultipartFile file, String accessToken) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            ObjectMapper mapper = new ObjectMapper();

            // Step 1: Initialize video publish
            Map<String, Object> postInfo = new HashMap<>();
            postInfo.put("title", "This will be a funny #cat video on your @tiktok #fyp");
            postInfo.put("privacy_level", "SELF_ONLY");
            postInfo.put("disable_duet", false);
            postInfo.put("disable_comment", true);
            postInfo.put("disable_stitch", false);
            postInfo.put("video_cover_timestamp_ms", 1000);

            Map<String, Object> sourceInfo = new HashMap<>();
            sourceInfo.put("source", "FILE_UPLOAD");
            sourceInfo.put("video_size", file.getSize());
            sourceInfo.put("chunk_size", file.getSize());  // If sending in one go, chunk_size = video_size
            sourceInfo.put("total_chunk_count", 1);

            Map<String, Object> initBody = new HashMap<>();
            initBody.put("post_info", postInfo);
            initBody.put("source_info", sourceInfo);

            String initRequestBody = mapper.writeValueAsString(initBody);

            HttpRequest initRequest = HttpRequest.newBuilder()
                    .uri(URI.create("https://open.tiktokapis.com/v2/post/publish/video/init/"))
                    .header("Authorization", "Bearer " + accessToken)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(initRequestBody))
                    .build();

            HttpResponse<String> initResponse = client.send(initRequest, HttpResponse.BodyHandlers.ofString());
            Map<String, Object> initResponseMap = mapper.readValue(initResponse.body(), Map.class);

            Map<String, Object> data = (Map<String, Object>) initResponseMap.get("data");
            String publishId = (String) data.get("publish_id");
            String uploadUrl = (String) data.get("upload_url");

            // Step 2: Upload video file
            HttpRequest uploadRequest = HttpRequest.newBuilder()
                    .uri(URI.create(uploadUrl))
                    .header("Content-Type", "video/mp4")
                    .PUT(HttpRequest.BodyPublishers.ofByteArray(file.getBytes()))
                    .build();

            client.send(uploadRequest, HttpResponse.BodyHandlers.ofString());

            // Step 3: Complete video publish
            Map<String, Object> completeBody = new HashMap<>();
            completeBody.put("publish_id", publishId);
            String completeRequestBody = mapper.writeValueAsString(completeBody);

            HttpRequest completeRequest = HttpRequest.newBuilder()
                    .uri(URI.create("https://open.tiktokapis.com/v2/post/publish/complete/"))
                    .header("Authorization", "Bearer " + accessToken)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(completeRequestBody))
                    .build();

            HttpResponse<String> completeResponse = client.send(completeRequest, HttpResponse.BodyHandlers.ofString());

            return completeResponse.body();

        } catch (Exception e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }
}
