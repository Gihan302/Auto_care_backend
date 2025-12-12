package com.autocare.autocarebackend;

import com.autocare.autocarebackend.models.ERole;
import com.autocare.autocarebackend.models.Role;
import com.autocare.autocarebackend.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
public class AutoCareBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(AutoCareBackendApplication.class, args);
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry
                        .addMapping("/**")
                        .allowedOrigins("*")
                        .allowedMethods("OPTIONS","GET","POST","DELETE","PATCH","PUT");
            }
        };
    }

    @Bean
    public CommandLineRunner initialRoles(RoleRepository roleRepository) {
        return args -> {
            if (roleRepository.findByName(ERole.ROLE_USER).isEmpty()) {
                roleRepository.save(new Role(ERole.ROLE_USER));
            }
            if (roleRepository.findByName(ERole.ROLE_ADMIN).isEmpty()) {
                roleRepository.save(new Role(ERole.ROLE_ADMIN));
            }
            if (roleRepository.findByName(ERole.ROLE_AGENT).isEmpty()) {
                roleRepository.save(new Role(ERole.ROLE_AGENT));
            }
            if (roleRepository.findByName(ERole.ROLE_ICOMPANY).isEmpty()) {
                roleRepository.save(new Role(ERole.ROLE_ICOMPANY));
            }
            if (roleRepository.findByName(ERole.ROLE_LCOMPANY).isEmpty()) {
                roleRepository.save(new Role(ERole.ROLE_LCOMPANY));
            }
        };
    }


}
