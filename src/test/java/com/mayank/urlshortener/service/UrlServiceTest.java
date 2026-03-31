package com.mayank.urlshortener.service;

import com.mayank.urlshortener.dto.ShortenUrlRequest;
import com.mayank.urlshortener.dto.ShortenUrlResponse;
import com.mayank.urlshortener.exception.InvalidUrlException;
import com.mayank.urlshortener.model.UrlMapping;
import com.mayank.urlshortener.repository.UrlRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import java.time.LocalDateTime;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UrlServiceTest {

    @Mock
    private UrlRepository repo;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @InjectMocks
    private UrlService service;

    @Test
    void shouldReturnExistingShortUrlForDuplicateOriginalUrl() {
        ShortenUrlRequest request = new ShortenUrlRequest();
        request.setUrl("https://google.com");

        UrlMapping mapping = new UrlMapping();
        mapping.setId(1L);
        mapping.setOriginalUrl("https://google.com");
        mapping.setShortUrl("b");
        mapping.setCreatedAt(LocalDateTime.now());

        when(repo.findFirstByOriginalUrlOrderByIdAsc("https://google.com"))
                .thenReturn(Optional.of(mapping));

        ShortenUrlResponse response = service.shortenUrl(request);

        assertEquals("b", response.getShortUrl());
        assertEquals("https://google.com", response.getOriginalUrl());
        verify(repo, never()).save(any());
    }

    @Test
    void shouldThrowExceptionForInvalidUrl() {
        ShortenUrlRequest request = new ShortenUrlRequest();
        request.setUrl("google.com");

        assertThrows(InvalidUrlException.class, () -> service.shortenUrl(request));
        verify(repo, never()).save(any());
    }
}
