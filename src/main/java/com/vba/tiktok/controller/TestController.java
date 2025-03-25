package com.vba.tiktok.controller;

import com.vba.tiktok.service.TikTokUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/")
public class TestController {

    @Autowired
    TikTokUploadService tikTokUploadService;

    private final String CLIENT_KEY = "sbawh5jwr50tttl4l9";
    private final String CLIENT_SECRET = "CbFAl4j7LMgJvtrLxuvIOqfr99t9bNUC";
    private final String REDIRECT_URI = "https://dzew7cvh3rahv.cloudfront.net/callback";

    @GetMapping
    public String test() {
        return "Tiktok Test Application";
    }

    @GetMapping("callback")
    public String callback() {
        return "Tiktok Logged In Successfully";
    }

    @PostMapping("tiktok/exchange-token")
    public ResponseEntity<?> exchangeToken(@RequestBody Map<String, String> request) {
        String code = request.get("code");
        String state = request.get("state");

        RestTemplate restTemplate = new RestTemplate();

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("client_key", CLIENT_KEY);
        params.add("client_secret", CLIENT_SECRET);
        params.add("code", code);
        params.add("grant_type", "authorization_code");
        params.add("redirect_uri", REDIRECT_URI);

        ResponseEntity<String> response = restTemplate.postForEntity(
                "https://open.tiktokapis.com/v2/oauth/token/",
                new HttpEntity<>(params, new HttpHeaders()),
                String.class
        );

        return ResponseEntity.ok(response.getBody());
    }

    @PostMapping("tiktok/upload")
    public ResponseEntity<String> uploadVideo(@RequestParam("file") MultipartFile file, @RequestParam("accessToken") String accessToken) {
        return ResponseEntity.ok(tikTokUploadService.uploadVideo(file, accessToken));
    }
}
