/*
package com.example.identityservice.configuration;

import com.example.identityservice.grpcService.AuthGrpcService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
//import net.devh.boot.grpc.server.service.GrpcService;

@Configuration
public class GrpcServerConfig {

    @Value("${grpc.server.port}")
    private int port;

    @Bean
    public Server grpcServer(AuthGrpcService authService) throws IOException {
        Server server = ServerBuilder.forPort(port)
                .addService(authService)
                .build();
        server.start();
        return server;
    }
}
*/
