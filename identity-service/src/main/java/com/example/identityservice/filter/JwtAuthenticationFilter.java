package com.example.identityservice.filter;

import com.example.identityservice.utility.ApiEndpointSecurityInspector;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import jakarta.servlet.FilterChain;
import org.apache.commons.lang3.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final FirebaseAuth firebaseAuth;
    private final ApiEndpointSecurityInspector apiEndpointSecurityInspector;

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String USER_ID_CLAIM = "user_id";

    @Override
    @SneakyThrows
    protected void doFilterInternal(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull FilterChain filterChain) {

        final var unsecuredApiBeingInvoked = apiEndpointSecurityInspector.isUnsecureRequest(request);

        // Only handle authentication for secured API endpoints
        if (Boolean.FALSE.equals(unsecuredApiBeingInvoked)) {
            final var authorizationHeader = request.getHeader(AUTHORIZATION_HEADER);

            // Check if the Authorization header is present and starts with Bearer
            if (StringUtils.isNotEmpty(authorizationHeader) && authorizationHeader.startsWith(BEARER_PREFIX)) {
                final var token = authorizationHeader.replace(BEARER_PREFIX, StringUtils.EMPTY);

                try {
                    // Verify the Firebase token
                    final var firebaseToken = firebaseAuth.verifyIdToken(token);

                    // Extract user ID from claims (or throw an exception if it's missing)
                    final var userId = Optional.ofNullable(firebaseToken.getClaims().get(USER_ID_CLAIM))
                            .orElseThrow(() -> new IllegalStateException("User ID claim missing"));

                    // Set the authentication in the SecurityContext
                    final var authentication = new UsernamePasswordAuthenticationToken(userId, null, null);
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } catch (FirebaseAuthException | IllegalStateException e) {
                    // Log the error and respond with a 401 Unauthorized status
                    logger.error("Authentication failed: {}", e);
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("Authentication failed: " + e);
                    return; // Prevent further processing of the request
                } catch (IllegalArgumentException e) {
                    // Handle token format errors or other argument-related exceptions
                    logger.error("Invalid token format: {}", e);
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("Invalid token format");
                    return; // Prevent further processing of the request
                }
            }
        }

        // Continue with the filter chain
        filterChain.doFilter(request, response);
    }
}
