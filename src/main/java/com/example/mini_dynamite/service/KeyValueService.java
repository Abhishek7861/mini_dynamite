package com.example.mini_dynamite.service;

import com.example.mini_dynamite.storage.RocksDbStorage;
import lombok.AllArgsConstructor;
import org.rocksdb.RocksDBException;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class KeyValueService {
    private final RocksDbStorage storage;

    public void put(String key, String value)
            throws RocksDBException {
        storage.put(key, value);
    }

    public String get(String key)
            throws RocksDBException {
        return storage.get(key);
    }

    public void delete(String key)
            throws RocksDBException {
        storage.delete(key);
    }
}
