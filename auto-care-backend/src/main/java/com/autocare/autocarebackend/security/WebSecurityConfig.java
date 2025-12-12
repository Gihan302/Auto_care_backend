
package com.autocare.autocarebackend.security;

import com.autocare.autocarebackend.security.jwt.AuthEntryPointJwt;
import com.autocare.autocarebackend.security.jwt.AuthTokenFilter;
import com.autocare.autocarebackend.security.services.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
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

@Configuration
@EnableWebSecurity
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
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("authorization", "content-type", "x-auth-token", "x-requested-with"));
        configuration.setAllowCredentials(true);
        configuration.addExposedHeader("x-auth-token");
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
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/api/v1/leasing-companies/**").permitAll()
                        .requestMatchers("/api/v1/insurance-companies/**").permitAll()
                        .requestMatchers("/api/leasing-plans").permitAll()
                        .requestMatchers("/api/leasing-plans/public/all").permitAll()
                        .requestMatchers("/api/insurance-plans").permitAll()
                        .requestMatchers("/api/insurance-plans/public/all").permitAll()
                        .requestMatchers("/api/advertisement/getconfrimad").permitAll()
                        .requestMatchers("/api/test/**").permitAll()
                        .requestMatchers("/api/autogenie/**").permitAll()
                        .requestMatchers("/advertisement/**").permitAll()
                        .requestMatchers("/admin/getallagents").permitAll()
                        .requestMatchers("/user/getlplan/**").permitAll()
                        .requestMatchers("/user/getiplan/**").permitAll()
                        .requestMatchers("/images/**").permitAll()
                        .requestMatchers("/api/agent/applications").hasRole("AGENT")
                        .requestMatchers("/api/agent/messages/**").hasRole("AGENT")
                        .requestMatchers("/api/agent/**").hasRole("AGENT")

                        // 🚩 CORE FIX APPLIED HERE: Using .authenticated() to allow any authenticated user to access messaging.
                        .requestMatchers("/api/messages/**").authenticated()

                        .requestMatchers("/api/icompany/**").hasAnyAuthority("ROLE_ICOMPANY", "ROLE_LCOMPANY", "ROLE_ADMIN", "ROLE_AGENT", "ROLE_USER")
                        .requestMatchers("/api/lcompany/**").hasAnyAuthority("ROLE_ICOMPANY", "ROLE_LCOMPANY", "ROLE_ADMIN", "ROLE_AGENT", "ROLE_USER")
                        .requestMatchers("/api/company/**").hasAnyAuthority("ROLE_LCOMPANY", "ROLE_ICOMPANY")
                        .anyRequest().authenticated()
                );

        http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}