package com.example.problemservice.service;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.Node;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class Judge0Service {
    private final KubernetesClient kubernetesClient;
    private static final String NAMESPACE = "default";
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

    public List<String> getPods() {
        List<Pod> pods = kubernetesClient.pods()
                .inNamespace(NAMESPACE)
                .list()
                .getItems();

        return pods.stream()
                .map(pod -> String.format("%s - %s", pod.getMetadata().getName(), pod.getStatus().getPhase()))
                .collect(Collectors.toList());
    }

    public List<String> getNodes() {
        List<Node> nodes = kubernetesClient.nodes()
                .list()
                .getItems();

        return nodes.stream()
                .map(node -> String.format("%s - %s", node.getMetadata().getName(), getNodeCondition(node)))
                .collect(Collectors.toList());
    }

    private String getNodeCondition(Node node) {
        // Return the "Ready" condition status or UNKNOWN if not found
        return node.getStatus().getConditions().stream()
                .filter(cond -> "Ready".equals(cond.getType()))
                .findFirst()
                .map(cond -> cond.getStatus())
                .orElse("UNKNOWN");
    }
}