package com.josemaba.marquesitasapi.util;

import com.josemaba.marquesitasapi.exception.InvalidImageException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Optional;
import org.springframework.web.multipart.MultipartFile;

/** Validates uploads by sniffing magic bytes, ignoring the client-declared content type and filename. */
public final class ImageValidator {

    public record ValidatedImage(String extension, String contentType) {
    }

    private ImageValidator() {
    }

    public static ValidatedImage validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidImageException("Image file is required");
        }
        byte[] header = new byte[12];
        try (InputStream in = file.getInputStream()) {
            int read = in.readNBytes(header, 0, header.length);
            if (read < header.length) {
                throw new InvalidImageException("Unsupported image format; use JPEG, PNG or WebP");
            }
        } catch (IOException ex) {
            throw new InvalidImageException("Could not read image file");
        }
        return detect(header).orElseThrow(() -> new InvalidImageException("Unsupported image format; use JPEG, PNG or WebP"));
    }

    static Optional<ValidatedImage> detect(byte[] h) {
        if ((h[0] & 0xFF) == 0xFF && (h[1] & 0xFF) == 0xD8 && (h[2] & 0xFF) == 0xFF) {
            return Optional.of(new ValidatedImage("jpg", "image/jpeg"));
        }
        if (Arrays.equals(Arrays.copyOf(h, 8), new byte[] {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A})) {
            return Optional.of(new ValidatedImage("png", "image/png"));
        }
        if (h[0] == 'R' && h[1] == 'I' && h[2] == 'F' && h[3] == 'F'
                && h[8] == 'W' && h[9] == 'E' && h[10] == 'B' && h[11] == 'P') {
            return Optional.of(new ValidatedImage("webp", "image/webp"));
        }
        return Optional.empty();
    }
}
