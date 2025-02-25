package com.example.identityservice.controller;

import com.example.identityservice.configuration.PublicEndpoint;
import com.example.identityservice.dto.ApiResponse;
import com.example.identityservice.dto.request.auth.UserUpdateRequest;
import com.example.identityservice.dto.request.profile.MultipleProfileInformationRequest;
import com.example.identityservice.dto.request.profile.SingleProfileInformationRequest;
import com.example.identityservice.dto.response.profile.ProgressLanguageResponse;
import com.example.identityservice.dto.response.profile.ProgressLevelResponse;
import org.springframework.security.core.GrantedAuthority;
import com.example.identityservice.dto.response.auth.UserInfoResponse;
import com.example.identityservice.dto.response.profile.MultipleProfileInformationResponse;
import com.example.identityservice.dto.response.profile.SingleProfileInformationResponse;
import com.example.identityservice.service.AuthService;
import com.example.identityservice.service.ProfileService;
import com.google.firebase.auth.FirebaseAuthException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/profile")
@Tag(name = "Profile")
public class ProfileController {
    private final ProfileService profileService;
    private final AuthService authService;

    @Operation(
            summary = "Get single profile information"
    )
    @GetMapping(value = "/single")
    public ApiResponse<SingleProfileInformationResponse> getSingleProfileInformation(Authentication authentication) {
        String userId = (String) authentication.getPrincipal();
        return ApiResponse.<SingleProfileInformationResponse>builder()
                        .result(profileService.getSingleProfileInformation(userId))
                        .build();
    }

    @Operation(
            summary = "Get multiple profile information"
    )
    @PublicEndpoint
    @PostMapping(value = "/multiple", consumes = MediaType.APPLICATION_JSON_VALUE)
    /*
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE

     */
    public ApiResponse<MultipleProfileInformationResponse> getMultipleProfileInformation(
            @RequestBody @Valid MultipleProfileInformationRequest request
    ) {
        return ApiResponse.<MultipleProfileInformationResponse>builder()
                .result(profileService.getMultipleProfileInformation(request))
                .build();
    }

    @GetMapping("/me")
    public ResponseEntity<UserInfoResponse> getMyProfile(Authentication authentication) {
        String userId = (String) authentication.getPrincipal();
        String role = authentication.getAuthorities().stream()
                .findFirst() // Get first authority (ROLE_xxx)
                .map(GrantedAuthority::getAuthority)
                .map(auth -> auth.replace("ROLE_", "")) // Remove "ROLE_" prefix
                .orElse("USER");
        UserInfoResponse response = profileService.getUserInfo(userId, null);
        response.setRole(role);
        return ResponseEntity.ok(response);
    }

//    @Operation(
//            summary = "get profile photo url"
//    )
//    @GetMapping(value = "/photo")
//    public ApiResponse<String> getProfilePictureUrl(Authentication authentication) {
//        String userId = (String) authentication.getPrincipal();
//        return ApiResponse.<String>builder()
//                .result(profileService.getProfilePictureUrlByEmail(userId))
//                .build();
//    }

    @Operation(
            summary = "Upload profile photo url"
    )
    @PostMapping(value = "/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<String> uploadProfilePicture(Authentication authentication, @RequestPart(value = "file", required = true) MultipartFile file) throws FirebaseAuthException {
        System.out.println("Received file: " + file.getOriginalFilename() +  file.getSize());
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded file is empty!");
        }

        String userId = (String) authentication.getPrincipal();
        profileService.uploadProfilePicture(userId, file);
        return ApiResponse.<String>builder()
                .code(200)
                .message("Profile picture uploaded successfully")
                .build();
    }

    @Operation(
            summary = "Update user"
    )
    @PutMapping("/update")
    public ResponseEntity<HttpStatus> updateUserByEmail(Authentication authentication, @Validated @RequestBody UserUpdateRequest userUpdateRequest) {
        String userId = (String) authentication.getPrincipal();
        authService.update(userId, userUpdateRequest);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @Operation(
            summary = "Statistic by getting progress level"
    )
    @GetMapping("/statistics/progress/level")
    public ResponseEntity<ProgressLevelResponse> getProgressLevel() {
        return ResponseEntity.ok(profileService.getProgressLevel());
    }

    @Operation(
            summary = "Statistic by getting progress"
    )
    @GetMapping("/statistics/progress/language")
    public ResponseEntity<ProgressLanguageResponse> getProgressLanguage() {
        return ResponseEntity.ok(profileService.getProgressLanguage());
    }


}
