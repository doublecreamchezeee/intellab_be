package com.example.identityservice.service;

import com.example.identityservice.client.FirebaseAuthClient;
import com.example.identityservice.configuration.RedirectUrlConfig;
import com.example.identityservice.dto.request.auth.ResetPasswordRequest;
import com.example.identityservice.dto.request.auth.UserCreationRequest;
import com.example.identityservice.dto.request.auth.UserLoginRequest;
import com.example.identityservice.dto.request.auth.UserUpdateRequest;
import com.example.identityservice.dto.response.auth.*;
import com.example.identityservice.enums.account.PremiumPackageStatus;
import com.example.identityservice.exception.AccountAlreadyExistsException;
import com.example.identityservice.exception.AppException;
import com.example.identityservice.exception.ErrorCode;
import com.example.identityservice.exception.SendingEmailFailedException;
import com.example.identityservice.mapper.VNPayPaymentPremiumPackageMapper;
import com.example.identityservice.model.User;
import com.example.identityservice.model.VNPayPaymentPremiumPackage;
import com.example.identityservice.repository.VNPayPaymentPremiumPackageRepository;
import com.example.identityservice.specification.VNPayPaymentPremiumPackageSpecification;
import com.example.identityservice.utility.JwtUtil;
import com.example.identityservice.utility.ParseUUID;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.auth.UserRecord;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.Date;
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
    private final RedirectUrlConfig redirectUrlConfig;
    private final JwtUtil jwtUtil;

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

            int timeout = 20; // seconds
            while (true) {
                if (timeout-- <= 0) {
                    log.error("Timeout while creating user's firestore document");
                    throw new RuntimeException("Timeout while creating user's firestore document");
                }
                try {
                    firestoreService.createUserByUid(userRecord.getUid(), "User");
                    break;
                } catch (ExecutionException e) {
                    log.error("Error creating user's firestore document: {}", e.getMessage(), e);
                }
            }

            sendVerificationEmail(userRecord.getUid());

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
       /* boolean isEmailVerified = false;
        isEmailVerified = firebaseAuthClient.isEmailVerified(userLoginRequest.getEmail());
        if (!isEmailVerified) {
            throw new NotVerifiedEmailException();
        }*/

        try {
            TokenSuccessResponse response = firebaseAuthClient.login(userLoginRequest);

            // get role of user by email
            UserInfoResponse user = firebaseAuthClient.getUserInfo(null, userLoginRequest.getEmail());
            String role = firestoreService.getRoleByUid(user.getUserId());

            response.setUserRole(role);

            return response;
        } catch (AppException e) {
            log.error(e.getMessage());
            throw new AppException(ErrorCode.ERROR_WHEN_LOGIN);
        }
    }

    public FirebaseGoogleSignUpResponse signUpWithGoogle(@NonNull final String idToken) throws FirebaseAuthException {

        FirebaseToken decodeToken = firebaseAuth.verifyIdToken(idToken);

        if (decodeToken == null || decodeToken.getUid() == null) {
            throw new AppException(ErrorCode.ERROR_WHEN_SIGNUP_WITH_GOOGLE);

            /*return FirebaseGoogleSignUpResponse.builder()
                    .uid(null)
                    .email(null)
                    .isSignUpSuccessful(false)
                    .message("Invalid ID token.")
                    .build();*/
        }

        try {
            User user = firestoreService.getUserByUid(decodeToken.getUid());

            if (user == null) {
                System.out.println("Write to firestore User with uid: " + decodeToken.getUid());
                firestoreService.createUserByUid(decodeToken.getUid(), "User");
            }
            else if (user.getFirstName() == null || user.getLastName() == null)
            {
                System.out.println("Update firestore User with uid: " + decodeToken.getUid());
                firestoreService.updateUserByUid(decodeToken.getUid(), "User", decodeToken.getUid());
            } else {
                log.info("User with uid {} already exists in Firestore.", decodeToken.getUid());
            }


            return FirebaseGoogleSignUpResponse.builder()
                    .uid(decodeToken.getUid())
                    .email(decodeToken.getEmail())
                    .isSignUpSuccessful(true)
                    .message("User sign up successfully.")
                    .build();

        } catch (ExecutionException | InterruptedException e) {
            log.error("Error finding user by uid: {}", e.getMessage(), e);
        }

        return FirebaseGoogleSignUpResponse.builder()
                .uid(decodeToken.getUid())
                .email(decodeToken.getEmail())
                .isSignUpSuccessful(true)
                .message("User sign up successfully! However, there was a minor error creating user information in firestore.")
                .build();

    }

    /**
     * Login with Google using Firebase Authentication.
     *
     * @param idToken The ID token from Google Sign-In.
     * @return A response containing user information and login status.
     * @throws FirebaseAuthException If there is an error during Firebase authentication.
     */

    public FirebaseGoogleSignInResponse loginWithGoogle(@NonNull final String idToken) throws FirebaseAuthException {
        /*FirebaseToken decodeToken = firebaseAuth.verifyIdToken(idToken);
        boolean isUseExisted = firebaseAuthClient.checkIfUserExistsByEmail(decodeToken.getEmail());
        log.info("User with email {} exists: {}", decodeToken.getEmail(), isUseExisted);
        log.info("User with uid {} exists: {}", decodeToken.getUid(), isUseExisted);

        if (!isUseExisted) {
            throw new AppException(ErrorCode.USER_WITH_EMAIL_NOT_EXISTED);
            *//*return FirebaseGoogleSignInResponse.builder()
                    .uid(null)
                    .email(null)
                    .isLoginSuccessful(false)
                    .message("User with this email does not exist. Please register first.")
                    .build();*//*
        }*/

        ValidatedTokenResponse firebaseToken = firebaseAuthClient.verifyToken(idToken);

        try {
            User user = firestoreService.getUserByUid(firebaseToken.getUserId());

            if (user == null || user.getFirstName() == null || user.getLastName() == null) {
                firestoreService.createUserByUid(firebaseToken.getUserId(), "User");

                return FirebaseGoogleSignInResponse.builder()
                        .uid(firebaseToken.getUserId())
                        .email(firebaseToken.getEmail())
                        .isLoginSuccessful(true)
                        .message("User logins successfully.")
                        .build();
            }

        } catch (ExecutionException | InterruptedException e) {
            log.error("Error finding user by uid (ExecutionException): {}", e.getMessage(), e);

        } catch (Exception e) {
            log.error("Error finding user by uid: {}", e.getMessage(), e);
        }

        return FirebaseGoogleSignInResponse.builder()
                .uid(firebaseToken.getUserId())
                .email(firebaseToken.getEmail())
                .isLoginSuccessful(true)
                .message("User logins successfully.")
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
            FirebaseToken decodeToken = firebaseAuth.verifyIdToken(token);
            String role = firestoreService.getRoleByUid(decodeToken.getUid());
            if (role == null) {
                System.out.println("Role not found for user with uid: " + decodeToken.getUid());
                role = "user"; // Default role if not found
            }
            String premium = null;
            if (role.equals("user")) {
                //PremiumSubscription pre = firestoreService.getUserPremiumSubscriptionByUid(decodeToken.getUid());

                Specification<VNPayPaymentPremiumPackage> spec = Specification.where(VNPayPaymentPremiumPackageSpecification.hasUserUid(decodeToken.getUid()))
                        .and(VNPayPaymentPremiumPackageSpecification.hasStatus(PremiumPackageStatus.ACTIVE.getCode()));

                VNPayPaymentPremiumPackage pre = vnpayPaymentPremiumPackageRepository.findFirstByUserUidAndStatusOrderByEndDateDesc(
                                decodeToken.getUid(),
                                PremiumPackageStatus.ACTIVE.getCode()
                        )
                        .orElse(null);

                //log.info("uid: {}",decodeToken.getUid());

                //vnpayPaymentPremiumPackageRepository.findByUserUid(decodeToken.getUid())

                if (pre != null)
                {
                    //log.info("premium package: {}", pre.getUserUid());
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
                    .isEmailVerified(decodeToken.isEmailVerified())
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

    public void resendVerificationEmail(@NonNull String email) {
        try {
            UserRecord userRecord = firebaseAuth.getUserByEmail(email);
            String uid = userRecord.getUid();
            sendVerificationEmail(uid);
        } catch (FirebaseAuthException e) {
            throw new RuntimeException("Error finding user by email: " + e.getMessage(), e);
        }
    }

    public void sendVerificationEmail(String uid) {
        try {
            UserRecord userRecord = firebaseAuth.getUser(uid);
            String email = userRecord.getEmail();

            if (email != null) {
                //String link = firebaseAuthClient.generateEmailVerification(email);

                String link = generateCustomVerificationLink(email);
                System.out.println("generate email to " + email + " with link: " + link);

                //String htmlContent = "<a href=\"" + link + "\" target=\"_blank\" style=\"color: blue; text-decoration: underline;\">Click here to verify your email</a>";

                User userFirestore = firestoreService.getUserByUid(userRecord.getUid());

                String name = userRecord.getDisplayName();

                if (userFirestore != null) {
                    name = userFirestore.getFirstName() + " " + userFirestore.getLastName();
                }



                String feUrl = redirectUrlConfig.getFeUrl();

                String htmlContent = String.format("""
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Verify email</title>
</head>
<body style="font-family: Arial, sans-serif; background-color: #f9f9f9; padding: 20px;">
    <div style="max-width: 600px; margin: auto; background-color: #ffffff; padding: 40px; border-radius: 8px; box-shadow: 0 2px 5px rgba(0,0,0,0.1);">
        <div style="text-align: center;">
            <h1 style="color: #333;">Intellab</h1>
                   
        </div>
        <h2 style="color: #333;">Hello %s!</h2>
        <p style="color: #555;">We received a request to verify email for User ID: <strong>%s</strong></p>
        <p>To verify your email, click the button below:</p>
        <div style="text-align: center; margin: 30px 0;">
            <a href="%s" target="_blank" style="background-color: #007bff; color: white; padding: 12px 24px; text-decoration: none; border-radius: 5px;">Verify email</a>
        </div>
        <p style="color: #777;">
            If you did not make this request, your email address may have been entered by mistake and you can safely disregard this email.
            Visit your account settings page on <a href="%s" target="_blank">Intellab</a> to update your information.
        </p>
        <p>If you have any questions or concerns, please contact us at
            <a href="mailto:graduation21072003@gmail.com">graduation21072003@gmail.com</a>.
        </p>
        <p style="color: #555;">Thank you,<br>The Intellab Team</p>
    </div>
</body>
</html>
""", name, uid, link, feUrl);

                emailService.sendMail(
                        email,
                        "[Intellab] Verify your email",
                        htmlContent);
            }
        } catch (Exception e) {
            throw new SendingEmailFailedException();
        }
    }

    public void sendPasswordResetLink(String email) {
        try {
            UserRecord userRecord = firebaseAuth.getUserByEmail(email);
            String token = jwtUtil.generateJwtByUserUid(userRecord.getUid());

            //String link = firebaseAuthClient.generatePasswordResetLink(email);
            String resetLink = generateCustomResetPasswordLink(token);

            System.out.println("generate email to " + email + " with link: " + resetLink);
            //String htmlContent = "<a href=\"" + link + "\" style=\"color: blue; text-decoration: underline;\">Click here to reset your password</a>";

            //<img src="https://www.docker.com/wp-content/uploads/2022/03/Moby-logo.png" alt="Intellab" width="100" />

            User userFirestore = firestoreService.getUserByUid(userRecord.getUid());

            String name = userFirestore.getFirstName() + " " + userFirestore.getLastName();

            String feUrl = redirectUrlConfig.getFeUrl();

            String htmlContent = String.format("""
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Password Reset</title>
</head>
<body style="font-family: Arial, sans-serif; background-color: #f9f9f9; padding: 20px;">
    <div style="max-width: 600px; margin: auto; background-color: #ffffff; padding: 40px; border-radius: 8px; box-shadow: 0 2px 5px rgba(0,0,0,0.1);">
        <div style="text-align: center;">
            <h1 style="color: #333;">Intellab</h1>
            
        </div>
        <h2 style="color: #333;">Hello %s!</h2>
        <p style="color: #555;">We received a request to update the password for User ID: <strong>%s</strong></p>
        <p>To reset your password, click the button below:</p>
        <div style="text-align: center; margin: 30px 0;">
            <a href="%s" target="_blank" style="background-color: #007bff; color: white; padding: 12px 24px; text-decoration: none; border-radius: 5px;">Reset Password</a>
        </div>
        <p style="color: #777;">
            If you did not make this request, your email address may have been entered by mistake and you can safely disregard this email.
            Visit your account settings page on <a href="%s" target="_blank">Intellab</a> to update your information.
        </p>
        <p>If you have any questions or concerns, please contact us at
            <a href="mailto:graduation21072003@gmail.com">graduation21072003@gmail.com</a>.
        </p>
        <p style="color: #555;">Thank you,<br>The Intellab Team</p>
    </div>
</body>
</html>
""", name, userFirestore.getUid(), resetLink, feUrl);


            emailService.sendMail(
                    email,
                    "[Intellab] Reset password request",
                    htmlContent);
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

    public void setVerifiedEmail(String email) {
        try {
            firebaseAuthClient.setVerifiedEmail(email);
        } catch (Exception e) {
            throw new RuntimeException("Error verifying email: " + e.getMessage(), e);
        }
    }

    public void setUnverifiedListEmails(List<String> email) {
        try {
            firebaseAuthClient.setUnverifiedListEmails(email);
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

        //log.info("uid: {}", uid);

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

    public String generateCustomVerificationLink(String email) {
        try {
            return new StringBuilder()
                    .append(redirectUrlConfig.getCallbackDomain())
                    .append("/identity/auth/callback-set-verified-email")
                    .append("?email=")
                    .append(email)
                    .toString();
        } catch (Exception e) {
            throw new SendingEmailFailedException();
        }
    }

    public String generateCustomResetPasswordLink(String token) {
        try {
            return new StringBuilder()
                    .append(redirectUrlConfig.getFeUrl())
                    .append("/profile/reset-password")
                    .append("?token=")
                    .append(token)
                    .toString();
        } catch (Exception e) {
            throw new SendingEmailFailedException();
        }
    }

    public Boolean updatePasswordForUser(ResetPasswordRequest request) {
        ResetPasswordSessionToken sessionToken = jwtUtil.getUserUidAndExpiration(request.getToken());

        Date now = new Date();

        //log.info("expirationTime: {}", sessionToken.getExpirationDate());
        //log.info("now: {}", now);

        if (now.after(sessionToken.getExpirationDate())) {
            throw new AppException(ErrorCode.TOKEN_IS_EXPIRED);
        }

        try {
            return firebaseAuthClient.updatePasswordByUserUid(sessionToken.getUserUid(), request.getNewPassword());
        } catch (Exception e) {
            throw new AppException(ErrorCode.CANNOT_UPDATE_PASSWORD);
        }
    }
}