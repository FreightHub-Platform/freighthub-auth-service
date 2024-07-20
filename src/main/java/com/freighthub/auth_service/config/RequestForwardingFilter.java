package com.freighthub.auth_service.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class RequestForwardingFilter extends OncePerRequestFilter {

    @Value("${core.backend.url}")
    private String CORE_BACKEND;

    @Autowired
    private RestTemplate restTemplate;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (request.getRequestURI().startsWith("/api/protected")) {
            String CORE_BACKEND_URL = CORE_BACKEND + request.getRequestURI().substring("/api".length());
            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization", request.getHeader("Authorization"));

            HttpEntity<String> entity = new HttpEntity<>(headers);
            try {
                ResponseEntity<String> backendResponse = restTemplate.exchange(CORE_BACKEND_URL, HttpMethod.valueOf(request.getMethod()), entity, String.class);

                response.setStatus(backendResponse.getStatusCodeValue());
                backendResponse.getHeaders().forEach((key, values) -> values.forEach(value -> response.addHeader(key, value)));
                response.getWriter().write(backendResponse.getBody());
            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write("Error forwarding request to core backend: " + e.getMessage());
            }
        } else {
            filterChain.doFilter(request, response);
        }
    }
}
