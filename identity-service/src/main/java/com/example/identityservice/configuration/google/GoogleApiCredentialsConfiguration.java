package com.example.identityservice.configuration.google;

import com.google.api.services.people.v1.PeopleServiceScopes;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.UserCredentials;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.validation.annotation.Validated;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

@Configuration
@RequiredArgsConstructor
@Validated
public class GoogleApiCredentialsConfiguration {
    @Value("${google.credentials.file.path}")
    private String credentialsFilePath;

    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    private static final List<String> SCOPES = Arrays.asList(
            PeopleServiceScopes.CONTACTS_READONLY,
            PeopleServiceScopes.USERINFO_PROFILE,
            PeopleServiceScopes.USERINFO_EMAIL
    );

    /*,PeopleServiceScopes.USER_EMAILS_READ,
            PeopleServiceScopes.USER_PHONENUMBERS_READ,
            PeopleServiceScopes.USER_BIRTHDAY_READ,
            PeopleServiceScopes.CONTACTS*/

    /*@Bean
    public UserCredentials userCredentials() throws IOException {
        //new FileInputStream(credentialsFilePath)
        InputStream inputStream = new ClassPathResource(credentialsFilePath).getInputStream();

        return UserCredentials.fromStream(inputStream);
    }*/

    @Bean
    public GoogleCredentials googleCredentials() throws IOException {
        InputStream inputStream = new ClassPathResource(credentialsFilePath).getInputStream();

        return GoogleCredentials.fromStream(inputStream)
                .createScoped(SCOPES);
    }
}
