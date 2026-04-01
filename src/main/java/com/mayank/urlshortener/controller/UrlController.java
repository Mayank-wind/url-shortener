package com.mayank.urlshortener.controller;

import com.mayank.urlshortener.dto.*;
import com.mayank.urlshortener.service.RateLimitService;
import com.mayank.urlshortener.service.UrlService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api")
public class UrlController {

    @Autowired
    private UrlService service;

    @Autowired
    private RateLimitService rateLimitService;

    @PostMapping("/shorten")
    public ShortenUrlResponse shorten(@RequestBody ShortenUrlRequest request, HttpServletRequest httpRequest) {
        String clientIp = httpRequest.getRemoteAddr();
        rateLimitService.validateRequest(clientIp);
        return service.shortenUrl(request);
    }


    @GetMapping("/{shortUrl}")
    public ResponseEntity<Void> redirect(@PathVariable String shortUrl) {
        String originalUrl = service.getOriginalUrl(shortUrl);

        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(originalUrl))
                .build();
    }
    @GetMapping("/stats/{shortUrl}")
    public UrlStatsResponse getStats(@PathVariable String shortUrl) {
        return service.getStats(shortUrl);
    }
    @GetMapping("/urls")
    public List<UrlListResponse> getAllUrls() {
        return service.getAllUrls();
    }
    @DeleteMapping("/{shortUrl}")
    public ResponseEntity<Void> deleteUrl(@PathVariable String shortUrl) {
        service.deleteUrl(shortUrl);
        return ResponseEntity.noContent().build();
    }
    @PutMapping("/{shortUrl}/expiration")
    public ResponseEntity<Void> updateExpiration(
            @PathVariable String shortUrl,
            @RequestBody UpdateExpirationRequest request) {
        service.updateExpiration(shortUrl, request);
        return ResponseEntity.noContent().build();
    }

}