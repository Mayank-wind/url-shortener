package com.mayank.urlshortener.repository;

import com.mayank.urlshortener.model.UrlMapping;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UrlRepository extends JpaRepository<UrlMapping, Long> {
    Optional<UrlMapping> findByShortUrl(String shortUrl);
    Optional<UrlMapping> findFirstByOriginalUrlOrderByIdAsc(String originalUrl);

}