package com.example.identityservice.client;

import com.example.identityservice.annotation.ExecutionTiming;
import com.example.identityservice.configuration.firebase.FirebaseConfigurationProperties;
import com.example.identityservice.dto.request.auth.FirebaseSignInRequest;
import com.example.identityservice.dto.request.auth.UserLoginRequest;
import com.example.identityservice.dto.response.admin.AdminUserResponse;
import com.example.identityservice.dto.response.auth.*;
import com.example.identityservice.enums.account.PremiumPackageStatus;
import com.example.identityservice.exception.AppException;
import com.example.identityservice.exception.ErrorCode;
import com.example.identityservice.exception.FirebaseAuthenticationException;
import com.example.identityservice.exception.InvalidLoginCredentialsException;
import com.example.identityservice.mapper.UserMapper;
import com.example.identityservice.model.User;
import com.example.identityservice.model.VNPayPaymentPremiumPackage;
import com.example.identityservice.repository.VNPayPaymentPremiumPackageRepository;
import com.example.identityservice.service.FirestoreService;
import com.example.identityservice.specification.VNPayPaymentPremiumPackageSpecification;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.auth.*;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.*;
import java.util.concurrent.ExecutionException;

@Component
@RequiredArgsConstructor
@EnableConfigurationProperties(FirebaseConfigurationProperties.class)
@Slf4j
public class FirebaseAuthClient {

    private final FirebaseConfigurationProperties firebaseConfigurationProperties;

    private final Firestore firestore;

    private final VNPayPaymentPremiumPackageRepository vnpayPaymentPremiumPackageRepository;

    private static final String BASE_URL = "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword";
    private static final String API_KEY_PARAM = "key";
    private static final String INVALID_CREDENTIALS_ERROR = "INVALID_LOGIN_CREDENTIALS";
    private static final String REFRESH_TOKEN_URL = "https://securetoken.googleapis.com/v1/token";
    private final FirebaseAuth firebaseAuth;
    private final FirestoreService firestoreService;

    @Value("${domain.auth.reset-password-callback-url}")
    private String RESET_PASSWORD_CALLBACK_URL;

    @Value("${domain.auth.email-verification-callback-url}")
    private String EMAIL_VERIFICATION_CALLBACK_URL;

    public TokenSuccessResponse login(@NonNull final UserLoginRequest userLoginRequest) {
        final var requestBody = prepareRequestBody(userLoginRequest);
        final var response = sendSignInRequest(requestBody);
        return TokenSuccessResponse.builder()
                .accessToken(response.getIdToken())
                .refreshToken(response.getRefreshToken())
                .build();
    }

    public String getUid(UUID userUUID) throws ExecutionException, InterruptedException {
        DocumentSnapshot document = firestore.collection("users").document(userUUID.toString()).get().get();
        if (document.exists()) {
            return document.toObject(User.class).getUid();
        }
        return null;
    }

    public ValidatedTokenResponse verifyToken(@NonNull final String token) {
        try {
            FirebaseToken decodeToken = firebaseAuth.verifyIdToken(token);
            String userId = decodeToken.getUid(); // Correct way to get user ID

            log.info("Decoded token userId: {} - email: {}", userId, decodeToken.getEmail());
//            String role = (String) decodeToken.getClaims().getOrDefault("role", "USER"); // Default to "USER" if missing
            String role = firestoreService.getRoleByUid(userId);

            if (role == null) {
//                firestoreService.createUserByUid(userId, "User");
                System.out.println("(FirebaseAuth-verifyToken) Role is null for userId: " + userId);
                role = "user"; // Default role if not found
            }

            String premium = null;

            if (role.equals("user")) {
                //PremiumSubscription pre = firestoreService.getUserPremiumSubscriptionByUid(decodeToken.getUid());
                /*VNPayPaymentPremiumPackage pre = vnpayPaymentPremiumPackageRepository.findByUserUid(userId)
                        .orElse(null);*/

                Specification<VNPayPaymentPremiumPackage> spec = Specification.where(VNPayPaymentPremiumPackageSpecification.hasUserUid(decodeToken.getUid()))
                        .and(VNPayPaymentPremiumPackageSpecification.hasStatus(PremiumPackageStatus.ACTIVE.getCode()));

                VNPayPaymentPremiumPackage pre = vnpayPaymentPremiumPackageRepository.findFirstByUserUidAndStatusOrderByEndDateDesc(
                                    decodeToken.getUid(),
                                    PremiumPackageStatus.ACTIVE.getCode()
                                )
                        .orElse(null);

                if (pre != null) {
                    premium = pre.getPackageType();
                    //premium = pre.getPlanType();
                }
                else {
                    premium = "free";
                }
            }
            String email = decodeToken.getEmail(); // Get email if available

            log.info("Decoded token for userId: {}, role: {}, premium: {}, email: {}", userId, role, premium, email);

            return ValidatedTokenResponse.builder()
                    .userId(userId)
                    .role(role)
                    .premium(premium)
                    .email(email) // Ensure email is included
                    .isValidated(true)
                    .message("Token validation successful.")
                    .isEmailVerified(decodeToken.isEmailVerified())
                    .build();
        } catch (FirebaseAuthException e) {
            log.error("FirebaseAuthException while verifying token: {}", e.getMessage());
            return ValidatedTokenResponse.builder()
                    .isValidated(false)
                    .message("Invalid token: " + e.getMessage())
                    .build();
        } catch (Exception e) {
            log.error("Unexpected error while verifying token: {}", e.getMessage());
            return ValidatedTokenResponse.builder()
                    .isValidated(false)
                    .message("An unexpected error occurred: " + e.getMessage())
                    .build();
        }
    }

