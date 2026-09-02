package com.tailormade.tailor.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class MojangApiAccessor {
    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();
    public static CompletableFuture<String> fetchUsernameFromUUID(UUID uuid) {
        String uuidStr = uuid.toString().replace("-", "");
        String url = "https://sessionserver.mojang.com/session/minecraft/profile/" + uuidStr;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        return HTTP_CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 200) {
                        JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
                        return json.get("name").getAsString();
                    } else if (response.statusCode() == 204) {
                        return null;
                    } else {
                        System.err.println("Mojang API error: " + response.statusCode());
                        return null;
                    }
                })
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    return null;
                });
    }
}
