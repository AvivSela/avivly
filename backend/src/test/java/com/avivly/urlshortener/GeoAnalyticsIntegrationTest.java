package com.avivly.urlshortener;

import com.avivly.urlshortener.dto.CreateLinkRequest;
import com.avivly.urlshortener.model.ClickAnalytics;
import com.avivly.urlshortener.model.GeoStatus;
import com.avivly.urlshortener.model.ShortLink;
import com.avivly.urlshortener.model.User;
import com.avivly.urlshortener.repository.ClickAnalyticsRepository;
import com.avivly.urlshortener.repository.UserRepository;
import com.avivly.urlshortener.service.LinkService;
import com.avivly.urlshortener.support.AuthTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestPropertySource(properties = "geo.db.path=src/test/resources/GeoLite2-City-Test.mmdb")
class GeoAnalyticsIntegrationTest extends AuthTestSupport {

    @Autowired TestRestTemplate rest;
    @Autowired LinkService linkService;
    @Autowired ClickAnalyticsRepository clickRepo;
    @Autowired UserRepository userRepo;

    private ShortLink testLink;
    private String token;

    @BeforeEach
    void setUp() {
        String email = "geo-test-" + UUID.randomUUID() + "@test.com";
        token = registerAndGetToken(email);
        User user = userRepo.findByEmail(email).orElseThrow();
        testLink = linkService.create(new CreateLinkRequest(
            "https://example.com/geo-test", null, null, null, null, null, null), user.getId());
    }

    @Test
    void loopbackIpClickPersistsPrivateGeoStatus() {
        rest.getForEntity("/" + testLink.getShortCode(), Void.class);

        await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
            List<ClickAnalytics> clicks =
                clickRepo.findByShortCodeOrderByClickedAtDesc(testLink.getShortCode());
            assertThat(clicks).isNotEmpty();
            ClickAnalytics last = clicks.get(0);
            assertThat(last.getGeoStatus()).isEqualTo(GeoStatus.PRIVATE);
            assertThat(last.getCountry()).isNull();
            assertThat(last.getCity()).isNull();
        });
    }

    @Test
    void publicIpClickPersistsResolvedGeoStatus() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Real-IP", "81.2.69.142");
        rest.exchange("/" + testLink.getShortCode(), HttpMethod.GET,
            new HttpEntity<>(headers), Void.class);

        await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
            List<ClickAnalytics> clicks =
                clickRepo.findByShortCodeOrderByClickedAtDesc(testLink.getShortCode());
            assertThat(clicks).isNotEmpty();
            assertThat(clicks.get(0).getGeoStatus()).isEqualTo(GeoStatus.RESOLVED);
            assertThat(clicks.get(0).getCountry()).isNotBlank();
        });
    }

    @Test
    void analyticsEndpointReturnsGeoFields() {
        ResponseEntity<String> res = rest.exchange(
            "/api/links/" + testLink.getShortCode() + "/analytics",
            HttpMethod.GET, new HttpEntity<>(bearerHeaders(token)), String.class);
        assertThat(res.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(res.getBody()).contains("topCountries", "topCities");
    }

    @Test
    void analyticsEndpointReturnsEmptyGeoArraysForNoData() {
        ResponseEntity<String> res = rest.exchange(
            "/api/links/" + testLink.getShortCode() + "/analytics",
            HttpMethod.GET, new HttpEntity<>(bearerHeaders(token)), String.class);
        assertThat(res.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(res.getBody()).contains("\"topCountries\":[]", "\"topCities\":[]");
    }
}
