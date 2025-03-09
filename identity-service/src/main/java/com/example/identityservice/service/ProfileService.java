package com.example.identityservice.service;

import com.example.identityservice.client.CourseClient;
import com.example.identityservice.client.FirebaseAuthClient;
import com.example.identityservice.client.GooglePeopleApiClient;
import com.example.identityservice.client.ProblemClient;
import com.example.identityservice.dto.request.profile.MultipleProfileInformationRequest;
import com.example.identityservice.dto.response.auth.UserInfoResponse;
import com.example.identityservice.dto.response.course.CompleteCourseResponse;
import com.example.identityservice.dto.response.profile.MultipleProfileInformationResponse;
import com.example.identityservice.dto.response.profile.ProgressLanguageResponse;
import com.example.identityservice.dto.response.profile.ProgressLevelResponse;
import com.example.identityservice.dto.response.profile.SingleProfileInformationResponse;
import com.example.identityservice.exception.AppException;
import com.example.identityservice.exception.ErrorCode;
import com.example.identityservice.model.User;
import com.example.identityservice.utility.CloudinaryUtil;
import com.example.identityservice.utility.ParseUUID;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import com.google.firebase.auth.*;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileService {
    private final FirebaseAuth firebaseAuth;
    private final FirebaseAuthClient firebaseAuthClient;
    private final GooglePeopleApiClient googlePeopleApiClient;
    private final ProblemClient problemClient;
    private final CourseClient courseClient;
    private final CloudinaryService cloudinaryService;
    private final FirestoreService firestoreService;

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

    public UserInfoResponse getUserInfo(String userUid, String email) {
        UserInfoResponse userInfoResponse = firebaseAuthClient.getUserInfo(userUid, email);
        try {
            User userFirestore = firestoreService.getUserByUid(userUid);
            userInfoResponse.setFirstName(userFirestore.getFirstName());
            userInfoResponse.setLastName(userFirestore.getLastName());

            List<CompleteCourseResponse> completeCourseResponse = courseClient.getCourseByUserId().block().getResult();
            userInfoResponse.setCourseCount(completeCourseResponse.size());

            return userInfoResponse;
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
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

    public ProgressLevelResponse getProgressLevel() {
        return problemClient.getProgressLevel().block();
    }

    public ProgressLanguageResponse getProgressLanguage() {
        return problemClient.getProgressLanguage().block();
    }
}
