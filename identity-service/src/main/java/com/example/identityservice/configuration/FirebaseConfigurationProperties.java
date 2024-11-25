package com.example.identityservice.configuration;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "com.example")
public class FirebaseConfigurationProperties {

    @Valid
    private FireBase firebase = new FireBase();
    @Getter
    @Setter
    public static class FireBase {
        @NotBlank(message = "Firestore private key must be configured")
        private String privateKey;

        @NotBlank(message = "Firebase Web API key must be configured")
        private String webApiKey;
    }

}