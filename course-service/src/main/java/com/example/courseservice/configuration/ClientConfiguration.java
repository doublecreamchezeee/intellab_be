package com.example.courseservice.configuration;
import com.example.courseservice.client.ProblemClient;
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
        String port = DotenvConfig.get("PROBLEM_PORT");
        String baseUrl = "http://" + hostname + ":" + port + "/problem";
        return WebClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    @Bean
    ProblemClient problemClient(WebClient webClient){
        HttpServiceProxyFactory httpServiceProxyFactory = HttpServiceProxyFactory
                .builderFor(
                        WebClientAdapter.create(webClient)
                )
                .build();
        return httpServiceProxyFactory.createClient(ProblemClient.class);
    }

}
