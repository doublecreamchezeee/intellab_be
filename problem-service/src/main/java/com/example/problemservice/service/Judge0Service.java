package com.example.problemservice.service;

import com.example.problemservice.model.TestCaseOutput;
import com.example.problemservice.repository.ProblemSubmissionRepository;
import com.example.problemservice.repository.TestCaseOutputRepository;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.metrics.v1beta1.PodMetrics;
import io.fabric8.kubernetes.api.model.metrics.v1beta1.PodMetricsList;
import io.fabric8.kubernetes.client.KubernetesClient;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class Judge0Service {
    private final KubernetesClient client;
    private static final String NAMESPACE = "default";
    private static final String WORKER_DEPLOYMENT_NAME = "workers";
    private final TestCaseOutputRepository testCaseOutputRepository;

    public Judge0Service(KubernetesClient client, ProblemSubmissionRepository problemSubmissionRepository, TestCaseOutputRepository testCaseOutputRepository) {
        this.client = client;
        this.testCaseOutputRepository = testCaseOutputRepository;
    }

    public List<Map<String, Object>> getSimplifiedPodMetrics() {
        // Step 1: get all pods
        List<Pod> pods = client.pods()
                .inNamespace(NAMESPACE)
                .list()
                .getItems()
                .stream()
                .filter(pod -> pod.getMetadata().getName().contains("worker"))
                .toList();

        // Step 2: get all pod metrics and map them by pod name
        Map<String, PodMetrics> metricsMap = Optional.ofNullable(client.top().pods().metrics(NAMESPACE))
                .map(PodMetricsList::getItems)
                .orElse(List.of())
                .stream()
                .collect(Collectors.toMap(
                        podMetrics -> podMetrics.getMetadata().getName(),
                        Function.identity()
                ));

        // Step 3: process each pod
        return pods.stream().map(pod -> {
            String podName = pod.getMetadata().getName();

            Map<String, Object> podInfo = new HashMap<>();
            podInfo.put("metadata", Map.of(
                    "name", podName,
                    "namespace", pod.getMetadata().getNamespace()
            ));

            // Status (simplified)
            String phase = pod.getStatus() != null ? pod.getStatus().getPhase() : "Unknown";
            String finalStatus = phase;

            if (pod.getMetadata().getDeletionTimestamp() != null) {
                finalStatus = "Terminating";
            } else if ("Pending".equals(phase)) {
                finalStatus = "Creating";
            }

            podInfo.put("status", finalStatus);

            // Uptime
            String creationTimestampStr = pod.getMetadata().getCreationTimestamp();
            OffsetDateTime creationTime = OffsetDateTime.parse(creationTimestampStr);
            Duration uptimeDuration = Duration.between(creationTime.toInstant(), Instant.now());
            podInfo.put("uptime", uptimeDuration.getSeconds());

            // Restart count
            int restarts = 0;
            if (pod.getStatus() != null && pod.getStatus().getContainerStatuses() != null) {
                restarts = pod.getStatus().getContainerStatuses().stream()
                        .mapToInt(status -> status.getRestartCount() != null ? status.getRestartCount() : 0)
                        .sum();
            }
            podInfo.put("restartCount", restarts);

            // Container metrics (if exist)
            PodMetrics podMetrics = metricsMap.get(podName);
            if (podMetrics != null) {
                podInfo.put("timestamp", podMetrics.getTimestamp());
                podInfo.put("window", podMetrics.getWindow());

                List<Map<String, Object>> containers = podMetrics.getContainers().stream().map(container -> {
                    Map<String, Object> containerInfo = new HashMap<>();
                    containerInfo.put("name", container.getName());
                    containerInfo.put("usage", container.getUsage());
                    return containerInfo;
                }).collect(Collectors.toList());

                podInfo.put("containers", containers);
            } else {
                // No metrics yet — include empty containers list
                podInfo.put("containers", List.of());
                podInfo.put("timestamp", null);
                podInfo.put("window", null);
            }

            return podInfo;
        }).collect(Collectors.toList());
    }

    public long getSubmissionInQueue() {
        Date tenMinutesAgo = Date.from(Instant.now().minus(Duration.ofMinutes(3)));

        List<TestCaseOutput> timedOut = testCaseOutputRepository.findTimedOutInQueue("In Queue", tenMinutesAgo);

        for (TestCaseOutput t : timedOut) {
            t.setResult_status("Time out");
        }

        testCaseOutputRepository.saveAll(timedOut);

        return testCaseOutputRepository.countByStatus("In Queue");
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
