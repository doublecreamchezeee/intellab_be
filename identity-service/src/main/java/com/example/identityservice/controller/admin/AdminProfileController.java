package com.example.identityservice.controller.admin;

import com.example.identityservice.configuration.PublicEndpoint;
import com.example.identityservice.dto.ApiResponse;
import com.example.identityservice.dto.request.profile.MultipleProfileInformationRequest;
import com.example.identityservice.dto.response.admin.AdminUserResponse;
import com.example.identityservice.dto.response.profile.MultipleProfileInformationResponse;
import com.example.identityservice.service.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/profile")
@Tag(name = "Admin Profile")
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminProfileController {
    ProfileService profileService;

    @Operation(
            summary = "Get list user information"
    )
    @PublicEndpoint
    @GetMapping(value = "/list-users")
    public ApiResponse<Page<AdminUserResponse>> getListUsers(
            @ParameterObject Pageable pageable
    ) {
        return ApiResponse.<Page<AdminUserResponse>>builder()
                .message("Get list user information successfully")
                .result(profileService.adminGetListUsers(pageable))
                .build();
    }
}