    private FirebaseSignInResponse sendSignInRequest(@NonNull final FirebaseSignInRequest request) {
        final var webApiKey = firebaseConfigurationProperties.getFirebase().getWebApiKey();
        try {
            return RestClient.create(BASE_URL)
                    .post()
                    .uri(uriBuilder -> uriBuilder
                            .queryParam(API_KEY_PARAM, webApiKey)
                            .build())
                    .body(request)
                    .contentType(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(FirebaseSignInResponse.class);
        } catch (HttpClientErrorException exception) {
            if (exception.getResponseBodyAsString().contains(INVALID_CREDENTIALS_ERROR)) {
                throw new InvalidLoginCredentialsException("Failed to refresh token.", exception);
            }
            throw exception;
        }
    }

    private FirebaseSignInRequest prepareRequestBody(@NonNull final UserLoginRequest userLoginRequest) {
        final var request = new FirebaseSignInRequest();
        request.setEmail(userLoginRequest.getEmail());
        request.setPassword(userLoginRequest.getPassword());
        request.setReturnSecureToken(Boolean.TRUE);
        return request;
    }

    public RefreshTokenSuccessResponse refreshAccessToken(@NonNull final String refreshToken) {
        final var webApiKey = firebaseConfigurationProperties.getFirebase().getWebApiKey();
        final var requestBody = Map.of(
                "grant_type", "refresh_token",
                "refresh_token", refreshToken
        );

        System.out.println("Sending request with refreshToken: " + refreshToken);

        try {
            final var response = RestClient.create(REFRESH_TOKEN_URL)
                    .post()
                    .uri(uriBuilder -> uriBuilder.queryParam("key", webApiKey).build())
                    .body(requestBody)
                    .contentType(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(RefreshTokenSuccessResponse.class);


            return RefreshTokenSuccessResponse.builder()
                    .id_token(Objects.requireNonNull(response).getId_token())
                    .refresh_token(response.getRefresh_token())
                    .expires_in(response.getExpires_in())
                    .build();
        } catch (HttpClientErrorException exception) {
            System.err.println("Error refreshing token: " + exception.getResponseBodyAsString());
            throw new InvalidLoginCredentialsException("Failed to refresh token.", exception);
        }
    }

    public String generateEmailVerification(@NonNull final String email) {
        try {
            ActionCodeSettings actionCodeSettings = ActionCodeSettings.builder()
                    .setUrl(EMAIL_VERIFICATION_CALLBACK_URL)
                    .setHandleCodeInApp(true)
                    .setIosBundleId("com.example.ios")
                    .setAndroidPackageName("com.example.android")
                    .setAndroidInstallApp(true)
                    .setAndroidMinimumVersion("12")
                    .setDynamicLinkDomain("example.page.link")
                    .build();

            return firebaseAuth.generateEmailVerificationLink(email);
        } catch (Exception e) {
            throw new FirebaseAuthenticationException();
        }
    }

    public String generatePasswordResetLink(@NonNull final String email) {
        try {
            ActionCodeSettings actionCodeSettings = ActionCodeSettings.builder()
                    .setUrl(RESET_PASSWORD_CALLBACK_URL)
                    .setHandleCodeInApp(true)
                    .setIosBundleId("com.example.ios")
                    .setAndroidPackageName("com.example.android")
                    .setAndroidInstallApp(true)
                    .setAndroidMinimumVersion("12")
                    .setDynamicLinkDomain("https://example.page.link/summer-sale")
                    .build();

            return firebaseAuth.generatePasswordResetLink(email); //, actionCodeSettings);
        } catch (Exception e) {
            throw new FirebaseAuthenticationException();
        }
    }

    public void updateUserProfilePicture(String uid, String photoUrl) throws FirebaseAuthException {
        UserRecord.UpdateRequest request = new UserRecord.UpdateRequest(uid)
                .setPhotoUrl(photoUrl);

        firebaseAuth.updateUser(request);
    }

    public Boolean isEmailVerified(String email) {
        try {
            UserRecord userRecord = firebaseAuth.getUserByEmail(email);
            //UserRecord userRecord = firebaseAuth.getUser(uid);
            return userRecord.isEmailVerified();
        } catch (FirebaseAuthException e) {
            throw new FirebaseAuthenticationException();
        }
    }

    public void setVerifiedListEmails(List<String> emails) {
        for (String email : emails) {
            try {
                UserRecord userRecord = firebaseAuth.getUserByEmail(email);

                UserRecord.UpdateRequest request = new UserRecord.UpdateRequest(userRecord.getUid())
                        .setEmailVerified(true);
                firebaseAuth.updateUser(request);
            } catch (FirebaseAuthException e) {
                throw new FirebaseAuthenticationException();
            }
        }
    }

    public void setVerifiedEmail(String email) {
        try {
            UserRecord userRecord = firebaseAuth.getUserByEmail(email);

            UserRecord.UpdateRequest request = new UserRecord.UpdateRequest(userRecord.getUid())
                    .setEmailVerified(true);

            firebaseAuth.updateUser(request);

        } catch (FirebaseAuthException e) {
            throw new FirebaseAuthenticationException();
        }
    }

    public void setUnverifiedListEmails(List<String> emails) {
        for (String email : emails) {
            try {
                UserRecord userRecord = firebaseAuth.getUserByEmail(email);

                UserRecord.UpdateRequest request = new UserRecord.UpdateRequest(userRecord.getUid())
                        .setEmailVerified(false);
                firebaseAuth.updateUser(request);
            } catch (FirebaseAuthException e) {
                throw new FirebaseAuthenticationException();
            }
        }
    }

    public UserInfoResponse getUserInfo(String userId, String email) {
        try {
            UserRecord userRecord;
            if (userId != null) {
                userRecord = firebaseAuth.getUser(userId);
            } else if (email != null) {
                userRecord = firebaseAuth.getUserByEmail(email);
            } else {
                throw new IllegalArgumentException("Either userId or email must be provided.");
            }
            return UserMapper.INSTANCE.fromRecordToResponse(userRecord);

        } catch (FirebaseAuthException e) {
            throw new RuntimeException("Error retrieving user information: " + e.getMessage(), e);
        }
    }

    public GetUsersResult getUsersByIds(List<String> userIds) {
        try {
            // Convert userIds into Firebase UidIdentifiers
            List<UserIdentifier> identifiers = userIds.stream()
                    .map(id -> (UserIdentifier) new UidIdentifier(id))  // Use EmailIdentifier(email) if querying by email
                    .toList();

            return firebaseAuth.getUsers(identifiers);
        } catch (FirebaseAuthException e) {
            throw new RuntimeException("Error finding users by ids: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Error finding users by ids: " + e.getMessage(), e);
        }
    }

    public Boolean isEmailVerifiedByUserUid(String userUid) {
        try {
            UserRecord userRecord = firebaseAuth.getUser(
                    userUid
            );
            return userRecord.isEmailVerified();
        } catch (FirebaseAuthException e) {
            throw new RuntimeException("Error finding user by uid: " + e.getMessage(), e);
        }
    }

    public Boolean updatePasswordByUserUid(String userUid, String password) {
        try {
            UserRecord.UpdateRequest request = new UserRecord.UpdateRequest(userUid)
                    .setPassword(password);
            firebaseAuth.updateUser(request);
            return true;
        } catch (FirebaseAuthException e) {
            throw new RuntimeException("Error finding user by uid: " + e.getMessage(), e);
        }
    }

    @ExecutionTiming
    public void getAllListUsers(Integer size, String pageIndex) {
        try {
            int countTotalUsers = 0;
            List<AdminUserResponse> adminUserResponses = new ArrayList<>();
            ListUsersPage userPage = firebaseAuth.listUsers(null);

            // Iterate through all users
            while (userPage != null) {
                for (ExportedUserRecord user : userPage.getValues()) {
                    System.out.println("User UID: " + user.getUid());
                    System.out.println("Email: " + user.getEmail());
                    // Add more user details as needed
                    countTotalUsers++;


                    /*if (countTotalUsers < size && countTotalUsers > Integer.parseInt(pageIndex)) {
                        adminUserResponses.add(UserMapper.INSTANCE.fromRecordToResponse(user));
                    }*/
                }
                userPage = userPage.getNextPage();
            }
            log.info("Total users: {}", countTotalUsers);



        } catch (FirebaseAuthException e) {
            throw new AppException(ErrorCode.ERROR_WHEN_RETRIEVING_USER_FROM_FIREBASE_AUTHENTICATION);
        }
    }

    public Page<AdminUserResponse> getAllUsers2(Integer size, Integer pageIndex) {
        try {
            List<AdminUserResponse> allUsers = new ArrayList<>();
            ListUsersPage userPage = firebaseAuth.listUsers(null);

            // Collect all users
            while (userPage != null) {
                for (ExportedUserRecord user : userPage.getValues()) {
                    allUsers.add(UserMapper.INSTANCE.fromRecordToResponse(user));
                }
                userPage = userPage.getNextPage();
            }

            // Pagination logic
            int start = pageIndex * size;
            int end = Math.min(start + size, allUsers.size());
            if (start > allUsers.size()) {
                return new PageImpl<>(Collections.emptyList(), PageRequest.of(pageIndex, size), allUsers.size());
            }

            List<AdminUserResponse> paginatedUsers = allUsers.subList(start, end);
            return new PageImpl<>(paginatedUsers, PageRequest.of(pageIndex, size), allUsers.size());
        } catch (FirebaseAuthException e) {
            throw new AppException(ErrorCode.ERROR_WHEN_RETRIEVING_USER_FROM_FIREBASE_AUTHENTICATION);
        }
    }

    public Page<AdminUserResponse> getAllUsers(Integer pageSize, Integer pageNumber) {
        try {
            List<AdminUserResponse> paginatedUsers = new ArrayList<>();
            int start = pageNumber * pageSize;
            int end = start + pageSize;
            int currentIndex = 0;

            ListUsersPage userPage = firebaseAuth.listUsers(null);

            // Collect only the necessary users for the requested page
            while (userPage != null ) { //&& currentIndex < end
                for (ExportedUserRecord user : userPage.getValues()) {
                    if (currentIndex >= start && currentIndex < end) {
                        paginatedUsers.add(UserMapper.INSTANCE.fromRecordToResponse(user));
                    }
                    currentIndex++;
                    /*if (currentIndex >= end) {
                        break;
                    }*/
                }
                userPage = userPage.getNextPage();
            }

            return new PageImpl<>(paginatedUsers, PageRequest.of(pageNumber, pageSize), currentIndex);
        } catch (FirebaseAuthException e) {
            throw new AppException(ErrorCode.ERROR_WHEN_RETRIEVING_USER_FROM_FIREBASE_AUTHENTICATION);
        }
    }

    public List<ExportedUserRecord> findUsersByDisplayName(String targetDisplayName) {
        try {
            List<ExportedUserRecord> matchedUsers = new ArrayList<>();
            ListUsersPage page = firebaseAuth.listUsers(null);

            targetDisplayName = targetDisplayName.toLowerCase();

            while (page != null) {
                for (ExportedUserRecord user : page.getValues()) {
                    if (user.getDisplayName().toLowerCase().contains(targetDisplayName)) {
                        matchedUsers.add(user);
                    }
                }
                page = page.getNextPage();
            }
            return matchedUsers;
        } catch (FirebaseAuthException e) {
            throw new AppException(ErrorCode.ERROR_WHEN_RETRIEVING_USER_FROM_FIREBASE_AUTHENTICATION);
        }
    }

    public Page<AdminUserResponse> getAllUsersByDisplayName(String targetDisplayName, Integer pageSize, Integer pageNumber) {
        try {
            List<ExportedUserRecord> matchedUsers = findUsersByDisplayName(targetDisplayName);

            int start = pageNumber * pageSize;
            int end = Math.min(start + pageSize, matchedUsers.size());

            if (start > matchedUsers.size()) {
                return new PageImpl<>(Collections.emptyList(), PageRequest.of(pageNumber, pageSize), matchedUsers.size());
            }

            List<AdminUserResponse> paginatedUsers = matchedUsers.subList(start, end).stream()
                    .map(UserMapper.INSTANCE::fromRecordToResponse)
                    .toList();

            return new PageImpl<>(paginatedUsers, PageRequest.of(pageNumber, pageSize), matchedUsers.size());
        } catch (Exception e) {
            throw new AppException(ErrorCode.ERROR_WHEN_RETRIEVING_USER_FROM_FIREBASE_AUTHENTICATION);
        }
    }

    public boolean checkIfUserExistsByEmail(String email) {
        try {
            UserRecord userRecord = firebaseAuth.getUserByEmail(email);
            return userRecord != null;
        } catch (FirebaseAuthException e) {
            log.error("Error checking if user exists by email: {}", e.getMessage());
            return false;
        }
    }
}