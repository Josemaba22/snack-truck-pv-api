package com.josemaba.marquesitasapi.storage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class LocalImageStorageTest {

    @TempDir
    Path tempDir;

    private LocalImageStorage storage;

    @BeforeEach
    void setUp() {
        storage = new LocalImageStorage(new StorageProperties("local", "http://host:8081/", new StorageProperties.Local(tempDir.toString())));
    }

    @Test
    void store_shouldWriteFileAndReturnKey_whenContentProvided() throws IOException {
        String key = storage.store("products", "png", "image/png", new ByteArrayInputStream(new byte[] {1, 2, 3}), 3);

        assertThat(key).startsWith("products/").endsWith(".png");
        assertThat(Files.readAllBytes(tempDir.resolve(key))).containsExactly(1, 2, 3);
    }

    @Test
    void delete_shouldRemoveFile_whenKeyExists() throws IOException {
        String key = storage.store("products", "png", "image/png", new ByteArrayInputStream(new byte[] {1}), 1);

        storage.delete(key);

        assertThat(tempDir.resolve(key)).doesNotExist();
    }

    @Test
    void delete_shouldNotDeleteOutsideRoot_whenKeyTraversesPath() throws IOException {
        Path outside = Files.createTempFile(tempDir.getParent(), "outside", ".txt");

        storage.delete("../" + outside.getFileName());

        assertThat(outside).exists();
        Files.delete(outside);
    }

    @Test
    void urlFor_shouldJoinBaseUrlAndKey_whenBaseUrlHasTrailingSlash() {
        assertThat(storage.urlFor("products/a.png")).isEqualTo("http://host:8081/media/products/a.png");
    }
}
