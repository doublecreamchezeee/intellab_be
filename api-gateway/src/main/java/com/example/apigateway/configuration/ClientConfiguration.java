package com.example.apigateway.configuration;

import com.example.apigateway.client.IdentityClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

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
