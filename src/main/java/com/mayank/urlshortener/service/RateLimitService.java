package com.mayank.urlshortener.service;

import com.mayank.urlshortener.exception.RateLimitExceededException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitService {

    private static final int MAX_REQUESTS = 5;
    private static final long WINDOW_SECONDS = 60;

    private final Map<String, RequestInfo> requestStore = new ConcurrentHashMap<>();

    public void validateRequest(String clientIp) {
        LocalDateTime now = LocalDateTime.now();
        RequestInfo info = requestStore.get(clientIp);

        if (info == null || info.windowStart.plusSeconds(WINDOW_SECONDS).isBefore(now)) {
            requestStore.put(clientIp, new RequestInfo(now, 1));
            return;
        }

        if (info.requestCount >= MAX_REQUESTS) {
            throw new RateLimitExceededException("Too many requests. Please try again later.");
        }

        info.requestCount++;
    }

    private static class RequestInfo {
        private LocalDateTime windowStart;
        private int requestCount;

        private RequestInfo(LocalDateTime windowStart, int requestCount) {
            this.windowStart = windowStart;
            this.requestCount = requestCount;
        }
    }
}
