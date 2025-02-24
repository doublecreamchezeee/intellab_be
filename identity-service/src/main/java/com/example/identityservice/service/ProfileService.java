package com.example.identityservice.service;

import com.example.identityservice.client.FirebaseAuthClient;
import com.example.identityservice.client.GooglePeopleApiClient;
import com.example.identityservice.client.ProblemClient;
import com.example.identityservice.dto.request.auth.UserUpdateRequest;
import com.example.identityservice.dto.request.profile.MultipleProfileInformationRequest;
import com.example.identityservice.dto.request.profile.SingleProfileInformationRequest;
import com.example.identityservice.dto.response.auth.UserInfoResponse;
import com.example.identityservice.dto.response.profile.MultipleProfileInformationResponse;
import com.example.identityservice.dto.response.profile.ProgressResponse;
import com.example.identityservice.dto.response.profile.SingleProfileInformationResponse;
import com.example.identityservice.exception.AppException;
import com.example.identityservice.exception.ErrorCode;
import com.example.identityservice.utility.CloudinaryUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileService {
    private final FirebaseAuth firebaseAuth;
    private final FirebaseAuthClient firebaseAuthClient;
    private final GooglePeopleApiClient googlePeopleApiClient;
    private final ProblemClient problemClient;
    private final CloudinaryService cloudinaryService;

    public SingleProfileInformationResponse getSingleProfileInformation(
            @NonNull String userId
    ) {
        try {
            UserRecord userRecord = firebaseAuth.getUser(
                    userId
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

    public MultipleProfileInformationResponse getMultipleProfileInformation(
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
    }

    public UserInfoResponse getUserInfo(String userId, String email) {
        return firebaseAuthClient.getUserInfo(userId, email);
    }

    public void uploadProfilePicture(String userId, MultipartFile file) throws FirebaseAuthException {
        final SingleProfileInformationResponse user = getSingleProfileInformation(userId);
        if (user == null) {
            throw new AppException(ErrorCode.USER_NOT_EXISTED);
        }
        String oldPhotoUrl = user.getPhotoUrl();
        String oldPublicId = CloudinaryUtil.extractPublicIdFromUrl(oldPhotoUrl);
        if (oldPublicId != null) {
            cloudinaryService.deleteImage(oldPublicId);
        }
        String newPhotoUrl = cloudinaryService.uploadImage(file);
        if (newPhotoUrl == null) {
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
        firebaseAuthClient.updateUserProfilePicture(userId, newPhotoUrl);

    }

    public String getProfilePictureUrlByEmail(String userId) {
        try {
            return googlePeopleApiClient.getProfilePictureUrl(userId);
        } catch (IOException e) {
            throw new RuntimeException("Error finding user by uid: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Error finding user by uid: " + e.getMessage(), e);
        }
    }

    public ProgressResponse getProgress() {
        return problemClient.getProgress().block();
    }
}
