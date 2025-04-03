package com.example.identityservice.service;

import com.example.identityservice.client.FirebaseAuthClient;
import com.example.identityservice.dto.request.auth.UserCreationRequest;
import com.example.identityservice.dto.request.auth.UserLoginRequest;
import com.example.identityservice.dto.request.auth.UserUpdateRequest;
import com.example.identityservice.dto.response.auth.*;
import com.example.identityservice.enums.account.PremiumPackageStatus;
import com.example.identityservice.exception.AccountAlreadyExistsException;
import com.example.identityservice.exception.NotVerifiedEmailException;
import com.example.identityservice.exception.SendingEmailFailedException;
import com.example.identityservice.mapper.VNPayPaymentPremiumPackageMapper;
import com.example.identityservice.model.User;
import com.example.identityservice.model.VNPayPaymentPremiumPackage;
import com.example.identityservice.repository.VNPayPaymentPremiumPackageRepository;
import com.example.identityservice.specification.VNPayPaymentPremiumPackageSpecification;
import com.example.identityservice.utility.ParseUUID;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.auth.UserRecord;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final FirebaseAuth firebaseAuth;
    private final FirebaseAuthClient firebaseAuthClient;
    private final EmailService emailService;
    private final FirestoreService firestoreService;
    private final VNPayPaymentPremiumPackageRepository vnpayPaymentPremiumPackageRepository;
    private final VNPayPaymentPremiumPackageMapper vnpayPaymentPremiumPackageMapper;

    @SneakyThrows
    public void create(@NonNull final UserCreationRequest userCreationRequest) {
        log.info("Creating user with email: {}", userCreationRequest.getEmail());
        final var request = new UserRecord.CreateRequest()
                .setEmail(userCreationRequest.getEmail())
                .setPassword(userCreationRequest.getPassword())
                .setEmailVerified(Boolean.TRUE)
                .setDisplayName(userCreationRequest.getDisplayName())
                .setEmailVerified(Boolean.FALSE);

        try {
            UserRecord userRecord = firebaseAuth.createUser(request);
            log.info("User successfully created: {}", userCreationRequest.getEmail());

            firebaseAuth.generatePasswordResetLink(userRecord.getEmail());
            sendEmailVerification(userRecord.getUid());
            try{
                firestoreService.createUserByUid(userRecord.getUid(), "User");
            } catch (ExecutionException e) {
                log.error("Error creating user's firestore document: {}", e.getMessage(), e);
            }

        } catch (final FirebaseAuthException exception) {
            if (exception.getMessage().contains("EMAIL_EXISTS")) {
                throw new AccountAlreadyExistsException("Account with provided email already exists");
            }
            if (exception.getMessage().contains("PHONE_NUMBER_EXISTS")) {
                throw new AccountAlreadyExistsException("Account with provided phone number already exists");
            }
            throw new RuntimeException("Error creating user: " + exception.getMessage(), exception);
        }
    }

    public TokenSuccessResponse login(@NonNull final UserLoginRequest userLoginRequest) {
        boolean isEmailVerified = false;
        isEmailVerified = firebaseAuthClient.isEmailVerified(userLoginRequest.getEmail());
        if (!isEmailVerified) {
            throw new NotVerifiedEmailException();
        }
        return firebaseAuthClient.login(userLoginRequest);
    }

    public FirebaseGoogleSignInResponse loginWithGoogle(@NonNull final String idToken) throws FirebaseAuthException {
        ValidatedTokenResponse firebaseToken = firebaseAuthClient.verifyToken(idToken);

        try {
            User user = firestoreService.getUserByUid(firebaseToken.getUserId());

            if (user == null || user.getFirstName() == null || user.getLastName() == null) {
                firestoreService.createUserByUid(firebaseToken.getUserId(), "User");

                return FirebaseGoogleSignInResponse.builder()
                        .uid(firebaseToken.getUserId())
                        .email(firebaseToken.getEmail())
                        .build();
            }

        } catch (ExecutionException | InterruptedException e) {
            log.error("Error finding user by uid: {}", e.getMessage(), e);

        } catch (Exception e) {
            log.error("Error finding user by uid: {}", e.getMessage(), e);
        }

        return FirebaseGoogleSignInResponse.builder()
                .uid(firebaseToken.getUserId())
                .email(firebaseToken.getEmail())
                .build();
    }

    public RefreshTokenSuccessResponse refreshAccessToken(@NonNull final String refreshToken) {
        return firebaseAuthClient.refreshAccessToken(refreshToken);
    }

    public void updateByEmail(@NonNull String email, @NonNull UserUpdateRequest userUpdateRequest) {
        try {
            UserRecord userRecord = firebaseAuth.getUserByEmail(email);

            update(userRecord.getUid(), userUpdateRequest);
        } catch (FirebaseAuthException e) {
            throw new RuntimeException("Error finding user by email: " + e.getMessage(), e);
        }
    }

    public void update(@NonNull String uid, @NonNull UserUpdateRequest userUpdateRequest) {
        final var request = new UserRecord.UpdateRequest(uid);

        if (userUpdateRequest.getDisplayName() != null) {
            request.setDisplayName(userUpdateRequest.getDisplayName());
        }
        if (userUpdateRequest.getPassword() != null ) {
            request.setPassword(userUpdateRequest.getPassword());
        }
        try {
            firestoreService.updateUserByUid(uid, userUpdateRequest.getFirstName(), userUpdateRequest.getLastName());
            firebaseAuth.updateUser(request);
        } catch (final Exception exception) {
            throw new RuntimeException("Error updating user: " + exception.getMessage(), exception);
        }
    }

    public ValidatedTokenResponse validateToken(@NonNull final String token) {
        try {
            FirebaseToken decodeToken = FirebaseAuth.getInstance().verifyIdToken(token);
            String role = firestoreService.getRoleByUid(decodeToken.getUid());
            String premium = null;
            if (role.equals("user")) {
                //PremiumSubscription pre = firestoreService.getUserPremiumSubscriptionByUid(decodeToken.getUid());

                Specification<VNPayPaymentPremiumPackage> spec = Specification.where(VNPayPaymentPremiumPackageSpecification.hasUserUid(decodeToken.getUid()))
                        .and(VNPayPaymentPremiumPackageSpecification.hasStatus(PremiumPackageStatus.ACTIVE.getCode()));

                VNPayPaymentPremiumPackage pre =vnpayPaymentPremiumPackageRepository.findFirstByUserUidAndStatusOrderByEndDateDesc(
                                decodeToken.getUid(),
                                PremiumPackageStatus.ACTIVE.getCode()
                        )
                        .orElse(null);

                log.info("uid: {}",decodeToken.getUid());

                //vnpayPaymentPremiumPackageRepository.findByUserUid(decodeToken.getUid())

                if (pre != null)
                {
                    log.info("premium package: {}", pre.getUserUid());
                    premium = pre.getPackageType();
                    //premium = pre.getPlanType();
                }
                else
                {
                    premium = "free";
                }

            }

            return ValidatedTokenResponse.builder()
                    .isValidated(true)
                    .userId(decodeToken.getUid())
                    .name(decodeToken.getName())
                    .email(decodeToken.getEmail())
                    .role(role)
                    .premium(premium)
                    .message("Token validation successful.")
                    .build();
        } catch (FirebaseAuthException e) {
            return ValidatedTokenResponse.builder()
                    .isValidated(false)
                    .message("Invalid token: " + e.getMessage())
                    .build();
        } catch (Exception e) {
            return ValidatedTokenResponse.builder()
                    .isValidated(false)
                    .message("An unexpected error occurred: " + e.getMessage())
                    .build();
        }
    }

    public void sendEmailVerification(String uid) {
        try {
            UserRecord userRecord = firebaseAuth.getUser(uid);
            String email = userRecord.getEmail();

            if (email != null) {
                String link = firebaseAuthClient.generateEmailVerification(email);
                System.out.println("generate email to " + email + " with link: " + link);
                emailService.sendMail(
                        email,
                        "Verify your email in Intellab website",
                        "Congrats on sending your confirmation link: " + link);
            }
        } catch (Exception e) {
            throw new SendingEmailFailedException();
        }
    }

    public void sendPasswordResetLink(String email) {
        try {
            String link = firebaseAuthClient.generatePasswordResetLink(email);

            emailService.sendMail(
                    email,
                    "Reset your password in Intellab website",
                    "Congrats on sending your password reset link: " + link);
        } catch (Exception e) {
            throw new SendingEmailFailedException();
        }
    }

    public void setVerifiedListEmails(List<String> email) {
        try {
            firebaseAuthClient.setVerifiedListEmails(email);
        } catch (Exception e) {
            throw new RuntimeException("Error verifying email: " + e.getMessage(), e);
        }
    }

    public PremiumSubscriptionResponse getUserPremiumSubscriptionByUid(String uid) {
        VNPayPaymentPremiumPackage pre =vnpayPaymentPremiumPackageRepository.findFirstByUserUidAndStatusOrderByEndDateDesc(
                        uid,
                        PremiumPackageStatus.ACTIVE.getCode()
                )
                .orElse(null);

        log.info("uid: {}", uid);

        PremiumSubscriptionResponse response = null;
        if (pre != null)
        {
            log.info("premium package uuid: {}", pre.getUserUid());
            response = vnpayPaymentPremiumPackageMapper.toPremiumSubscriptionResponse(pre);
            return response;
        }
        else
        {
            response = PremiumSubscriptionResponse.builder()
                    .planType("free")
                    .userUid(uid)
                    .userUuid(
                            ParseUUID.normalizeUID(uid)
                    )
                    .build();
            return response;
        }

    }

}