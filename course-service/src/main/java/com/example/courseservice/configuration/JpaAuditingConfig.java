package com.example.courseservice.configuration;


import com.example.courseservice.utils.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Optional;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
@RequiredArgsConstructor
public class JpaAuditingConfig {
    // This class is used to enable JPA auditing features in the application.
    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> Optional.ofNullable(UserContext.getCurrentUser());
    }

}
