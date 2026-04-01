package com.mayank.urlshortener.controller;


import com.mayank.urlshortener.dto.ShortenUrlResponse;
import com.mayank.urlshortener.dto.UrlStatsResponse;
import com.mayank.urlshortener.service.RateLimitService;
import com.mayank.urlshortener.service.UrlService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UrlController.class)
class UrlControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UrlService service;
    @MockitoBean
    private RateLimitService rateLimitService;


    @Autowired
    private ObjectMapper objectMapper;


    @Test
    void shouldShortenUrl() throws Exception {
        String requestBody = """
                {
                  "url": "https://google.com"
                }
                """;

        when(service.shortenUrl(org.mockito.ArgumentMatchers.any()))
                .thenReturn(new ShortenUrlResponse("b", "https://google.com"));

        mockMvc.perform(post("/api/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shortUrl").value("b"))
                .andExpect(jsonPath("$.originalUrl").value("https://google.com"));
    }

    @Test
    void shouldReturnStats() throws Exception {
        when(service.getStats("b"))
                .thenReturn(new UrlStatsResponse("b", "https://google.com", 5L, LocalDateTime.now(), null));

        mockMvc.perform(get("/api/stats/b"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shortUrl").value("b"))
                .andExpect(jsonPath("$.originalUrl").value("https://google.com"))
                .andExpect(jsonPath("$.clickCount").value(5));
    }
}
