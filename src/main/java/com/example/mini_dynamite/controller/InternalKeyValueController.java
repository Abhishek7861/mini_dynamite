package com.example.mini_dynamite.controller;

import com.example.mini_dynamite.service.KeyValueService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.rocksdb.RocksDBException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/kv")
@AllArgsConstructor
@Slf4j
public class InternalKeyValueController {
    private final KeyValueService service;

    @GetMapping("/{key}")
    public ResponseEntity<String> get(
            @PathVariable String key
    ) throws RocksDBException {
        log.info("Received Internal Get Request | Key: {}",key);

        String value = service.get(key);

        if (value == null) {
            log.info("Not Found | Key: {}",key);
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(value);
    }

    @PutMapping("/{key}")
    public ResponseEntity<Void> put(
            @PathVariable String key,
            @RequestBody String value
    ) throws RocksDBException {
        log.info("Received Internal Put Request | Key: {}, Value: {}",key,value);

        service.put(key, value);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{key}")
    public ResponseEntity<Void> delete(
            @PathVariable String key
    ) throws RocksDBException {

        log.info("Received Internal Delete Request | Key: {}",key);

        service.delete(key);

        return ResponseEntity.noContent().build();
    }
}
