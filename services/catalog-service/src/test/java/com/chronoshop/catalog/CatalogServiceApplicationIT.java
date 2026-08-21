package com.chronoshop.catalog;

import com.chronoshop.dto.CatalogDtos.BrandRequest;
import com.chronoshop.dto.CatalogDtos.BrandResponse;
import com.chronoshop.dto.CatalogDtos.CategoryRequest;
import com.chronoshop.dto.CatalogDtos.CategoryResponse;
import com.chronoshop.dto.WatchDtos.WatchRequest;
import com.chronoshop.dto.WatchDtos.WatchResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = "app.seed.enabled=false")
@Testcontainers
class CatalogServiceApplicationIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void createAndSearchWatch_worksThroughRealDatabase() {
        HttpHeaders adminHeaders = new HttpHeaders();
        adminHeaders.set("X-User-Id", "1");
        adminHeaders.set("X-User-Roles", "ADMIN");
        adminHeaders.setContentType(MediaType.APPLICATION_JSON);

        BrandRequest brandReq = new BrandRequest("Longines", "Švajcarska", "Test brend", null);
        BrandResponse brand = restTemplate.exchange("/api/brands", HttpMethod.POST,
                new HttpEntity<>(brandReq, adminHeaders), BrandResponse.class).getBody();
        assertThat(brand).isNotNull();

        CategoryRequest categoryReq = new CategoryRequest("TestCategory", "Test kategorija");
        CategoryResponse category = restTemplate.exchange("/api/categories", HttpMethod.POST,
                new HttpEntity<>(categoryReq, adminHeaders), CategoryResponse.class).getBody();
        assertThat(category).isNotNull();

        WatchRequest watchReq = new WatchRequest(
                "Master Collection", "L2.128", brand.id(), category.id(), "Opis",
                new BigDecimal("1500.00"), 4, null, null, null, null, List.of(), true, null, null, null);
        var watchResponseEntity = restTemplate.exchange("/api/watches", HttpMethod.POST,
                new HttpEntity<>(watchReq, adminHeaders), WatchResponse.class);
        assertThat(watchResponseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        var searchResponse = restTemplate.getForObject(
                "/api/watches?search=Master Collection", String.class);
        assertThat(searchResponse).contains("Master Collection", "L2.128");
    }

    @Test
    void createWatch_withoutAuthentication_isUnauthorized() {
        WatchRequest watchReq = new WatchRequest(
                "Unauthorized Watch", "X-1", 1L, 1L, null,
                new BigDecimal("100.00"), 1, null, null, null, null, List.of(), true, null, null, null);

        var response = restTemplate.postForEntity("/api/watches", watchReq, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void createWatch_withNonAdminRole_isForbidden() {
        HttpHeaders customerHeaders = new HttpHeaders();
        customerHeaders.set("X-User-Id", "2");
        customerHeaders.set("X-User-Roles", "CUSTOMER");
        customerHeaders.setContentType(MediaType.APPLICATION_JSON);

        WatchRequest watchReq = new WatchRequest(
                "Unauthorized Watch", "X-1", 1L, 1L, null,
                new BigDecimal("100.00"), 1, null, null, null, null, List.of(), true, null, null, null);

        var response = restTemplate.exchange("/api/watches", HttpMethod.POST,
                new HttpEntity<>(watchReq, customerHeaders), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }
}
