package com.freighthub.auth_service.config;

import com.freighthub.auth_service.util.JwtUtils;
import com.freighthub.auth_service.service.UserService;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserService userService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String jwt = parseJwt(request);
        if (jwt != null && validateJwtToken(jwt)) {
            String username = getUsernameFromJwtToken(jwt);

            UserDetails userDetails = userService.loadUserByUsername(username);
            if (userDetails != null) {
                System.out.println("User details: " + userDetails);
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        filterChain.doFilter(request, response);
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
}
