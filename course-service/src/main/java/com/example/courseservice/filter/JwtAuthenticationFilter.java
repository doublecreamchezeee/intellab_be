package com.example.courseservice.filter;

import com.example.courseservice.configuration.JwtAuthenticationToken;
import com.example.courseservice.dto.ApiResponse;
import com.example.courseservice.dto.request.IntrospectRequest;
import com.example.courseservice.dto.response.IntrospectResponse;
import com.example.courseservice.repository.httpClient.IdentityClient;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

// AbstractPreAuthenticatedProcessingFilter

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {


    private final IdentityClient identityClient;

    /*@Override
    protected Object getPreAuthenticatedPrincipal(HttpServletRequest request) {
        return request.getHeader("Authorization");
    }

    @Override
    protected Object getPreAuthenticatedCredentials(HttpServletRequest request) {
        return "N/A";
    }*/

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            IntrospectRequest introspectRequest = new IntrospectRequest(token);
            //ApiResponse<IntrospectResponse> introspectResponse = identityClient.introspect(introspectRequest);

            if (true){ // || introspectResponse.getResult().isValid()) {
                // Set authentication in the context
                SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(token));
            } else {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid token");
                return;
            }
        }
        chain.doFilter(request, response);
    }
}
