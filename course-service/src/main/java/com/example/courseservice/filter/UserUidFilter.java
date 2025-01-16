package com.example.courseservice.filter;

import com.example.courseservice.utils.ParseUUID;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class UserUidFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Initialization logic if needed
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String userUid = httpRequest.getHeader("X-UserId");
        if (userUid != null) {
            httpRequest.setAttribute("X-UserId", userUid);
        }

        chain.doFilter(httpRequest, httpResponse);
    }

    @Override
    public void destroy() {
        // Cleanup logic if needed
    }
}
