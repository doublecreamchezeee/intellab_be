package com.example.problemservice.service;

import com.example.problemservice.client.IdentityClient;
import com.example.problemservice.dto.request.profile.MultipleProfileInformationRequest;
import com.example.problemservice.dto.request.profile.SingleProfileInformationRequest;
import com.example.problemservice.dto.response.ApiResponse;
import com.example.problemservice.dto.response.profile.MultipleProfileInformationResponse;
import com.example.problemservice.dto.response.profile.SingleProfileInformationResponse;
import com.example.problemservice.exception.AppException;
import com.example.problemservice.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProfileService {
    private final IdentityClient identityClient;

    public MultipleProfileInformationResponse getMultipleProfileInformation(MultipleProfileInformationRequest request) {
        try {
            ApiResponse<MultipleProfileInformationResponse> response =  identityClient
                    .getMultipleProfileInformation(
                            request
                    ).block();

            if (response == null) {
                throw new AppException(ErrorCode.IDENTITY_SERVER_ERROR);
            }

            return response.getResult();

        } catch (Exception e) {
            throw new AppException(ErrorCode.IDENTITY_SERVER_ERROR);
        }
    }

    public SingleProfileInformationResponse getSingleProfileInformation(String userId) {
        try {
            ApiResponse<SingleProfileInformationResponse> response =  identityClient
                    .getSingleProfileInformation(
                            new SingleProfileInformationRequest(
                                    userId
                            )
                    ).block();

            if (response == null) {
                throw new AppException(ErrorCode.IDENTITY_SERVER_ERROR);
            }

            return response.getResult();

        } catch (Exception e) {
            throw new AppException(ErrorCode.IDENTITY_SERVER_ERROR);
        }
    }

}
