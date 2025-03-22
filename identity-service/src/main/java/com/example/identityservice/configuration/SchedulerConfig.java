package com.example.identityservice.configuration;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "scheduler")
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SchedulerConfig {
    String customCronExpression = "0 0 12 * * ?";
}
