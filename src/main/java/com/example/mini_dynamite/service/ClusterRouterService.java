package com.example.mini_dynamite.service;

import com.example.mini_dynamite.cluster.ClusterNode;
import com.example.mini_dynamite.cluster.ConsistentHashRing;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class ClusterRouterService {
    private final ConsistentHashRing ring;
    private final String currentNodeId;
    private final RestClient restClient;

    public ClusterRouterService(
            ConsistentHashRing ring,
            @Value("${cluster.node-id}") String currentNodeId,
            RestClient.Builder restClientBuilder
    ) {
        this.ring = ring;
        this.currentNodeId = currentNodeId;
        this.restClient = restClientBuilder.build();
    }

    public ClusterNode owner(String key) {
        return ring.getNode(key);
    }

    public boolean isLocal(String key) {
        return owner(key).id().equals(currentNodeId);
    }

    public String getRemote(String key) {

        return restClient.get()
                .uri(owner(key).url() + "/internal/kv/{key}", key)
                .retrieve()
                .body(String.class);
    }

    public void putRemote(String key, String value) {

        restClient.put()
                .uri(owner(key).url() + "/internal/kv/{key}", key)
                .body(value)
                .retrieve()
                .toBodilessEntity();
    }

    public void deleteRemote(String key) {

        restClient.delete()
                .uri(owner(key).url() + "/internal/kv/{key}", key)
                .retrieve()
                .toBodilessEntity();
    }
}
