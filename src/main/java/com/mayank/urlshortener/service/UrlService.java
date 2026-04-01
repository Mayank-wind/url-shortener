package com.mayank.urlshortener.service;

import com.mayank.urlshortener.dto.*;
import com.mayank.urlshortener.exception.AliasAlreadyExistsException;
import com.mayank.urlshortener.exception.InvalidUrlException;
import com.mayank.urlshortener.exception.UrlExpiredException;
import com.mayank.urlshortener.exception.UrlNotFoundException;
import com.mayank.urlshortener.model.UrlMapping;
import com.mayank.urlshortener.repository.UrlRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class UrlService {

    @Autowired
    private UrlRepository repo;
    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    public ShortenUrlResponse shortenUrl(ShortenUrlRequest request) {
        String originalUrl = request.getUrl();
        String customAlias = request.getCustomAlias();
        LocalDateTime expiresAt = request.getExpiresAt();


        if (!isValidUrl(originalUrl)) {
            throw new InvalidUrlException("Invalid URL format. Please provide a valid http or https URL.");
        }
        Optional<UrlMapping> existingMapping = repo.findFirstByOriginalUrlOrderByIdAsc(originalUrl);
        if (existingMapping.isPresent()) {
            UrlMapping mapping = existingMapping.get();
            return new ShortenUrlResponse(mapping.getShortUrl(), mapping.getOriginalUrl());
        }
        if (customAlias != null && !customAlias.isBlank()) {
            repo.findByShortUrl(customAlias).ifPresent(existing -> {
                throw new AliasAlreadyExistsException("Custom alias already exists: " + customAlias);
            });

            UrlMapping mapping = new UrlMapping();
            mapping.setOriginalUrl(originalUrl);
            mapping.setShortUrl(customAlias);
            mapping.setCreatedAt(LocalDateTime.now());
            mapping.setExpiresAt(expiresAt);


            mapping = repo.save(mapping);

            return new ShortenUrlResponse(mapping.getShortUrl(), mapping.getOriginalUrl());
        }

        UrlMapping mapping = new UrlMapping();
        mapping.setOriginalUrl(originalUrl);
        mapping.setCreatedAt(LocalDateTime.now());
        mapping.setExpiresAt(expiresAt);


        mapping = repo.save(mapping);

        String shortUrl = generateShortUrl(mapping.getId());
        mapping.setShortUrl(shortUrl);

        repo.save(mapping);

        return new ShortenUrlResponse(shortUrl, originalUrl);
    }

    public String getOriginalUrl(String shortUrl) {

        try {
            String cachedUrl = redisTemplate.opsForValue().get(shortUrl);
            if (cachedUrl != null) {
                return cachedUrl;
            }
        } catch (Exception e) {
            System.out.println("Redis read error: " + e.getMessage());
        }

        UrlMapping mapping = repo.findByShortUrl(shortUrl)
                .orElseThrow(() -> new UrlNotFoundException("URL not found: "+shortUrl));

        if (mapping.getExpiresAt() != null && mapping.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new UrlExpiredException("Short URL has expired: " + shortUrl);
        }

        try {
            redisTemplate.opsForValue().set(shortUrl, mapping.getOriginalUrl());
        } catch (Exception e) {
            System.out.println("Redis write error: " + e.getMessage());
        }

        mapping.setClickCount(mapping.getClickCount() + 1);
        mapping.setLastAccessedAt(LocalDateTime.now());
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

    public UrlStatsResponse getStats(String shortUrl) {
        UrlMapping mapping = repo.findByShortUrl(shortUrl)
                .orElseThrow(() -> new UrlNotFoundException("URL not found: " + shortUrl));

        return new UrlStatsResponse(
                mapping.getShortUrl(),
                mapping.getOriginalUrl(),
                mapping.getClickCount(),
                mapping.getCreatedAt(),
                mapping.getExpiresAt(),
                mapping.getLastAccessedAt()
        );
    }


    private boolean isValidUrl(String url) {
        try {
            URI uri = new URI(url);
            return uri.getScheme() != null
                    && (uri.getScheme().equals("http") || uri.getScheme().equals("https"))
                    && uri.getHost() != null;
        } catch (URISyntaxException e) {
            return false;
        }
    }
    public List<UrlListResponse> getAllUrls() {
        return repo.findAll().stream()
                .map(mapping -> new UrlListResponse(
                        mapping.getShortUrl(),
                        mapping.getOriginalUrl(),
                        mapping.getClickCount(),
                        mapping.getCreatedAt(),
                        mapping.getExpiresAt(),
                        mapping.getLastAccessedAt()
                ))
                .toList();
    }
    public void deleteUrl(String shortUrl) {
        UrlMapping mapping = repo.findByShortUrl(shortUrl)
                .orElseThrow(() -> new UrlNotFoundException("URL not found: " + shortUrl));

        repo.delete(mapping);

        try {
            redisTemplate.delete(shortUrl);
        } catch (Exception e) {
            System.out.println("Redis delete error: " + e.getMessage());
        }
    }
    public void updateExpiration(String shortUrl, UpdateExpirationRequest request) {
        UrlMapping mapping = repo.findByShortUrl(shortUrl)
                .orElseThrow(() -> new UrlNotFoundException("URL not found: " + shortUrl));

        mapping.setExpiresAt(request.getExpiresAt());
        repo.save(mapping);
    }

}