package com.autocare.autocarebackend.security.jwt;

import com.autocare.autocarebackend.security.services.UserDetailsServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class AuthTokenFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    private static final Logger logger = LoggerFactory.getLogger(AuthTokenFilter.class);

    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        logger.info("🔍 Processing: " + request.getMethod() + " " + request.getRequestURI());

        try {
            String jwt = parseJwt(request);
            logger.info("🔐 JWT Status: " + (jwt != null ? "Token found" : "No token"));

            // --- MODIFIED LOGIC FOR CLARITY ---
            if (jwt != null) {
                // First, check if the token is valid (not expired, correct signature)
                boolean isValid = jwtUtils.validateJwtToken(jwt);

                if (isValid) {
                    String username = jwtUtils.getUsernameFromJwtToken(jwt);
                    logger.info("👤 Username: " + username);

                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    logger.info("✅ User loaded: " + userDetails.getUsername() + " with authorities: " + userDetails.getAuthorities());

                    // CRITICAL: Create the authentication token correctly
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // CRITICAL: Set the SecurityContext
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    // Verify SecurityContext is set
                    logger.info("🎯 SecurityContext SET - Authenticated: " +
                            SecurityContextHolder.getContext().getAuthentication().isAuthenticated());
                } else {
                    // This is the most likely error path
                    logger.warn("❌ JWT Token was FOUND but is INVALID (e.g., expired or bad signature). Clearing SecurityContext.");
                    SecurityContextHolder.clearContext();
                }

            } else {
                logger.info("ℹ️ No JWT token found in request, skipping authentication.");
                // SecurityContext is implicitly cleared by default
            }
            // --- END MODIFIED LOGIC ---

        } catch (Exception e) {
            logger.error("💥 JWT Filter Exception: {}", e.getMessage(), e);
            SecurityContextHolder.clearContext();
        }

        // CRITICAL: Always call filterChain.doFilter
        filterChain.doFilter(request, response);
    }

    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");
        logger.info("📋 Auth Header: " + (headerAuth != null ? headerAuth.substring(0, Math.min(20, headerAuth.length())) + "..." : "null"));

        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            String token = headerAuth.substring(7);
            logger.info("🎫 Token Length: " + token.length());
            return token;
        }

        return null;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        boolean skip = path.startsWith("/api/auth/");
        if (skip) {
            logger.info("⏭️ Skipping JWT filter for: " + path);
        }
        return skip;
    }
}
