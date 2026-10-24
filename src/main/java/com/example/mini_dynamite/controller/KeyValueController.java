package com.example.mini_dynamite.controller;

import com.example.mini_dynamite.service.ClusterRouterService;
import com.example.mini_dynamite.service.KeyValueService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.rocksdb.RocksDBException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/kv")
@AllArgsConstructor
@Slf4j
public class KeyValueController {
    private final KeyValueService service;
    private final ClusterRouterService router;

    public record PutRequest(String value) {}

    @PutMapping("/{key}")
    public ResponseEntity<?> put(@PathVariable String key, @RequestBody PutRequest request
    ) throws RocksDBException {

        log.info("Received Put Request | Key: {}, Value: {}",key,request.value);

        if (request == null || request.value() == null) {
            return ResponseEntity.badRequest().build();
        }

        if (router.isLocal(key)) {
            service.put(key, request.value());
            log.info("Stored | Key: {}, Value: {}",key,request.value);
        } else {
            router.putRemote(key, request.value());
            log.info("Sending to other Node| Key: {}, Value: {}",key,request.value);
        }

        return ResponseEntity.ok(
                Map.of(
                        "key", key,
                        "value", request.value(),
                        "owner", router.owner(key).id()
                )
        );
    }

    @GetMapping("/{key}")
    public ResponseEntity<?> get( @PathVariable String key) throws RocksDBException {

        String value;

        log.info("Received Get Request | Key: {}",key);


        if (router.isLocal(key)) {
            value = service.get(key);
            log.info("Search Local | Key: {}, Value: {}",key, value);
        } else {
            value = router.getRemote(key);
            log.info("Get Remote | Key: {}, Value: {}",key, value);
        }

        if (value == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(
                Map.of(
                        "key", key,
                        "value", value,
                        "owner", router.owner(key).id()
                )
        );
    }

    @DeleteMapping("/{key}")
    public ResponseEntity<Void> delete( @PathVariable String key) throws RocksDBException {

        log.info("Received Delete Request | Key: {}",key);

        if (router.isLocal(key)) {
            service.delete(key);
            log.info("Deleted | Key: {}",key);
        } else {
            router.deleteRemote(key);
            log.info("Remote Delete | Key: {}",key);
        }

        return ResponseEntity.noContent().build();
    }
}
