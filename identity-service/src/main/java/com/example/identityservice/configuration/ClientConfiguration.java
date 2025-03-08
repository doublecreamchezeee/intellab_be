package com.example.identityservice.configuration;
import com.example.identityservice.client.CourseClient;
import com.example.identityservice.client.ProblemClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class ClientConfiguration {
    @Bean
    WebClient problemWebClient(){
        String hostname = DotenvConfig.get("HOST_NAME");
        String port = DotenvConfig.get("PROBLEM_PORT");
        String baseUrl = "http://" + hostname + ":" + port + "/problem";
        return WebClient.builder()
                .baseUrl(baseUrl)
                .filter(addUserIdHeaderFilter())
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

    private ExchangeFilterFunction addUserIdHeaderFilter() {
        return (request, next) -> next.exchange(
                ClientRequest.from(request)
                        .headers(headers -> headers.set("X-UserId", getUserIdFromAuthentication()))
                        .build()
        );
    }

    private String getUserIdFromAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof String userId) {
            return userId;
        }
        return "unknown-user";
    }

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
