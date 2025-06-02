package com.example.courseservice.interceptor;


import com.example.courseservice.utils.UserContext;
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
        if (userId == null || userId.isEmpty()) {
            System.out.println("No UserId found in the request header");
            return true; // Continue processing if no UserId is present
        }
        userId = userId.split(",")[0];
        if ( userRole == null || userRole.isEmpty()) {
            System.out.println("No UserRole found in the request header");
            userRole = "ambiguous role"; // Default role if not provided
        }
        userRole = userRole.split(",")[0];
        System.out.println("User Role: " + userRole);


        if (userId != null) {


            UserContext.setCurrentUser(userId + "," + userRole);
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler,
                                Exception ex) {
        UserContext.clear();
    }
}
