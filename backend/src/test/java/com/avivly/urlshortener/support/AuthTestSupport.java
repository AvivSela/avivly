package com.avivly.urlshortener.support;

import com.avivly.urlshortener.dto.AuthRequest;
import com.avivly.urlshortener.dto.AuthResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;

public abstract class AuthTestSupport {

    @LocalServerPort
    protected int port;

    @Autowired
    protected TestRestTemplate restTemplate;

    protected String url(String path) {
        return "http://localhost:" + port + path;
    }

    protected String registerAndGetToken(String email) {
        AuthRequest req = new AuthRequest(email, "password123");
        return restTemplate.postForEntity(url("/api/auth/register"), req, AuthResponse.class)
            .getBody().token();
    }

    protected HttpHeaders bearerHeaders(String token) {
        HttpHeaders h = new HttpHeaders();
        h.setBearerAuth(token);
        return h;
    }
}
