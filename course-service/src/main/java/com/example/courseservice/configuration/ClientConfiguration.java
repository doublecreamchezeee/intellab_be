package com.example.courseservice.configuration;
import com.example.courseservice.client.AiServiceClient;
import com.example.courseservice.client.IdentityClient;
import com.example.courseservice.client.ProblemClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
@Slf4j
public class ClientConfiguration {
    @Bean
    WebClient problemWebClient(){
        String hostname = DotenvConfig.get("HOST_NAME");
        String port = DotenvConfig.get("PROBLEM_PORT");

        String baseUrl = "http://" + hostname + ":" + port + "/problem";
        return WebClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    @Bean
    WebClient identityWebClient(){
        String hostname = DotenvConfig.get("HOST_NAME");

        String port = DotenvConfig.get("IDENTITY_PORT");

        String baseUrl = "http://" + hostname + ":" + port + "/identity";
        return WebClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    @Bean
    WebClient aiServiceWebClient(){
        String hostname = DotenvConfig.get("HOST_NAME");
        String port = DotenvConfig.get("AI_PORT");

        String baseUrl = "http://" + hostname + ":" + port + "/ai";
        //log.info("AI Service Base URL: {}", baseUrl);
        return WebClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    @Bean
    ProblemClient problemClient(WebClient problemWebClient){
        HttpServiceProxyFactory httpServiceProxyFactory = HttpServiceProxyFactory
                .builderFor(
                        WebClientAdapter.create(problemWebClient)
                )
                .build();
        return httpServiceProxyFactory.createClient(ProblemClient.class);
    }

    @Bean
    IdentityClient identityClient(WebClient identityWebClient){
        HttpServiceProxyFactory httpServiceProxyFactory = HttpServiceProxyFactory
                .builderFor(
                        WebClientAdapter.create(identityWebClient)
                ).build();
        return httpServiceProxyFactory.createClient(IdentityClient.class);

    }

    @Bean
    AiServiceClient aiServiceClient(WebClient aiServiceWebClient) {
        HttpServiceProxyFactory httpServiceProxyFactory = HttpServiceProxyFactory
                .builderFor(
                        WebClientAdapter.create(aiServiceWebClient)
                ).build();
        return httpServiceProxyFactory.createClient(AiServiceClient.class);
    }
}
