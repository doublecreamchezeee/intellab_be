package com.example.paymentservice.configuration;

import com.example.paymentservice.client.CourseClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class ClientConfiguration {
    @Bean
    public WebClient courseWebClient() {
        String hostname = DotenvConfig.get("HOST_NAME");
        String port = DotenvConfig.get("COURSE_PORT");
        String baseUrl = "http://" + hostname + ":" + port + "/course";
        return WebClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    @Bean
    public CourseClient courseClient(WebClient courseWebClient) {
        HttpServiceProxyFactory httpServiceProxyFactory = HttpServiceProxyFactory
                .builderFor(
                        WebClientAdapter.create(courseWebClient)
                ).build();
        return httpServiceProxyFactory.createClient(CourseClient.class);
    }
}
