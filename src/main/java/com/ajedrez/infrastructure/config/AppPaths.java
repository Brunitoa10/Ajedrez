package com.ajedrez.infrastructure.config;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Resuelve dónde vive la aplicación para que sea PORTABLE: la base de datos y la configuración
 * se guardan junto al programa (por ejemplo, en el pendrive), no en el perfil del usuario.
 * <ol>
 *     <li>Si se pasa {@code -Dajedrez.home=...}, se usa esa carpeta.</li>
 *     <li>Si se ejecuta desde un .jar, la carpeta del jar (o la superior si es la carpeta "app" de jpackage).</li>
 *     <li>Si esa carpeta no tiene permisos de escritura, {@code ~/.ajedrez}.</li>
 * </ol>
 */
public record AppPaths(Path home) {

    private static final String HOME_PROPERTY = "ajedrez.home";
    private static final String JPACKAGE_APP_DIR = "app";

    public static AppPaths detect() {
        String override = System.getProperty(HOME_PROPERTY);
        if (override != null && !override.isBlank()) {
            return new AppPaths(Paths.get(override).toAbsolutePath());
        }
        Path candidate = installationDirectory();
        if (!isWritable(candidate)) {
            candidate = Paths.get(System.getProperty("user.home"), ".ajedrez");
        }
        return new AppPaths(candidate.toAbsolutePath());
    }

    public Path dataDir() {
        return ensureExists(home.resolve("data"));
    }

    public Path configDir() {
        return ensureExists(home.resolve("config"));
    }

    private static Path installationDirectory() {
        try {
            Path location = Paths.get(AppPaths.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            if (Files.isRegularFile(location) && location.toString().endsWith(".jar")) {
                Path dir = location.getParent();
                if (dir.getFileName() != null && JPACKAGE_APP_DIR.equalsIgnoreCase(dir.getFileName().toString())) {
                    dir = dir.getParent();
                }
                return dir;
            }
        } catch (URISyntaxException | SecurityException | NullPointerException e) {
            // Sin ubicación conocida: usamos el directorio de trabajo.
        }
        return Paths.get(System.getProperty("user.dir"));
    }

    private static boolean isWritable(Path dir) {
        try {
            Files.createDirectories(dir);
            Path probe = Files.createTempFile(dir, ".probe", ".tmp");
            Files.deleteIfExists(probe);
            return true;
        } catch (IOException | SecurityException e) {
            return false;
        }
    }

    private static Path ensureExists(Path dir) {
        try {
            return Files.createDirectories(dir);
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo crear la carpeta " + dir, e);
        }
    }
}
