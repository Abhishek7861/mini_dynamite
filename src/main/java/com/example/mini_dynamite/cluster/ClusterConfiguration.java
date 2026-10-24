package com.example.mini_dynamite.cluster;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class ClusterConfiguration {

    @Bean
    public ConsistentHashRing consistentHashRing(
            @Value("${cluster.virtual-nodes:50}")
            int virtualNodes,
            @Value("${cluster.nodes}")
            List<String> nodeDefinitions
    ){
        ConsistentHashRing ring =
                new ConsistentHashRing(virtualNodes);

        for (String definition : nodeDefinitions) {

            String[] parts = definition.split("\\|", 2);

            if (parts.length != 2) {
                throw new IllegalArgumentException(
                        "Invalid cluster node: " + definition
                );
            }

            ring.addNode(
                    new ClusterNode(parts[0], parts[1])
            );
        }

        return ring;
    }
}
