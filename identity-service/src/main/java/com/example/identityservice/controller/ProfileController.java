package com.example.identityservice.controller;

import com.example.identityservice.configuration.PublicEndpoint;
import com.example.identityservice.dto.ApiResponse;
import com.example.identityservice.dto.request.profile.MultipleProfileInformationRequest;
import com.example.identityservice.dto.request.profile.SingleProfileInformationRequest;
import com.example.identityservice.dto.response.profile.MultipleProfileInformationResponse;
import com.example.identityservice.dto.response.profile.SingleProfileInformationResponse;
import com.example.identityservice.service.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/profile")
@Tag(name = "Profile")
public class ProfileController {
    private final ProfileService profileService;

    @Operation(
            summary = "Get single profile information"
    )
    @PublicEndpoint
    @PostMapping(value = "/single", consumes = MediaType.APPLICATION_JSON_VALUE)
    /*,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)*/
    public ApiResponse<SingleProfileInformationResponse> getSingleProfileInformation(
            @RequestBody @Valid SingleProfileInformationRequest request
    ) {
        return ApiResponse.<SingleProfileInformationResponse>builder()
                        .result(profileService.getSingleProfileInformation(request))
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

    @Operation(
            summary = "get profile photo url"
    )
    @PublicEndpoint
    @PostMapping(value = "/photo", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse<String> getProfilePictureUrlByEmail(
            @RequestBody @Valid String request
    ) {
        return ApiResponse.<String>builder()
                .result(profileService.getProfilePictureUrlByEmail(request))
                .build();
    }
}
