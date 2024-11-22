package com.freighthub.auth_service.config;

import com.freighthub.auth_service.util.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class RequestForwardingFilter extends OncePerRequestFilter {

    @Value("${core.backend.url}")
    private String CORE_BACKEND;

    @Autowired
    private RestTemplate restTemplate;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        System.out.println("Request URI: " + request.getRequestURI());


        if (shouldNotFilter(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String queryString = request.getQueryString();
        // String coreBackendUrl = CORE_BACKEND + request.getRequestURI().substring("/api".length());

        // Construct the core backend URL and append the query string if it exists

        String coreBackendUrl = CORE_BACKEND + request.getRequestURI().substring("/api".length());
        if (queryString != null && !queryString.isEmpty()) {
            coreBackendUrl += "?" + queryString;
        }

        System.out.println("Forwarding request to core backend: " + coreBackendUrl);
        HttpHeaders headers = new HttpHeaders();
        Collections.list(request.getHeaderNames()).forEach(headerName ->
                headers.add(headerName, request.getHeader(headerName)));

        byte[] requestBody = StreamUtils.copyToByteArray(request.getInputStream());
        HttpMethod method = HttpMethod.valueOf(request.getMethod());

        HttpEntity<byte[]> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<byte[]> backendResponse = restTemplate.exchange(
                    coreBackendUrl, method, entity, byte[].class);
            response.setStatus(backendResponse.getStatusCodeValue());
            response.setContentType("application/json");

            if (backendResponse.getBody() != null) {
                response.getOutputStream().write(backendResponse.getBody());
            } else {
                response.getWriter().write("");
            }

        } catch (Exception e) {
            setErrorResponse(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, response, "Error forwarding request to core backend: " + e.getMessage());
        }

    }

      @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        return path.startsWith("/api/auth") || path.startsWith("/swagger-ui") || path.startsWith("/v3/api-docs") || path.startsWith("/actuator/");
    }

    private void setErrorResponse(int status, HttpServletResponse response, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        ApiResponse<String> errorResponse = new ApiResponse<>(status, message);
        response.getWriter().write(new ObjectMapper().writeValueAsString(errorResponse));
    }

}
