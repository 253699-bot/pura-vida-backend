package com.puravida.modules.menu.infrastructure.storage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.puravida.modules.menu.domain.exception.MenuValidationException;
import com.puravida.shared.domain.exception.NotFoundException;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class LocalDishImageStorageAdapterTest {

    @TempDir
    Path tempDir;

    private LocalDishImageStorageAdapter adapter;

    @BeforeEach
    void setUp() {
        DishImageStorageProperties properties = new DishImageStorageProperties();
        properties.setRoot(tempDir.toString());
        properties.setMaxBytes(2_097_152L);
        adapter = new LocalDishImageStorageAdapter(properties);
    }

    @Test
    void storesAndLoadsVerifiedPngUnderGeneratedSafeName() throws Exception {
        byte[] png = pngBytes();

        var stored = adapter.store(png, "image/png");
        var loaded = adapter.load(stored.key());

        assertThat(stored.key()).matches("[0-9a-f-]{36}\\.png");
        assertThat(Files.isRegularFile(tempDir.resolve(stored.key()))).isTrue();
        assertThat(loaded.content()).isEqualTo(png);
        assertThat(loaded.mediaType()).isEqualTo("image/png");
        assertThat(loaded.etag()).hasSize(64);
    }

    @Test
    void storesWebpWhenHeaderAndDeclaredTypeAreValid() {
        byte[] webp = webpBytes();

        var stored = adapter.store(webp, "image/webp");
        var loaded = adapter.load(stored.key());

        assertThat(stored.key()).matches("[0-9a-f-]{36}\\.webp");
        assertThat(loaded.mediaType()).isEqualTo("image/webp");
        assertThat(loaded.content()).isEqualTo(webp);
    }

    @Test
    void rejectsSpoofedOversizedContentAndUnsafeKeys() {
        assertThatThrownBy(() -> adapter.store("not-an-image".getBytes(), "image/png"))
                .isInstanceOf(MenuValidationException.class);
        assertThatThrownBy(() -> adapter.store(webpBytes(), "image/png"))
                .isInstanceOf(MenuValidationException.class);
        assertThatThrownBy(() -> adapter.store(new byte[2_097_153], "image/png"))
                .isInstanceOf(MenuValidationException.class);
        assertThatThrownBy(() -> adapter.load("../secret.png"))
                .isInstanceOf(NotFoundException.class);
    }

    private byte[] pngBytes() throws Exception {
        BufferedImage image = new BufferedImage(4, 4, BufferedImage.TYPE_INT_ARGB);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(image, "png", output);
        return output.toByteArray();
    }

    private byte[] webpBytes() {
        return new byte[]{'R', 'I', 'F', 'F', 0, 0, 0, 0, 'W', 'E', 'B', 'P', 1};
    }
}