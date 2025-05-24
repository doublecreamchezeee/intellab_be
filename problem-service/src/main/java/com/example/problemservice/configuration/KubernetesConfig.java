package com.example.problemservice.configuration;

import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

@Configuration
public class KubernetesConfig {
    @Value("${k8s.server.url}")
    private String serverUrl;

    @Value("${k8s.server.token}")
    private String token;

    @Bean
    public KubernetesClient kubernetesClient() {
        Config config = new ConfigBuilder()
                .withMasterUrl(serverUrl)
                .withOauthToken(token)
                .withTrustCerts(true)
                .build();

        return new DefaultKubernetesClient(config);
    }
}
