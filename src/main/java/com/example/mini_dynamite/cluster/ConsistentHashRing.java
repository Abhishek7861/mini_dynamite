package com.example.mini_dynamite.cluster;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

public class ConsistentHashRing {
    private final NavigableMap<BigInteger, ClusterNode> ring =
            new TreeMap<>();

    private final int virtualNodes;

    public ConsistentHashRing(int virtualNodes) {
        if (virtualNodes <= 0) {
            throw new IllegalArgumentException(
                    "Virtual nodes must be positive"
            );
        }
        this.virtualNodes = virtualNodes;
    }

    public synchronized void addNode(ClusterNode node) {

        for (int i = 0; i < virtualNodes; i++) {

            String virtualNodeKey = node.id() + ":" + i;

            BigInteger hash = hash(virtualNodeKey);

            if (ring.containsKey(hash)) {
                throw new IllegalStateException(
                        "Hash collision for virtual node: "
                                + virtualNodeKey
                );
            }

            ring.put(hash, node);
        }
    }

    public synchronized ClusterNode getNode(String key) {

        if (ring.isEmpty()) {
            throw new IllegalStateException(
                    "No nodes available"
            );
        }

        BigInteger keyHash = hash(key);

        Map.Entry<BigInteger, ClusterNode> entry =
                ring.ceilingEntry(keyHash);

        if (entry == null) {
            entry = ring.firstEntry();
        }

        return entry.getValue();
    }

    public synchronized void removeNode(String nodeId) {
        ring.entrySet().removeIf(
                entry -> entry.getValue().id().equals(nodeId)
        );
    }

    public synchronized int size() {
        return ring.size();
    }

    private BigInteger hash(String input) {

        try {
            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] bytes = digest.digest(
                    input.getBytes(StandardCharsets.UTF_8)
            );

            return new BigInteger(1, bytes);

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

}
