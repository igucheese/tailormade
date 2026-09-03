package com.tailormade.tailor.utils.files;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.tailormade.tailor.data.TemplateRegistry;
import com.tailormade.tailor.data.records.DesignTemplate;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class TemplateLoader {
    private static final Gson GSON = new Gson();
    private TemplateLoader() {}

    public static int loadAll(Path templatesDir) {
        TemplateRegistry.clear();

        if (!Files.isDirectory(templatesDir)) {
            System.out.println("[TailorMade] Templates directory not found, skipping: " + templatesDir);
            return 0;
        }

        int loaded = 0;

        try (Stream<Path> files = Files.list(templatesDir)) {
            var jsonFiles = files.filter(p -> p.toString().endsWith(".json")).collect(Collectors.toList());

            for (Path file : jsonFiles) {
                try {
                    DesignTemplate template = loadOne(file);
                    // idはファイル名(拡張子抜き)をそのまま使う想定
                    String id = fileNameWithoutExtension(file);
                    TemplateRegistry.register(id, template);
                    loaded++;
                } catch (IOException | JsonParseException | IllegalArgumentException e) {
                    System.out.println("[TailorMade] Failed to load template: " + file + " (" + e.getMessage() + ")");
                }
            }
        } catch (IOException e) {
            System.out.println("[TailorMade] Failed to list templates directory: " + templatesDir);
            e.printStackTrace();
        }

        System.out.println("[TailorMade] Loaded " + loaded + " template(s) from " + templatesDir);
        return loaded;
    }

    private static DesignTemplate loadOne(Path file) throws IOException {
        String content = Files.readString(file, StandardCharsets.UTF_8);
        JsonObject json = GSON.fromJson(content, JsonObject.class);

        if (json == null || !json.has("name") || !json.has("type") || !json.has("pixelData")) {
            throw new IllegalArgumentException("Missing required fields (name/pixelData)");
        }

        String name = json.get("name").getAsString();
        String type = json.get("type").getAsString();
        JsonArray pixelArray = json.getAsJsonArray("pixelData");

        int[] pixels = new int[pixelArray.size()];
        for (int i = 0; i < pixels.length; i++) {
            pixels[i] = pixelArray.get(i).getAsInt();
        }

        return new DesignTemplate(name, type, pixels);
    }

    private static String fileNameWithoutExtension(Path file) {
        String fileName = file.getFileName().toString();
        int dotIndex = fileName.lastIndexOf('.');
        return dotIndex == -1 ? fileName : fileName.substring(0, dotIndex);
    }
}
