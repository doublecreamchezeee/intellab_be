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
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/profile")
@Tag(name = "Admin Profile")
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminProfileController {
    ProfileService profileService;

    @Operation(
            summary = "Get or search list user's information",
            description = """
                    Keyword is optional. If keyword is not null, it will search for users by display name.
                    If keyword is null, it will return all users.
                    """
    )
    @PublicEndpoint
    @GetMapping(value = "/list-users")
    public ApiResponse<Page<AdminUserResponse>> getListUsers(
            @RequestParam(value = "keyword", required = false) String keyword,
            @ParameterObject Pageable pageable
    ) {
        if (keyword != null) {
            return ApiResponse.<Page<AdminUserResponse>>builder()
                    .message("Search list user information successfully")
                    .result(profileService.adminFindUsersByDisplayName(keyword, pageable))
                    .build();
        }
        return ApiResponse.<Page<AdminUserResponse>>builder()
                .message("Get list user information successfully")
                .result(profileService.adminGetListUsers(pageable))
                .build();
    }
}
