package com.puravida.modules.menu.infrastructure.storage;

import com.puravida.modules.menu.application.dto.DishImageContent;
import com.puravida.modules.menu.application.dto.StoredDishImage;
import com.puravida.modules.menu.application.port.out.DishImageStoragePort;
import com.puravida.modules.menu.domain.exception.MenuValidationException;
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
public class LocalDishImageStorageAdapter implements DishImageStoragePort {

    private static final Pattern SAFE_KEY = Pattern.compile("^[0-9a-f-]{36}\\.(png|jpg|webp)$");
    private static final long HARD_MAX_BYTES = 2_097_152L;
    private static final int MAX_DIMENSION = 4_096;
    private static final long MAX_PIXELS = 16_777_216L;

    private final Path root;
    private final long maxBytes;

    public LocalDishImageStorageAdapter(DishImageStorageProperties properties) {
        if (properties.getRoot() == null || properties.getRoot().isBlank()) {
            throw new IllegalStateException("El directorio de almacenamiento de platillos es obligatorio.");
        }
        Path configuredRoot = Path.of(properties.getRoot()).toAbsolutePath().normalize();
        if (configuredRoot.getParent() == null) {
            throw new IllegalStateException("Las imagenes de platillos no pueden almacenarse en la raiz del sistema de archivos.");
        }
        this.root = configuredRoot;
        if (properties.getMaxBytes() <= 0) {
            throw new IllegalStateException("El limite de almacenamiento de platillos debe ser positivo.");
        }
        this.maxBytes = Math.min(properties.getMaxBytes(), HARD_MAX_BYTES);
    }

    @Override
    public StoredDishImage store(byte[] content, String declaredMediaType) {
        validateSize(content);
        ImageDescriptor descriptor = validateImage(content, declaredMediaType);
        String key = UUID.randomUUID() + "." + descriptor.extension();
        Path target = resolveSafe(key);
        Path temporary = null;
        try {
            Files.createDirectories(root);
            temporary = Files.createTempFile(root, "dish-", ".tmp");
            Files.write(temporary, content);
            moveAtomically(temporary, target);
            return new StoredDishImage(key, descriptor.mediaType(), content.length);
        } catch (IOException exception) {
            deleteQuietly(temporary);
            throw new IllegalStateException("No se pudo guardar la imagen del platillo.", exception);
        }
    }

    @Override
    public DishImageContent load(String key) {
        Path path = resolveSafe(key);
        if (!Files.isRegularFile(path)) {
            throw new NotFoundException("La imagen del platillo no esta disponible.");
        }
        try {
            byte[] content = Files.readAllBytes(path);
            validateSize(content);
            return new DishImageContent(content, mediaTypeFromKey(key), sha256(content));
        } catch (IOException exception) {
            throw new IllegalStateException("No se pudo leer la imagen del platillo.", exception);
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
            throw new IllegalStateException("No se pudo eliminar una imagen de platillo reemplazada.", exception);
        }
    }

    private ImageDescriptor validateImage(byte[] content, String declaredMediaType) {
        String declared = normalizeMediaType(declaredMediaType);
        if (!declared.equals("image/png") && !declared.equals("image/jpeg") && !declared.equals("image/webp")) {
            throw new MenuValidationException("La imagen debe ser PNG, JPEG o WebP.");
        }
        if (declared.equals("image/webp")) {
            if (!hasWebpHeader(content)) {
                throw invalidImage();
            }
            return new ImageDescriptor("image/webp", "webp");
        }
        return validateRasterImage(content, declared);
    }

    private ImageDescriptor validateRasterImage(byte[] content, String declared) {
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
                    throw new MenuValidationException("Las dimensiones de la imagen no son permitidas.");
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

    private boolean hasWebpHeader(byte[] content) {
        return content.length >= 12
                && content[0] == 'R'
                && content[1] == 'I'
                && content[2] == 'F'
                && content[3] == 'F'
                && content[8] == 'W'
                && content[9] == 'E'
                && content[10] == 'B'
                && content[11] == 'P';
    }

    private String mediaTypeFromKey(String key) {
        if (key.endsWith(".png")) {
            return "image/png";
        }
        if (key.endsWith(".webp")) {
            return "image/webp";
        }
        return "image/jpeg";
    }

    private String normalizeMediaType(String mediaType) {
        if (mediaType == null) {
            return "";
        }
        int separator = mediaType.indexOf(';');
        String normalized = separator >= 0 ? mediaType.substring(0, separator) : mediaType;
        return normalized.trim().toLowerCase(Locale.ROOT);
    }

    private MenuValidationException invalidImage() {
        return new MenuValidationException("El archivo no contiene una imagen valida.");
    }

    private void validateSize(byte[] content) {
        if (content == null || content.length == 0) {
            throw new MenuValidationException("El archivo de imagen esta vacio.");
        }
        if (content.length > maxBytes) {
            throw new MenuValidationException("La imagen no debe exceder 2 MiB.");
        }
    }

    private Path resolveSafe(String key) {
        if (key == null || !SAFE_KEY.matcher(key).matches()) {
            throw new NotFoundException("La imagen del platillo no esta disponible.");
        }
        Path resolved = root.resolve(key).normalize();
        if (!resolved.startsWith(root)) {
            throw new NotFoundException("La imagen del platillo no esta disponible.");
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