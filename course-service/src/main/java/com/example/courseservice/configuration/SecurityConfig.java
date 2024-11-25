package com.example.courseservice.configuration;

import com.example.courseservice.repository.httpClient.IdentityClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;

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
        http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/courses/**").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/courses/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/courses/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/lessons/**").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/lessons/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/lessons/**").authenticated()
                        .anyRequest().permitAll()
                )
                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);//identityClient

        return http.build();
    }


}
