package com.example.problemservice.service;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodMetricsList;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class Judge0Service {
    private final KubernetesClient client;
    private static final String NAMESPACE = "default";
    private static final String WORKER_DEPLOYMENT_NAME = "judge0-worker";

    public Judge0Service(KubernetesClient client) {
        this.client = client;
    }

    public List<Map<String, Object>> getSimplifiedPodMetrics() {
        PodMetricsList podMetricsList = client.top().pods().metrics(NAMESPACE);

        return podMetricsList.getItems().stream().map(podMetrics -> {
            Pod pod = client.pods().inNamespace(NAMESPACE).withName(podMetrics.getMetadata().getName()).get();

            Map<String, Object> podInfo = new HashMap<>();
            podInfo.put("metadata", Map.of(
                "name", podMetrics.getMetadata().getName(),
                "namespace", podMetrics.getMetadata().getNamespace()
            ));
            podInfo.put("timestamp", podMetrics.getTimestamp());
            podInfo.put("window", podMetrics.getWindow());

            // Uptime in seconds
            String creationTimestampStr = pod.getMetadata().getCreationTimestamp();
            OffsetDateTime creationTime = OffsetDateTime.parse(creationTimestampStr);
            Duration uptimeDuration = Duration.between(creationTime.toInstant(), Instant.now());
            podInfo.put("uptime", uptimeDuration.getSeconds());

            // Container usage
            List<Map<String, Object>> containers = podMetrics.getContainers().stream().map(container -> {
                Map<String, Object> containerInfo = new HashMap<>();
                containerInfo.put("name", container.getName());
                containerInfo.put("usage", container.getUsage());
                return containerInfo;
            }).collect(Collectors.toList());

            podInfo.put("containers", containers);
            return podInfo;
        }).collect(Collectors.toList());
    }

    public void scaleJudge0Workers(int replicas) {
        client.apps().deployments()
              .inNamespace(NAMESPACE)
              .withName(WORKER_DEPLOYMENT_NAME)
              .scale(replicas, true);
    }

    public int getJudge0WorkerReplicas() {
        Deployment deployment = client.apps().deployments()
                                      .inNamespace(NAMESPACE)
                                      .withName(WORKER_DEPLOYMENT_NAME)
                                      .get();
        if (deployment != null && deployment.getSpec() != null) {
            return deployment.getSpec().getReplicas();
        }
        return -1;
    }
}
