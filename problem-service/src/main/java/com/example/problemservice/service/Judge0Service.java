package com.example.problemservice.service;

import com.example.problemservice.repository.ProblemSubmissionRepository;
import com.example.problemservice.repository.TestCaseOutputRepository;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.metrics.v1beta1.PodMetricsList;
import io.fabric8.kubernetes.client.KubernetesClient;
import lombok.AllArgsConstructor;
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
    private static final String WORKER_DEPLOYMENT_NAME = "workers";
    private final TestCaseOutputRepository testCaseOutputRepository;

    public Judge0Service(KubernetesClient client, ProblemSubmissionRepository problemSubmissionRepository, TestCaseOutputRepository testCaseOutputRepository) {
        this.client = client;
        this.testCaseOutputRepository = testCaseOutputRepository;
    }

    public List<Map<String, Object>> getSimplifiedPodMetrics() {
        PodMetricsList podMetricsList = client.top().pods().metrics(NAMESPACE);

        return podMetricsList.getItems().stream()
                .filter(podMetrics -> podMetrics.getMetadata().getName().contains("worker"))
                .map(podMetrics -> {
                    String podName = podMetrics.getMetadata().getName();
                    Pod pod = client.pods()
                            .inNamespace(NAMESPACE)
                            .withName(podName)
                            .get();

                    Map<String, Object> podInfo = new HashMap<>();
                    podInfo.put("metadata", Map.of(
                            "name", podName,
                            "namespace", podMetrics.getMetadata().getNamespace()
                    ));
                    podInfo.put("timestamp", podMetrics.getTimestamp());
                    podInfo.put("window", podMetrics.getWindow());

                    // Add pod status/health
                    String podPhase = pod.getStatus() != null ? pod.getStatus().getPhase() : "Unknown";
                    podInfo.put("status", podPhase); // Can be "Running", "Pending", "Failed", etc.

                    // Uptime in seconds
                    String creationTimestampStr = pod.getMetadata().getCreationTimestamp();
                    OffsetDateTime creationTime = OffsetDateTime.parse(creationTimestampStr);
                    Duration uptimeDuration = Duration.between(creationTime.toInstant(), Instant.now());
                    podInfo.put("uptime", uptimeDuration.getSeconds());

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

    public long getSubmissionInQueue() {
        Date tenMinutesAgo = Date.from(Instant.now().minus(Duration.ofMinutes(10)));

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
