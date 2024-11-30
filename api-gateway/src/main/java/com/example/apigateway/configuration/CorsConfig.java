//package com.example.apigateway.configuration;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.reactive.config.WebFluxConfigurer;
//import org.springframework.web.cors.CorsConfiguration;
//import org.springframework.web.cors.reactive.CorsConfigurationSource;
//import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
//
//@Configuration
//public class CorsConfig  implements WebFluxConfigurer {
//    @Bean
//    public CorsConfigurationSource corsConfigurationSource() {
//        CorsConfiguration corsConfig = new CorsConfiguration();
//        corsConfig.addAllowedOrigin("http://localhost:3000");
//        corsConfig.addAllowedOrigin("http://localhost:3001");
//
//        corsConfig.addAllowedMethod("*");
//        corsConfig.addAllowedHeader("*");
//        corsConfig.setAllowCredentials(true);
//
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        source.registerCorsConfiguration("/**", corsConfig);
//        return source;
//    }
//}
