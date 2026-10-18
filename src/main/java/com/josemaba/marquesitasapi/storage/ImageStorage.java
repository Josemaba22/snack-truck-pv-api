package com.josemaba.marquesitasapi.storage;

import java.io.IOException;
import java.io.InputStream;

/**
 * Hosting-agnostic image storage. The database only keeps the opaque key returned by
 * {@link #store}; the public URL is resolved on demand via {@link #urlFor}, so switching
 * provider or domain never requires migrating data.
 */
public interface ImageStorage {

    /**
     * Stores the image under the given folder and returns its generated key
     * (e.g. {@code products/<uuid>.webp}).
     */
    String store(String folder, String extension, String contentType, InputStream content, long size) throws IOException;

    /** Deletes the image if it exists. Missing keys are ignored. */
    void delete(String key);

    /** Public URL for a key previously returned by {@link #store}. */
    String urlFor(String key);
}
