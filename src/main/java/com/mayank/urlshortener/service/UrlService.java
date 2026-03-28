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

        if (!originalUrl.startsWith("http://") && !originalUrl.startsWith("https://")) {
            throw new RuntimeException("Invalid URL. Must start with http:// or https://");
        }

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
        UrlMapping mapping = repo.findByShortUrl(shortUrl)
                .orElseThrow(() -> new RuntimeException("URL not found"));

        // Increment click count
        mapping.setClickCount(mapping.getClickCount() + 1);
        repo.save(mapping);

        return mapping.getOriginalUrl();
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

    public UrlMapping getStats(String shortUrl) {
        return repo.findByShortUrl(shortUrl)
                .orElseThrow(() -> new RuntimeException("URL not found"));
    }
}