package com.autocare.autocarebackend.security;

import com.autocare.autocarebackend.security.jwt.AuthEntryPointJwt;
import com.autocare.autocarebackend.security.jwt.AuthTokenFilter;
import com.autocare.autocarebackend.security.services.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
// --- THIS IS THE FIX ---
// We replace the deprecated 'EnableGlobalMethodSecurity' with 'EnableMethodSecurity'
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
// --- END OF FIX ---
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

import org.springframework.core.annotation.Order;

@Configuration
@EnableWebSecurity
// --- THIS IS THE FIX ---
// Replaced @EnableGlobalMethodSecurity(prePostEnabled = true) with the new annotation
@EnableMethodSecurity
public class WebSecurityConfig {
    @Autowired
    UserDetailsServiceImpl userDetailsService;

    @Autowired
    private AuthEntryPointJwt unauthorizedHandler;

    @Bean
    public AuthTokenFilter authenticationJwtTokenFilter(){
        return new AuthTokenFilter();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE"));
        configuration.setAllowedHeaders(Arrays.asList("authorization", "content-type", "x-auth-token"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    @Order(1)
    public SecurityFilterChain bannerAdsFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/banner-ads/**")
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .csrf(csrf -> csrf.disable());
        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain authFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/auth/**")
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .csrf(csrf -> csrf.disable());
        return http.build();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{
        http
                .securityMatcher("/**")
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .exceptionHandling(ex -> ex.authenticationEntryPoint(unauthorizedHandler))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/leasing-companies/**").permitAll()
                        .requestMatchers("/api/v1/insurance-companies/**").permitAll()
                        //.requestMatchers("/api/leasing-plans").permitAll()
                        .requestMatchers("/api/leasing-plans/public/all").permitAll()
                        .requestMatchers("/api/advertisement/getconfrimad").permitAll()
                        .requestMatchers("/api/test/**").permitAll()
                        .requestMatchers("/api/autogenie/**").permitAll()
                        .requestMatchers("/advertisement/**").permitAll()
                        .requestMatchers("/admin/getallagents").permitAll()
                        .requestMatchers("/user/getlplan/**").permitAll()
                        .requestMatchers("/user/getiplan/**").permitAll()
                        .requestMatchers("/api/icompany/**").authenticated()
                        .requestMatchers("/api/lcompany/**").authenticated()
                        .anyRequest().authenticated()
                );

        http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}