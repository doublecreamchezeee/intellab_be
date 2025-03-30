package com.example.problemservice.configuration;

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

    @Bean
    public KubernetesClient kubernetesClient() {
        return new DefaultKubernetesClient(new ConfigBuilder()
                .withMasterUrl(serverUrl)
                .withTrustCerts(true)
                .build()
        );
    }
}
