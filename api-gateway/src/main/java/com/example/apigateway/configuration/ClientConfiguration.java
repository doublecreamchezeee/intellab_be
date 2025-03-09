package com.example.apigateway.configuration;

import com.example.apigateway.client.IdentityClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import java.util.List;

@Configuration
public class ClientConfiguration {

    @Bean
    WebClient webClient(){
        String hostname = DotenvConfig.get("HOST_NAME");
        String port = DotenvConfig.get("IDENTITY_PORT");
        String baseUrl = "http://" + hostname + ":" + port + "/identity";
        return WebClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

//    @Bean
    CorsWebFilter corsWebFilter(){
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        String hostname = DotenvConfig.get("HOST_NAME");
        String identityPort = DotenvConfig.get("IDENTITY_PORT");
        String coursePort = DotenvConfig.get("COURSE_PORT");
        String apiGatewayPort = DotenvConfig.get("API_GATEWAY_PORT");
        String fePort = DotenvConfig.get("FRONTEND_PORT");
        String aiPort = DotenvConfig.get("AI_PORT");

        System.out.println(hostname + fePort);
        corsConfiguration.setAllowedOrigins(List.of(
                "http://" + hostname + ":" + identityPort,
                "http://" + hostname + ":" + coursePort,
                "http://" + hostname + ":" + apiGatewayPort,
                "http://" + hostname + ":" + fePort,
                "http://" + hostname + ":" + aiPort
        ));
        corsConfiguration.setAllowedHeaders(List.of("*"));
        corsConfiguration.setAllowedMethods(List.of("*"));
        corsConfiguration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource urlBasedCorsConfigurationSource = new UrlBasedCorsConfigurationSource();
        urlBasedCorsConfigurationSource.registerCorsConfiguration("/**", corsConfiguration);

        return new CorsWebFilter(urlBasedCorsConfigurationSource);
    }

    @Bean
    IdentityClient identityClient(WebClient webClient){
        HttpServiceProxyFactory httpServiceProxyFactory = HttpServiceProxyFactory
                .builderFor(
                        WebClientAdapter.create(webClient)
                )
                .build();
        return httpServiceProxyFactory.createClient(IdentityClient.class);
    }
}
