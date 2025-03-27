package com.example.apigateway.configuration;

import com.example.apigateway.service.IdentityService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Component
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PACKAGE, makeFinal = true)
public class AuthenticationFilter implements GlobalFilter, Ordered {
    IdentityService identityService;
    ObjectMapper objectMapper;
    private final String uuidPattern = "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}";

    @NonFinal
    private String[] publicEndpoints = {
            "/identity/auth/.*",

    };

    @NonFinal
    private String[] exploredEndpoints = {
            "/course/courses",
            "/course/reviews",
            "/course/lessons",
            "/course/courses/categories",
            "/course/courses/[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}/categories$",
            "/course/exceptEnrolled",
            "/course/courses/[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}/lessons$",
            "/course/courses/[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}/certificate$",
            "/problem/problems/search",
            "/problem/problems",
            "/problem/problems/[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}/partial-boilerplate$",
            "/course/reviews/[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$",
            "/course/courses/[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}/reviews$",
            "/course/courses/[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}/reviews-stats$",
           // "/identity/profile/single",
            "/identity/profile/single/public",
            "/identity/profile/multiple",
            "/problem/problems/[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}/comments$",
            "/problem/problem-comments/[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$",
            "/problem/problem-comments/[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}/root-and-children$",
            "/problem/problem-comments/[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}/children$",
            "/course/courses/enrollPaidCourse",
            "/course/courses/disenroll",
            "/identity/leaderboard",
    };

    @NonFinal
    private String[] restrictedEndpoints = {
            "/course/enrollCourses/.*",
    };

    @NonFinal
    private String[] hybridEndpoints = {
            "/course/enrollCourses/.*",
            "/problem/problems/search",
            "/course/courses/[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}/comments$",
            "/course/courses/comments/[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$",
            "/course/courses/comments/[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}/children$",
            "/course/courses/search",
            "/course/courses/[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$",
            "/course/courses/details",
            "/course/courses/courseList/me",
            "/problem/problem-submissions/submitList/me",
            "/problem/statistics/progress/language",
            "/problem/statistics/progress/level",
            "/identity/leaderboard",
            "/problem/problems/[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$",
    };

    @Value("${app.api-prefix}")
    @NonFinal
    private String apiPrefix;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        //log.info("Request path: {}", request.getURI().getPath());
        //log.info("isPublicEndpoint(request): {}", isPublicEndpoint(request));
        //log.info("isExploredEndpoint(request): {}", isExploredEndpoint(request));

        if (isPublicEndpoint(request) || isExploredEndpoint(request)) {
            return chain.filter(exchange);
        }

        List<String> authHeader = request.getHeaders().get(HttpHeaders.AUTHORIZATION);
        boolean isHybrid = isHybridEndpoint(request);

        if (CollectionUtils.isEmpty(authHeader)) {
            if (isHybrid) { // ko có token -> hybrid -> đi tiếp
                return chain.filter(exchange);
            } else {
                return unauthenticated(exchange.getResponse());
            }
        }

        String token = authHeader.get(0).replace("Bearer ", "");

        //log.info("token: {}", token);

        return identityService.validateToken(token).flatMap( response -> {

            //log.info("Objects.requireNonNull(response.getBody()).isValidated(): " + Objects.requireNonNull(response.getBody()).isValidated());

            if (Objects.requireNonNull(response.getBody()).isValidated()) {
                String role = response.getBody().getRole();
                if (role.equals("user"))
                {
                    String premium = response.getBody().getPremium();
                    role += "," + premium;
                }

                ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                        .header("X-UserId", response.getBody().getUserId())
                        .header("X-UserRole", role)
                        .build();


                return chain.filter(exchange.mutate().request(modifiedRequest).build());
            } else if (isHybrid) { // sai token -> hybrid -> đi tiếp
                return chain.filter(exchange);
            } else {
                return unauthenticated(exchange.getResponse());
            }
        }).onErrorResume(throwable -> isHybrid ? chain.filter(exchange) : unknownErrorOccurred(exchange.getResponse()));
    }

    private boolean isPublicEndpoint(ServerHttpRequest request) {
        return Arrays.stream(publicEndpoints)
                .anyMatch(s -> request.getURI().getPath().matches(apiPrefix + s));
    }

    private boolean isHybridEndpoint(ServerHttpRequest request) {
        return Arrays.stream(hybridEndpoints)
                .anyMatch(s -> request.getURI().getPath().matches(apiPrefix + s));
    }

    private boolean isExploredEndpoint(ServerHttpRequest request) {
        if (request.getMethod() == HttpMethod.GET || request.getMethod() == HttpMethod.POST) {
            /*boolean isRestricted = Arrays.stream(restrictedEndpoints)
                    .anyMatch(s -> request.getURI().getPath().matches(apiPrefix + s));

            if (isRestricted) {
                return false;
            }*/

            return Arrays.stream(exploredEndpoints)
                    .anyMatch(s -> request.getURI().getPath().matches(apiPrefix + s));
        }
        return false;

    }

    @Override
    public int getOrder() {
        return -1;
    }

    Mono<Void> unauthenticated(ServerHttpResponse response) {
        String message = "Unauthenticated";

        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String body = "{\"message\":\"" + message + "\"}";

        return response.writeWith(
                Mono.just(response.bufferFactory().wrap(body.getBytes()))
        );
    }

    Mono<Void> unknownErrorOccurred(ServerHttpResponse response) {
        String message = "Unauthenticated or unknown error occurred";

        response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String body = "{\"message\":\"" + message + "\"}";

        return response.writeWith(
                Mono.just(response.bufferFactory().wrap(body.getBytes()))
        );
    }

}
