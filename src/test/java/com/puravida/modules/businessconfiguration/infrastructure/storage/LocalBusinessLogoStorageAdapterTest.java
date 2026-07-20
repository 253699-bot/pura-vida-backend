package com.puravida.modules.businessconfiguration.infrastructure.storage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.puravida.modules.businessconfiguration.domain.exception.BusinessConfigurationValidationException;
import com.puravida.shared.domain.exception.NotFoundException;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class LocalBusinessLogoStorageAdapterTest {

    @TempDir
    Path tempDir;

    private LocalBusinessLogoStorageAdapter adapter;

    @BeforeEach
    void setUp() {
        BusinessLogoStorageProperties properties = new BusinessLogoStorageProperties();
        properties.setRoot(tempDir.toString());
        properties.setMaxBytes(2_097_152L);
        adapter = new LocalBusinessLogoStorageAdapter(properties);
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
    void rejectsSpoofedOrOversizedContentAndUnsafeKeys() {
        assertThatThrownBy(() -> adapter.store("not-an-image".getBytes(), "image/png"))
                .isInstanceOf(BusinessConfigurationValidationException.class);
        assertThatThrownBy(() -> adapter.store(new byte[2_097_153], "image/png"))
                .isInstanceOf(BusinessConfigurationValidationException.class);
        assertThatThrownBy(() -> adapter.load("../secret.png"))
                .isInstanceOf(NotFoundException.class);
    }

    private byte[] pngBytes() throws Exception {
        BufferedImage image = new BufferedImage(4, 4, BufferedImage.TYPE_INT_ARGB);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(image, "png", output);
        return output.toByteArray();
    }
}
