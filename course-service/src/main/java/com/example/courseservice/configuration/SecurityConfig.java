package com.example.courseservice.configuration;

import com.example.courseservice.filter.JwtAuthenticationFilter;
import com.example.courseservice.repository.httpClient.IdentityClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    //@Autowired
    private final IdentityClient identityClient;

    public SecurityConfig(IdentityClient identityClient) {
        this.identityClient = identityClient;
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(identityClient);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sessionConfigurer -> sessionConfigurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
//                        .requestMatchers(HttpMethod.POST, "/courses/**").authenticated()
//                        .requestMatchers(HttpMethod.PUT, "/courses/**").authenticated()
//                        .requestMatchers(HttpMethod.DELETE, "/courses/**").authenticated()
//                        .requestMatchers(HttpMethod.POST, "/lessons/**").authenticated()
//                        .requestMatchers(HttpMethod.PUT, "/lessons/**").authenticated()
//                        .requestMatchers(HttpMethod.DELETE, "/lessons/**").authenticated()
                        .anyRequest().permitAll()
                )
                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);//identityClient

        return http.build();
    }

    private CorsConfigurationSource corsConfigurationSource() {
        final var corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowedOrigins(java.util.List.of("*"));
        corsConfiguration.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        corsConfiguration.setAllowedHeaders(List.of("Authorization", "Origin", "Content-Type", "Accept"));

        final var corsConfigurationSource = new UrlBasedCorsConfigurationSource();
        corsConfigurationSource.registerCorsConfiguration("/**", corsConfiguration);
        return corsConfigurationSource;
    }

}
