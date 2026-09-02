package com.tailormade.tailor.network.payloads;

import com.tailormade.tailor.utils.ChatService;
import com.tailormade.tailor.utils.SoundService;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.tailormade.tailor.Tailormade.MODID;

public record ExportDesignDataToClientPayload(String fileName, CompoundTag nbt) implements CustomPacketPayload {
    public static final Type<ExportDesignDataToClientPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(MODID, "export_design_data"));

    public static final StreamCodec<ByteBuf, ExportDesignDataToClientPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, ExportDesignDataToClientPayload::fileName,
            ByteBufCodecs.COMPOUND_TAG, ExportDesignDataToClientPayload::nbt,
            ExportDesignDataToClientPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleClient(ExportDesignDataToClientPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            writeToFile(payload);
            SoundService.playSound(context.player(), SoundEvents.EXPERIENCE_ORB_PICKUP, 1.0F);
        }).exceptionally(e -> {
            e.printStackTrace();
            return null;
        });
    }

    private static void writeToFile(ExportDesignDataToClientPayload payload) {
        try {
            Path exportDir = Minecraft.getInstance().gameDirectory.toPath().resolve("tailormade_exports");
            Files.createDirectories(exportDir);

            Path targetFile = exportDir.resolve(payload.fileName() + ".nbt");
            NbtIo.write(payload.nbt(), targetFile);

            System.out.println("[TailorMade] Exported design data to: " + targetFile);
        } catch (IOException e) {
            System.out.println("[TailorMade] Failed to export design data: " + payload.fileName());
            e.printStackTrace();
        }
    }
}
