package com.example.identityservice.service;

import com.example.identityservice.client.FirebaseAuthClient;
import com.example.identityservice.client.GooglePeopleApiClient;
import com.example.identityservice.dto.request.profile.MultipleProfileInformationRequest;
import com.example.identityservice.dto.request.profile.SingleProfileInformationRequest;
import com.example.identityservice.dto.response.profile.MultipleProfileInformationResponse;
import com.example.identityservice.dto.response.profile.SingleProfileInformationResponse;
import com.google.firebase.auth.*;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileService {
    private final FirebaseAuth firebaseAuth;
    private final GooglePeopleApiClient googlePeopleApiClient;
    private final FirebaseAuthClient firebaseAuthClient;

    public SingleProfileInformationResponse getSingleProfileInformation(
            @NonNull final SingleProfileInformationRequest request
    ) {
        try {
            UserRecord userRecord = firebaseAuth.getUser(
                    request.getUserId()
            );

            return SingleProfileInformationResponse.builder()
                    .userId(userRecord.getUid())
                    .displayName(userRecord.getDisplayName())
                    .email(userRecord.getEmail())
                    .phoneNumber(userRecord.getPhoneNumber())
                    .photoUrl(userRecord.getPhotoUrl())
                    .build();
        } catch (FirebaseAuthException e) {
            throw new RuntimeException("Error finding user by uid: " + e.getMessage(), e);
        }
    }

    public String getProfilePictureUrlByEmail(String userId) {
        try {
            return googlePeopleApiClient.getProfilePictureUrlByEmail(userId);
        } catch (IOException e) {
            throw new RuntimeException("Error finding user by uid: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Error finding user by uid: " + e.getMessage(), e);
        }
    }

    public MultipleProfileInformationResponse getMultipleProfileInformation(
            @NonNull final MultipleProfileInformationRequest request
    ) {
        try {
            GetUsersResult usersResult = firebaseAuthClient.getUsersByIds(request.getUserIds());

            Set<UserRecord> setUserRecord = usersResult.getUsers();

            List<SingleProfileInformationResponse> listUserInformation = setUserRecord
                    .stream()
                    .map(
                            userRecord -> {
                                 return SingleProfileInformationResponse.builder()
                                        .userId(userRecord.getUid())
                                        .displayName(userRecord.getDisplayName())
                                        .email(userRecord.getEmail())
                                        .phoneNumber(userRecord.getPhoneNumber())
                                        .photoUrl(userRecord.getPhotoUrl())
                                        .build();
                            }
                    )
                    .toList();

            return MultipleProfileInformationResponse.builder()
                    .profiles(
                            listUserInformation
                    )
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Error finding users: " + e.getMessage(), e);
        }
    }


    /*public MultipleProfileInformationResponse getMultipleProfileInformation(
            @NonNull final MultipleProfileInformationRequest request
    ) {
        try {
            return MultipleProfileInformationResponse.builder()
                    .profiles(
                            request.getUserIds().stream()
                                    .map(userId -> {
                                        try {
                                            UserRecord userRecord = firebaseAuth.getUser(userId);

                                            return SingleProfileInformationResponse.builder()
                                                    .userId(userRecord.getUid())
                                                    .displayName(userRecord.getDisplayName())
                                                    .email(userRecord.getEmail())
                                                    .phoneNumber(userRecord.getPhoneNumber())
                                                    .photoUrl(userRecord.getPhotoUrl())
                                                    .build();
                                        } catch (FirebaseAuthException e) {
                                            throw new RuntimeException("Error finding user by uid: " + e.getMessage(), e);
                                        }
                                    })
                                    .toList()
                    )
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Error finding users: " + e.getMessage(), e);
        }
    }*/



}
