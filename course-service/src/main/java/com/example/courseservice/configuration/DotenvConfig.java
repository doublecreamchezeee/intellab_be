package com.example.courseservice.configuration;

import io.github.cdimascio.dotenv.Dotenv;

public class DotenvConfig {
    private static final Dotenv DOTENV = Dotenv.configure().load();

    public static String get(String key) {
        return DOTENV.get(key);
    }

}
