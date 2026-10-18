package com.josemaba.marquesitasapi.storage;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Filesystem implementation, intended for local development or a single server with a persistent volume.
 * Files are served by {@link com.josemaba.marquesitasapi.config.LocalMediaConfig} under {@code /media/**}.
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "app.storage.provider", havingValue = "local", matchIfMissing = true)
public class LocalImageStorage implements ImageStorage {

    public static final String MEDIA_PATH = "/media";

    private final Path root;
    private final String publicBaseUrl;

    public LocalImageStorage(StorageProperties properties) {
        this.root = Path.of(properties.local().path()).toAbsolutePath().normalize();
        this.publicBaseUrl = trimTrailingSlash(properties.publicBaseUrl());
    }

    public Path getRoot() {
        return root;
    }

    @Override
    public String store(String folder, String extension, String contentType, InputStream content, long size) throws IOException {
        String key = folder + "/" + UUID.randomUUID() + "." + extension;
        Path target = resolve(key);
        Files.createDirectories(target.getParent());
        Files.copy(content, target, StandardCopyOption.REPLACE_EXISTING);
        return key;
    }

    @Override
    public void delete(String key) {
        try {
            Files.deleteIfExists(resolve(key));
        } catch (IOException | IllegalArgumentException ex) {
            log.warn("Could not delete image '{}': {}", key, ex.getMessage());
        }
    }

    @Override
    public String urlFor(String key) {
        return publicBaseUrl + MEDIA_PATH + "/" + key;
    }

    private Path resolve(String key) {
        Path resolved = root.resolve(key).normalize();
        if (!resolved.startsWith(root)) {
            throw new IllegalArgumentException("Invalid storage key");
        }
        return resolved;
    }

    private static String trimTrailingSlash(String value) {
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }
}
