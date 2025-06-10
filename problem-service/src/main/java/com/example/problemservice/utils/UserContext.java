package com.example.problemservice.utils;


import org.springframework.stereotype.Component;

@Component
public class UserContext {
    private static final ThreadLocal<String> currentUser = new ThreadLocal<>();

    public static String getCurrentUser() {
        return currentUser.get();
    }

    public static void setCurrentUser(String userId) {
        currentUser.set(userId);
    }

    public static void clear() {
        currentUser.remove();
    }
}
