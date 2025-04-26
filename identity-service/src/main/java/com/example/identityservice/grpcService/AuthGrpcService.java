/*
package com.example.identityservice.grpcService;

import com.example.identityservice.proto.AuthServiceGrpc;
import com.example.identityservice.proto.ValidateTokenRequest;
import com.example.identityservice.proto.ValidateTokenResponse;
import com.example.identityservice.service.AuthService;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
//import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.stereotype.Service;


//@GrpcService
@Service
@RequiredArgsConstructor
public class AuthGrpcService extends AuthServiceGrpc.AuthServiceImplBase {

    @Override
    public void validateToken(ValidateTokenRequest request, StreamObserver<ValidateTokenResponse> responseObserver) {
        String token = request.getToken();

        // Validate the token (mock implementation)
        boolean isValid = token.equals("valid-token");
        String userId = isValid ? "12345" : null;
        String role = isValid ? "USER" : null;
        String message = isValid ? "Token is valid" : "Invalid token";

        ValidateTokenResponse response = ValidateTokenResponse.newBuilder()
                .setIsValidated(isValid)
                .setUserId(userId)
                .setRole(role)
                .setMessage(message)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
*/
