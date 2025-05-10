package com.example.courseservice.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.io.IOException;

@Slf4j
//@Component
public class RequestLoggingFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String method = httpRequest.getMethod();
        String requestPath = httpRequest.getRequestURI();

        // Build the query string
        String queryString = httpRequest.getQueryString();
        String fullPath = queryString != null ? requestPath + "?" + queryString : requestPath;


        long startTime = System.currentTimeMillis(); // Record start time

        // Proceed with the request
        chain.doFilter(request, response);

        long duration = System.currentTimeMillis() - startTime; // Calculate elapsed time

        // Log the method, request path, and HTTP status
        log.info("{} - {} - {}ms", method, fullPath, duration);
    }

    @Override
    public void destroy() {

    }
}