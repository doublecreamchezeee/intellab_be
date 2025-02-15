package com.example.identityservice.client;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.people.v1.PeopleService;
import com.google.api.services.people.v1.PeopleServiceScopes;
import com.google.api.services.people.v1.model.Person;
import com.google.api.services.people.v1.model.Photo;
import com.google.api.services.people.v1.model.SearchResponse;
import com.google.api.services.people.v1.model.SearchResult;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.UserCredentials;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
public class GooglePeopleApiClient {
    private static final String APPLICATION_NAME = "Intellab";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    private static final NetHttpTransport HTTP_TRANSPORT;

    static {
        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Value("${google.credentials.file.path}")
    private String CREDENTIALS_FILE_PATH; // = "/credentials.json";

    private final PeopleService peopleService;

    public GooglePeopleApiClient(GoogleCredentials credentials) throws GeneralSecurityException, IOException {
        this.peopleService = new PeopleService.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JSON_FACTORY,
                new HttpCredentialsAdapter(credentials)
        ).setApplicationName(APPLICATION_NAME).build();
    }


    public String getProfilePictureUrl(String userId) throws IOException {
        Person profile = peopleService.people().get("people/" + userId)
                .setPersonFields("photos")
                .execute();

        if (profile.getPhotos() != null && !profile.getPhotos().isEmpty()) {
            Photo photo = profile.getPhotos().get(0);
            return photo.getUrl();
        }

        return null;
    }

    public String getProfilePictureUrlByEmail(String email) throws IOException, InterruptedException {
        /*// Warmup cache
        SearchResponse response = peopleService.people().searchContacts()
                .setQuery("")
                .setReadMask("names,emailAddresses")
                .execute();


        // Wait a few seconds
        Thread.sleep(5);*/

        Person profile = peopleService.people().searchContacts()
                .setQuery(email)//
                .setReadMask("names,emailAddresses")
                .execute()
                .getResults()
                .stream()
                .peek(per -> log.info("person: " + per))
                .findFirst()
                .map(SearchResult::getPerson)
                /*.map(person -> {
                    try {
                        return peopleService.people().get("people/" + person.getResourceName())
                                .setPersonFields("photos")
                                .execute();
                    } catch (IOException e) {
                        log.error("Error getting profile picture url by email: " + e.getMessage(), e);
                        return null;
                    }
                })*/
                .orElse(null);

        if (profile != null && profile.getPhotos() != null && !profile.getPhotos().isEmpty()) {
            Photo photo = profile.getPhotos().get(0);
            return photo.getUrl();
        }

        return null;
    }

    /*private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT)
            throws IOException {
        // Load client secrets.
        InputStream in = GooglePeopleApiClient.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets =
                GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }*/
}
