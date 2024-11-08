package com.freighthub.auth_service.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.freighthub.auth_service.service.UserService;
import com.freighthub.auth_service.util.ApiResponse;
import com.freighthub.auth_service.util.JwtUtils;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserService userService;

    @Override
    public void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String jwt = parseJwt(request);
        System.out.println("JWT: " + jwt);
        if (jwt != null && validateJwtToken(jwt)) {
            String username = getUsernameFromJwtToken(jwt);

            UserDetails userDetails = userService.loadUserByUsername(username);

            if (userDetails != null) {
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    System.out.println("User " + username + " authenticated");
                    filterChain.doFilter(request, response);
            }
            else {
                setErrorResponse(HttpServletResponse.SC_UNAUTHORIZED, response, "Invalid credentials");
            }
        }
        else {
            setErrorResponse(HttpServletResponse.SC_UNAUTHORIZED, response, "JWT is null or not valid");
        }
    }

    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");

        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }

        return null;
    }

    private boolean validateJwtToken(String authToken) {
        try {
            Jwts.parser().setSigningKey(jwtUtils.getJwtSecret()).parseClaimsJws(authToken);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private String getUsernameFromJwtToken(String token) {
        return Jwts.parser()
                .setSigningKey(jwtUtils.getJwtSecret())
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    private void setErrorResponse(int status, HttpServletResponse response, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        ApiResponse<String> errorResponse = new ApiResponse<>(status, message);
        response.getWriter().write(new ObjectMapper().writeValueAsString(errorResponse));
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        return path.startsWith("/api/auth") || path.startsWith("/swagger-ui") || path.startsWith("/v3/api-docs") || path.startsWith("/actuator/");
    }
}
