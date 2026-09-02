package com.tailormade.tailor.utils.files;

import com.tailormade.tailor.data.DesignDataRecord;
import com.tailormade.tailor.network.payloads.ExportDesignDataToClientPayload;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

public final class DesignDataExportSender {
    private DesignDataExportSender() {}

    public static void sendToClient(ServerPlayer player, DesignDataRecord record) {
        CompoundTag nbt = DesignDataNbtExporter.toNbt(record);
        String fileName = sanitizeFileName(record.name()) + " (" + record.type() + ")";
        PacketDistributor.sendToPlayer(player, new ExportDesignDataToClientPayload(fileName, nbt));
    }

    private static String sanitizeFileName(String rawName) {
        String sanitized = rawName.replaceAll("[\\\\/:*?\"<>|]", "_").trim();
        return sanitized.isEmpty() ? "unnamed" : sanitized;
    }
}
