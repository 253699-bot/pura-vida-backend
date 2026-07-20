package com.puravida.modules.businessconfiguration.infrastructure.storage;

import com.puravida.modules.businessconfiguration.application.dto.BusinessLogoContent;
import com.puravida.modules.businessconfiguration.application.dto.StoredBusinessLogo;
import com.puravida.modules.businessconfiguration.application.port.out.BusinessLogoStoragePort;
import com.puravida.modules.businessconfiguration.domain.exception.BusinessConfigurationValidationException;
import com.puravida.shared.domain.exception.NotFoundException;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import org.springframework.stereotype.Component;

@Component
public class LocalBusinessLogoStorageAdapter implements BusinessLogoStoragePort {

    private static final Pattern SAFE_KEY = Pattern.compile("^[0-9a-f-]{36}\\.(png|jpg)$");
    private static final long HARD_MAX_BYTES = 2_097_152L;
    private static final int MAX_DIMENSION = 4_096;
    private static final long MAX_PIXELS = 16_777_216L;
    private final Path root;
    private final long maxBytes;

    public LocalBusinessLogoStorageAdapter(BusinessLogoStorageProperties properties) {
        if (properties.getRoot() == null || properties.getRoot().isBlank()) {
            throw new IllegalStateException("El directorio de almacenamiento del logo es obligatorio.");
        }
        Path configuredRoot = Path.of(properties.getRoot()).toAbsolutePath().normalize();
        if (configuredRoot.getParent() == null) {
            throw new IllegalStateException("El logo no puede almacenarse en la raiz del sistema de archivos.");
        }
        this.root = configuredRoot;
        if (properties.getMaxBytes() <= 0) {
            throw new IllegalStateException("El limite de almacenamiento del logo debe ser positivo.");
        }
        this.maxBytes = Math.min(properties.getMaxBytes(), HARD_MAX_BYTES);
    }

    @Override
    public StoredBusinessLogo store(byte[] content, String declaredMediaType) {
        validateSize(content);
        ImageDescriptor descriptor = validateImage(content, declaredMediaType);
        String key = UUID.randomUUID() + "." + descriptor.extension();
        Path target = resolveSafe(key);
        Path temporary = null;
        try {
            Files.createDirectories(root);
            temporary = Files.createTempFile(root, "logo-", ".tmp");
            Files.write(temporary, content);
            moveAtomically(temporary, target);
            return new StoredBusinessLogo(key, descriptor.mediaType(), content.length);
        } catch (IOException exception) {
            deleteQuietly(temporary);
            throw new IllegalStateException("No se pudo guardar el logo del negocio.", exception);
        }
    }

    @Override
    public BusinessLogoContent load(String key) {
        Path path = resolveSafe(key);
        if (!Files.isRegularFile(path)) {
            throw new NotFoundException("El logo del negocio no esta disponible.");
        }
        try {
            byte[] content = Files.readAllBytes(path);
            validateSize(content);
            String mediaType = key.endsWith(".png") ? "image/png" : "image/jpeg";
            return new BusinessLogoContent(content, mediaType, sha256(content));
        } catch (IOException exception) {
            throw new IllegalStateException("No se pudo leer el logo del negocio.", exception);
        }
    }

    @Override
    public void delete(String key) {
        if (key == null || key.isBlank()) {
            return;
        }
        try {
            Files.deleteIfExists(resolveSafe(key));
        } catch (IOException exception) {
            throw new IllegalStateException("No se pudo eliminar un logo reemplazado.", exception);
        }
    }

    private ImageDescriptor validateImage(byte[] content, String declaredMediaType) {
        String declared = normalizeMediaType(declaredMediaType);
        if (!declared.equals("image/png") && !declared.equals("image/jpeg")) {
            throw new BusinessConfigurationValidationException("El logo debe ser PNG o JPEG.");
        }
        try (ImageInputStream input = ImageIO.createImageInputStream(new ByteArrayInputStream(content))) {
            if (input == null) {
                throw invalidImage();
            }
            Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
            if (!readers.hasNext()) {
                throw invalidImage();
            }
            ImageReader reader = readers.next();
            try {
                reader.setInput(input, true, true);
                String format = reader.getFormatName().toLowerCase(Locale.ROOT);
                String actual = format.equals("png") ? "image/png"
                        : (format.equals("jpeg") || format.equals("jpg") ? "image/jpeg" : null);
                if (actual == null || !actual.equals(declared)) {
                    throw invalidImage();
                }
                int width = reader.getWidth(0);
                int height = reader.getHeight(0);
                if (width <= 0 || height <= 0 || width > MAX_DIMENSION || height > MAX_DIMENSION
                        || (long) width * height > MAX_PIXELS) {
                    throw new BusinessConfigurationValidationException("Las dimensiones del logo no son permitidas.");
                }
                BufferedImage decoded = reader.read(0);
                if (decoded == null) {
                    throw invalidImage();
                }
                return new ImageDescriptor(actual, actual.equals("image/png") ? "png" : "jpg");
            } finally {
                reader.dispose();
            }
        } catch (IOException exception) {
            throw invalidImage();
        }
    }

    private String normalizeMediaType(String mediaType) {
        if (mediaType == null) {
            return "";
        }
        int separator = mediaType.indexOf(';');
        String normalized = separator >= 0 ? mediaType.substring(0, separator) : mediaType;
        return normalized.trim().toLowerCase(Locale.ROOT);
    }

    private BusinessConfigurationValidationException invalidImage() {
        return new BusinessConfigurationValidationException(
                "El archivo no contiene una imagen PNG o JPEG valida."
        );
    }

    private void validateSize(byte[] content) {
        if (content == null || content.length == 0) {
            throw new BusinessConfigurationValidationException("El archivo de logo esta vacio.");
        }
        if (content.length > maxBytes) {
            throw new BusinessConfigurationValidationException("El logo no debe exceder 2 MiB.");
        }
    }

    private Path resolveSafe(String key) {
        if (key == null || !SAFE_KEY.matcher(key).matches()) {
            throw new NotFoundException("El logo del negocio no esta disponible.");
        }
        Path resolved = root.resolve(key).normalize();
        if (!resolved.startsWith(root)) {
            throw new NotFoundException("El logo del negocio no esta disponible.");
        }
        return resolved;
    }

    private void moveAtomically(Path source, Path target) throws IOException {
        try {
            Files.move(source, target, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException exception) {
            Files.move(source, target);
        }
    }

    private String sha256(byte[] content) {
        try {
            return java.util.HexFormat.of().formatHex(
                    MessageDigest.getInstance("SHA-256").digest(content)
            );
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 no esta disponible.", exception);
        }
    }

    private void deleteQuietly(Path path) {
        if (path != null) {
            try {
                Files.deleteIfExists(path);
            } catch (IOException ignored) {
                // La limpieza temporal no debe ocultar el fallo original.
            }
        }
    }

    private record ImageDescriptor(String mediaType, String extension) {
    }
}
