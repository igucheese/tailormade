package com.tailormade.tailor.utils;

import com.google.gson.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class InitialTemplateLoader {
    private static final String RESOURCE_DIR = "/config/templates";
    private InitialTemplateLoader() {}

    // デフォルトのテンプレート集を config にコピーする
    // ※同名ファイルがある場合は上書きしない
    public static int installDefaults(Path targetDir) {
        int copied = 0;

        URL resourceUrl = InitialTemplateLoader.class.getResource(RESOURCE_DIR);
        if (resourceUrl == null) {
            System.out.println("[TailorMade] Default templates resource not found: " + RESOURCE_DIR);
            return 0;
        }

        try {
            Files.createDirectories(targetDir);

            URI uri = resourceUrl.toURI();
            if (uri.getScheme().equals("jar")) {
                copied = copyFromJar(uri, targetDir);
            } else {
                copied = copyFromDirectory(Path.of(uri), targetDir);
            }
        } catch (URISyntaxException | IOException e) {
            System.out.println("[TailorMade] Failed to install default templates: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("[TailorMade] Installed " + copied + " default template(s) into " + targetDir);
        return copied;
    }

    private static int copyFromDirectory(Path sourceDir, Path targetDir) throws IOException {
        int copied = 0;
        try (Stream<Path> files = Files.list(sourceDir)) {
            for (Path file : files.filter(p -> p.toString().endsWith(".json")).toList()) {
                Path target = targetDir.resolve(file.getFileName().toString());
                if (Files.exists(target)) continue;
                Files.copy(file, target, StandardCopyOption.COPY_ATTRIBUTES);
                copied++;
            }
        }
        return copied;
    }

    private static int copyFromJar(URI jarUri, Path targetDir) throws IOException {
        int copied = 0;

        Map<String, String> env = new HashMap<>();
        try (FileSystem fs = getOrCreateFileSystem(jarUri, env)) {
            Path sourceDir = fs.getPath(RESOURCE_DIR);
            if (!Files.isDirectory(sourceDir)) {
                return 0;
            }

            try (Stream<Path> files = Files.list(sourceDir)) {
                for (Path file : files.filter(p -> p.toString().endsWith(".json")).toList()) {
                    Path target = targetDir.resolve(file.getFileName().toString());
                    if (Files.exists(target)) continue;
                    try (InputStream in = Files.newInputStream(file)) {
                        Files.copy(in, target);
                        copied++;
                    }
                }
            }
        }

        return copied;
    }

    private static FileSystem getOrCreateFileSystem(URI jarUri, Map<String, String> env) throws IOException {
        try {
            return FileSystems.newFileSystem(jarUri, env);
        } catch (FileSystemAlreadyExistsException e) {
            return FileSystems.getFileSystem(jarUri);
        }
    }
}