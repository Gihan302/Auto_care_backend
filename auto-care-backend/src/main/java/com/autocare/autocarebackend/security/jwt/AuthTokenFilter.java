package com.autocare.autocarebackend.security.jwt;

import com.autocare.autocarebackend.security.services.UserDetailsServicelmpl;
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
    private UserDetailsServicelmpl userDetailsService;

    private static final Logger logger = LoggerFactory.getLogger(AuthTokenFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        logger.info("🔍 Processing: " + request.getMethod() + " " + request.getRequestURI());

        try {
            String jwt = parseJwt(request);
            logger.info("🔐 JWT Status: " + (jwt != null ? "Token found" : "No token"));

            if (jwt != null && jwtUtils.validateJwtToken(jwt)) {
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
                logger.info("🎯 Principal Type: " +
                        SecurityContextHolder.getContext().getAuthentication().getPrincipal().getClass().getSimpleName());

            } else {
                logger.warn("❌ Invalid/missing JWT token - clearing SecurityContext");
                SecurityContextHolder.clearContext();
            }
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
        boolean skip = path.startsWith("/api/auth/") || path.equals("/error");
        if (skip) {
            logger.info("⏭️ Skipping JWT filter for: " + path);
        }
        return skip;
    }
}
