package com.mayank.urlshortener.controller;

import com.mayank.urlshortener.model.UrlMapping;
import com.mayank.urlshortener.service.UrlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api")
public class UrlController {

    @Autowired
    private UrlService service;

    @PostMapping("/shorten")
    public String shorten(@RequestParam String url) {
        return service.shortenUrl(url);
    }

    @GetMapping("/{shortUrl}")
    public ResponseEntity<Void> redirect(@PathVariable String shortUrl) {
        String originalUrl = service.getOriginalUrl(shortUrl);

        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(originalUrl))
                .build();
    }
    @GetMapping("/stats/{shortUrl}")
    public UrlMapping getStats(@PathVariable String shortUrl) {
        return service.getStats(shortUrl);
    }
}