package com.mayank.urlshortener.service;

import com.mayank.urlshortener.dto.ShortenUrlRequest;
import com.mayank.urlshortener.dto.ShortenUrlResponse;
import com.mayank.urlshortener.dto.UrlStatsResponse;
import com.mayank.urlshortener.exception.InvalidUrlException;
import com.mayank.urlshortener.exception.UrlNotFoundException;
import com.mayank.urlshortener.model.UrlMapping;
import com.mayank.urlshortener.repository.UrlRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;

@Service
public class UrlService {

    @Autowired
    private UrlRepository repo;
    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    public ShortenUrlResponse shortenUrl(ShortenUrlRequest request) {
        String originalUrl = request.getUrl();

        if (!isValidUrl(originalUrl)) {
            throw new InvalidUrlException("Invalid URL format. Please provide a valid http or https URL.");
        }

        UrlMapping mapping = new UrlMapping();
        mapping.setOriginalUrl(originalUrl);
        mapping.setCreatedAt(LocalDateTime.now());

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

        try {
            redisTemplate.opsForValue().set(shortUrl, mapping.getOriginalUrl());
        } catch (Exception e) {
            System.out.println("Redis write error: " + e.getMessage());
        }

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

    public UrlStatsResponse getStats(String shortUrl) {
        UrlMapping mapping = repo.findByShortUrl(shortUrl)
                .orElseThrow(() -> new UrlNotFoundException("URL not found: " + shortUrl));

        return new UrlStatsResponse(
                mapping.getShortUrl(),
                mapping.getOriginalUrl(),
                mapping.getClickCount(),
                mapping.getCreatedAt()
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

}