package com.example.problemservice.configuration;

import com.example.problemservice.client.IdentityClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class ClientConfiguration {
    @Bean
    WebClient identityWebClient() {
        String hostname = DotenvConfig.get("HOST_NAME");

        String port = DotenvConfig.get("IDENTITY_PORT");

        String baseUrl = "http://" + hostname + ":" + port + "/identity";
        return WebClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    @Bean
    IdentityClient identityClient(WebClient identityWebClient){
        HttpServiceProxyFactory httpServiceProxyFactory = HttpServiceProxyFactory
                .builderFor(
                        WebClientAdapter.create(identityWebClient)
                ).build();
        return httpServiceProxyFactory.createClient(IdentityClient.class);

    }

}
