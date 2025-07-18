package com.example.problemservice.interceptor;


import com.example.problemservice.utils.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@AllArgsConstructor
public class UserHeaderInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request,
                             @NotNull HttpServletResponse response,
                             @NotNull Object handler) {
        String userId = request.getHeader("X-UserId");

        String userRole = request.getHeader("X-UserRole");

        System.out.println(userId);
        if (userId == null || userId.isEmpty() || userRole == null || userRole.isEmpty()) {
            System.out.println("No UserId found in the request header");
            return true; // Continue processing if no UserId is present
        }

        try {
            userId = userId.split(",")[0];
            userRole = userRole.split(",")[0];
            System.out.println("User Role: " + userRole);


            if (userId != null) {
                UserContext.setCurrentUser(userId + "," + userRole);
            }
            return true;
        } catch (Exception e) {
            System.out.println("Error processing UserId or UserRole: " + e.getMessage());
            return true; // Continue processing even if there's an error
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler,
                                Exception ex) {
        UserContext.clear();
    }
}
