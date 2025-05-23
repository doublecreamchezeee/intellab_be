package com.example.problemservice.configuration;

import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
                .withAuthorization("Bearer " + token)
                .withTrustCerts(true)
                .build();

        return new DefaultKubernetesClient(config);
    }
}
