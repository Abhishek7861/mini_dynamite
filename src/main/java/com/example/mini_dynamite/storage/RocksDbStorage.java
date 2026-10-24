package com.example.mini_dynamite.storage;

import jakarta.annotation.PreDestroy;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.rocksdb.WriteOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.rocksdb.util.ByteUtil.bytes;

/**
 * Local key-value storage engine for a mini_dynamite node, backed by an
 * embedded RocksDB instance.
 *
 * <p>Keys and values are plain {@link String}s, stored on disk as UTF-8 bytes.
 * Every write is synced to disk before it returns, so an acknowledged
 * {@link #put} or {@link #delete} survives a process crash.
 *
 * <p>Spring creates this as a singleton bean. The database is opened when the
 * bean is constructed and closed when the application context shuts down
 * (see {@link #close()}). RocksDB handles are thread-safe, so one instance can
 * be shared across request threads.
 */
@Component
public class RocksDbStorage {
    private final RocksDB db;
    // Options and WriteOptions wrap native (C++) objects. They are kept as
    // fields so they can be freed explicitly in close() rather than leaking.
    private final Options options;
    private final WriteOptions writeOptions;

    /**
     * Opens (or creates) the RocksDB database at the configured path.
     *
     * @param path filesystem directory for the database files, taken from the
     *             {@code storage.path} application property; created if it
     *             does not exist
     * @throws Exception if the directory cannot be created, the native library
     *                   cannot be loaded, or RocksDB fails to open the database
     *                   (for example because another process holds its lock)
     */
    public RocksDbStorage(
            @Value("${storage.path}") String path
    ) throws Exception {

        // Load the bundled native RocksDB library; safe to call more than once.
        RocksDB.loadLibrary();
        Files.createDirectories(Path.of(path));

        // Create a fresh database on first start instead of failing.
        options = new Options()
                .setCreateIfMissing(true);

        // Sync (fsync) the write-ahead log on every write: slower, but no
        // acknowledged write is lost if the machine crashes.
        writeOptions = new WriteOptions()
                .setSync(true);

        db = RocksDB.open(options, path);
    }

    /**
     * Stores a value under a key, overwriting any existing value.
     *
     * @param key   the key; must not be {@code null}
     * @param value the value; must not be {@code null}
     * @throws RocksDBException if RocksDB fails to write the entry
     */
    public void put(String key, String value)
            throws RocksDBException {

        db.put(writeOptions, bytes(key), bytes(value));
    }

    /**
     * Looks up the value stored under a key.
     *
     * @param key the key; must not be {@code null}
     * @return the stored value, or {@code null} if the key is not present
     * @throws RocksDBException if RocksDB fails to read the entry
     */
    public String get(String key)
            throws RocksDBException {

        byte[] value = db.get(bytes(key));

        // RocksDB returns null for a missing key; pass that through as-is.
        return value == null
                ? null
                : new String(value, StandardCharsets.UTF_8);
    }

    /**
     * Removes a key and its value. Deleting a key that does not exist is a
     * no-op.
     *
     * @param key the key; must not be {@code null}
     * @throws RocksDBException if RocksDB fails to write the deletion
     */
    public void delete(String key)
            throws RocksDBException {

        db.delete(writeOptions, bytes(key));
    }

    // Encodes a string as UTF-8 bytes for RocksDB. This method takes
    // precedence over the statically imported ByteUtil.bytes.
    private byte[] bytes(String value) {
        return value.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Closes the database and frees its native resources. Spring calls this
     * automatically when the application context shuts down.
     */
    @PreDestroy
    public void close() {
        // Close the database before the options objects it was opened with.
        db.close();
        writeOptions.close();
        options.close();
    }

}
