package com.tailormade.tailor.utils.files;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

@OnlyIn(Dist.CLIENT)
public final class NativeFileChooser {
    private NativeFileChooser() {}

    private static final ExecutorService DIALOG_EXECUTOR =
            Executors.newSingleThreadExecutor(r -> {
                Thread t = new Thread(r, "tailormade-file-dialog");
                t.setDaemon(true);
                return t;
            });

    public static void openNbtFileDialogAsync(Consumer<Path> callback) {
        CompletableFuture
                .supplyAsync(NativeFileChooser::openNbtFileDialogBlocking, DIALOG_EXECUTOR)
                .whenComplete((path, throwable) -> {
                    Minecraft.getInstance().execute(() -> {
                        if (throwable != null) {
                            System.out.println("[TailorMade] File dialog failed: " + throwable.getMessage());
                            throwable.printStackTrace();
                            callback.accept(null);
                        } else {
                            callback.accept(path);
                        }
                    });
                });
    }

    private static Path openNbtFileDialogBlocking() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            PointerBuffer filters = stack.mallocPointer(1);
            filters.put(stack.UTF8("*.nbt"));
            filters.flip();

            String result = TinyFileDialogs.tinyfd_openFileDialog(
                    Component.translatable("message.tailormade.pattern_manager.open_exported_file").getString(),
                    System.getProperty("user.home") + "/",
                    filters,
                    Component.translatable("message.tailormade.pattern_manager.open_exported_file.filter").getString(),
                    false
            );

            return result == null ? null : Path.of(result);
        }
    }
}
