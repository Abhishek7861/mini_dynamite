package com.example.mini_dynamite.controller;

import com.example.mini_dynamite.service.KeyValueService;
import lombok.AllArgsConstructor;
import org.rocksdb.RocksDBException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/kv")
@AllArgsConstructor
public class KeyValueController {
    private final KeyValueService service;

    public record PutRequest(String value) {}

    @PutMapping("/{key}")
    public ResponseEntity<?> put(@PathVariable String key, @RequestBody PutRequest request
    ) throws RocksDBException {

        if (request == null || request.value() == null) {
            return ResponseEntity.badRequest().build();
        }

        service.put(key, request.value());

        return ResponseEntity.ok(
                Map.of("key", key, "value", request.value())
        );
    }

    @GetMapping("/{key}")
    public ResponseEntity<?> get( @PathVariable String key) throws RocksDBException {

        String value = service.get(key);

        if (value == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(
                Map.of("key", key, "value", value)
        );
    }

    @DeleteMapping("/{key}")
    public ResponseEntity<Void> delete( @PathVariable String key) throws RocksDBException {

        service.delete(key);
        return ResponseEntity.noContent().build();
    }
}
