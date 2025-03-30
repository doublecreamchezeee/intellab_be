package com.example.problemservice.service;

import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.springframework.stereotype.Service;

@Service
public class Judge0Service {
    private final KubernetesClient kubernetesClient;
    private static final String NAMESPACE = "default"; // Đổi nếu cần
    private static final String DEPLOYMENT_NAME = "workers";

    public Judge0Service(KubernetesClient kubernetesClient) {
        this.kubernetesClient = kubernetesClient;
    }

    public int getReplicas() {
        Deployment deployment = kubernetesClient.apps().deployments()
                .inNamespace(NAMESPACE)
                .withName(DEPLOYMENT_NAME)
                .get();

        return (deployment != null && deployment.getSpec() != null) ? deployment.getSpec().getReplicas() : 0;
    }

    public void scaleReplicas(int replicas) {
        kubernetesClient.apps().deployments()
                .inNamespace(NAMESPACE)
                .withName(DEPLOYMENT_NAME)
                .scale(replicas);
    }
}
