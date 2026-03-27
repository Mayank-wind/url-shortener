package com.mayank.urlshortener.service;

import com.mayank.urlshortener.model.UrlMapping;
import com.mayank.urlshortener.repository.UrlRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class UrlService {

    @Autowired
    private UrlRepository repo;

    public String shortenUrl(String originalUrl) {
        String shortUrl = generateShortUrl();

        UrlMapping mapping = new UrlMapping();
        mapping.setOriginalUrl(originalUrl);
        mapping.setShortUrl(shortUrl);
        mapping.setCreatedAt(LocalDateTime.now());

        repo.save(mapping);

        return shortUrl;
    }

    public String getOriginalUrl(String shortUrl) {
        return repo.findByShortUrl(shortUrl)
                .map(UrlMapping::getOriginalUrl)
                .orElseThrow(() -> new RuntimeException("URL not found"));
    }

    private String generateShortUrl() {
        return UUID.randomUUID().toString().substring(0, 6);
    }
}