package com.example.identityservice.utility;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.example.identityservice.configuration.PublicEndpoint;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
//import io.swagger.v3.oas.models.PathItem.HttpMethod;

@Component
@RequiredArgsConstructor
public class ApiEndpointSecurityInspector {

    private final RequestMappingHandlerMapping requestHandlerMapping;

    @Getter
    private final List<String> publicGetEndpoints = new ArrayList<>();
    @Getter
    private final List<String> publicPostEndpoints = new ArrayList<>();

    @PostConstruct
    public void init() {
        final var handlerMethods = requestHandlerMapping.getHandlerMethods();
        handlerMethods.forEach((requestInfo, handlerMethod) -> {
            if (handlerMethod.hasMethodAnnotation(PublicEndpoint.class)) {
                final var httpMethod = requestInfo.getMethodsCondition().getMethods().iterator().next().asHttpMethod();
                final var apiPaths = requestInfo.getPathPatternsCondition().getPatternValues();

                if (httpMethod.equals(GET)) {
                    publicGetEndpoints.addAll(apiPaths);
                } else if (httpMethod.equals(POST)) {
                    publicPostEndpoints.addAll(apiPaths);
                }
            }
        });

        // Add swagger-ui to public GET endpoints
        /*publicGetEndpoints.add("/swagger-ui/**");
        publicGetEndpoints.add("/v3/api-docs/**,");*/
        publicGetEndpoints.addAll(
                List.of(
                        "/swagger-ui.html",
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/v2/api-docs/**",
                        "/swagger-resources/**",
                        "/swagger-resources/**",
                        "/configuration/ui/**",
                        "/configuration/security/**",
                        "/swagger-ui.html",
                        "/webjars/**")
        );
    }

    public boolean isUnsecureRequest(@NonNull final HttpServletRequest request) {
        final var requestHttpMethod = HttpMethod.valueOf(request.getMethod());
        var unsecuredApiPaths = getUnsecuredApiPaths(String.valueOf(requestHttpMethod));
        unsecuredApiPaths = Optional.ofNullable(unsecuredApiPaths).orElseGet(ArrayList::new);

        return unsecuredApiPaths.stream().anyMatch(apiPath -> {
            boolean match = new AntPathMatcher().match(apiPath, request.getRequestURI());
            if (match) {
                System.out.println("Match found for path: " + apiPath);
            }
            return match;
        });
    }

    private List<String> getUnsecuredApiPaths(@NonNull final String httpMethodString) {
        HttpMethod httpMethod = HttpMethod.valueOf(httpMethodString.toUpperCase());

        if (httpMethod.equals(GET)) {
            return publicGetEndpoints;
        } else if (httpMethod.equals(POST)) {
            return publicPostEndpoints;
        }
        return Collections.emptyList();
    }
}
