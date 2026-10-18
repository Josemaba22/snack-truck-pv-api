package com.josemaba.marquesitasapi.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.josemaba.marquesitasapi.exception.InvalidImageException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

class ImageValidatorTest {

    private static MockMultipartFile file(byte[] bytes) {
        return new MockMultipartFile("file", "whatever.exe", "application/octet-stream", bytes);
    }

    @Test
    void validate_shouldDetectJpeg_whenMagicBytesMatch() {
        byte[] b = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0, 0, 0, 0, 0, 0, 0, 0};
        assertThat(ImageValidator.validate(file(b)).extension()).isEqualTo("jpg");
    }

    @Test
    void validate_shouldDetectWebp_whenRiffWebpHeader() {
        byte[] b = {'R', 'I', 'F', 'F', 0, 0, 0, 0, 'W', 'E', 'B', 'P'};
        assertThat(ImageValidator.validate(file(b)).contentType()).isEqualTo("image/webp");
    }

    @Test
    void validate_shouldThrow_whenContentIsNotAnImage() {
        assertThatThrownBy(() -> ImageValidator.validate(file("<script>alert(1)</script>".getBytes())))
                .isInstanceOf(InvalidImageException.class);
    }

    @Test
    void validate_shouldThrow_whenFileIsEmpty() {
        assertThatThrownBy(() -> ImageValidator.validate(file(new byte[0])))
                .isInstanceOf(InvalidImageException.class);
    }
}
