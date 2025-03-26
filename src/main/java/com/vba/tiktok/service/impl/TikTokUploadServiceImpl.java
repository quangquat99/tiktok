package com.vba.tiktok.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vba.tiktok.service.TikTokUploadService;
import org.json.JSONArray;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

@Service
public class TikTokUploadServiceImpl implements TikTokUploadService {

    @Override
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
            long totalSize = file.getSize();
            byte[] fileBytes = file.getBytes();

            HttpRequest uploadRequest = HttpRequest.newBuilder()
                    .uri(URI.create(uploadUrl))
                    .header("Content-Type", "video/mp4")
//                    .header("Content-Length", String.valueOf(totalSize))
                    .header("Content-Range", "bytes 0-" + (totalSize - 1) + "/" + totalSize)
                    .PUT(HttpRequest.BodyPublishers.ofByteArray(fileBytes))
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


            // Step 4: Fetch status
            Map<String, Object> fetchStatusBody = new HashMap<>();
            fetchStatusBody.put("publish_id", publishId);
            String fetchStatusRequestBody = mapper.writeValueAsString(fetchStatusBody);

            HttpRequest fetchStatusRequest = HttpRequest.newBuilder()
                    .uri(URI.create("https://open.tiktokapis.com/v2/post/publish/status/fetch/"))
                    .header("Authorization", "Bearer " + accessToken)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(fetchStatusRequestBody))
                    .build();

            HttpResponse<String> fetchResponse = client.send(fetchStatusRequest, HttpResponse.BodyHandlers.ofString());

            return fetchResponse.body();

        } catch (Exception e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }

    @Override
    public String queryCreatorInfo(String accessToken) {
        try {

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://open.tiktokapis.com/v2/post/publish/creator_info/query/"))
                    .header("Authorization", "Bearer " + accessToken)
                    .header("Content-Type", "application/json; charset=UTF-8")
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            return response.body();

        } catch (Exception e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }

    @Override
    public String uploadMultipleImages(MultipartFile[] files, String accessToken) {
        try {
            // 1. Gọi content/init để lấy post_id và upload URLs
            JSONObject requestBody = new JSONObject();

            JSONObject postInfo = new JSONObject();
            postInfo.put("title", "Test upload from backend");
            postInfo.put("privacy_level", "SELF_ONLY"); // Chỉ mình tôi, để test
            postInfo.put("disable_duet", false);
            postInfo.put("disable_stitch", false);
            postInfo.put("disable_comment", false);

            JSONArray photoImages = new JSONArray();
//            photoImages.put("https://cdn.vbatechs.com/prod/wp-content/uploads/2023/09/VBA_The-Open-Group-Certified-Togaf-Foundation_badge.png");
            photoImages.put("https://cdn.vbatechs.com/prod/wp-content/uploads/2023/10/quang.jpeg");

            JSONObject sourceInfo = new JSONObject();
            sourceInfo.put("source", "PULL_FROM_URL");
            sourceInfo.put("photo_cover_index", 0);
            sourceInfo.put("photo_images", photoImages);

            requestBody.put("post_info", postInfo);
            requestBody.put("source_info", sourceInfo);
            requestBody.put("media_type", "PHOTO");
            requestBody.put("post_mode", "DIRECT_POST");

            HttpClient client = HttpClient.newHttpClient();

            HttpRequest initRequest = HttpRequest.newBuilder()
                    .uri(URI.create("https://open.tiktokapis.com/v2/post/publish/content/init/"))
                    .header("Authorization", "Bearer " + accessToken)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString(), StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> initResponse = client.send(initRequest, HttpResponse.BodyHandlers.ofString());

            JSONObject initResponseJson = new JSONObject(initResponse.body());

            // Step : Fetch status
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> fetchStatusBody = new HashMap<>();
            String publishId = initResponseJson.getJSONObject("data").getString("publish_id");

            fetchStatusBody.put("publish_id", publishId);
            String fetchStatusRequestBody = mapper.writeValueAsString(fetchStatusBody);

            HttpRequest fetchStatusRequest = HttpRequest.newBuilder()
                    .uri(URI.create("https://open.tiktokapis.com/v2/post/publish/status/fetch/"))
                    .header("Authorization", "Bearer " + accessToken)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(fetchStatusRequestBody))
                    .build();

            HttpResponse<String> fetchResponse = client.send(fetchStatusRequest, HttpResponse.BodyHandlers.ofString());

            return fetchResponse.body();

//            return null;


        } catch (Exception e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }
}