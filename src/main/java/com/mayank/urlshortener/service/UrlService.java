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

        UrlMapping mapping = new UrlMapping();
        mapping.setOriginalUrl(originalUrl);
        mapping.setCreatedAt(LocalDateTime.now());

        mapping = repo.save(mapping);

        String shortUrl = generateShortUrl(mapping.getId());
        mapping.setShortUrl(shortUrl);
        repo.save(mapping);

        return shortUrl;
    }

    public String getOriginalUrl(String shortUrl) {
        return repo.findByShortUrl(shortUrl)
                .map(UrlMapping::getOriginalUrl)
                .orElseThrow(() -> new RuntimeException("URL not found"));
    }

    private static final String BASE62 = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    private String generateShortUrl(Long id) {
        StringBuilder shortUrl = new StringBuilder();

        while (id > 0) {
            int remainder = (int) (id % 62);
            shortUrl.append(BASE62.charAt(remainder));
            id /= 62;
        }

        return shortUrl.reverse().toString();
    }
}