package com.tailormade.tailor.utils;

import com.tailormade.tailor.Tailormade;
import com.tailormade.tailor.data.DesignData;
import com.tailormade.tailor.data.DesignDataRecord;
import com.tailormade.tailor.data.DesignDataVersion;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static net.minecraft.world.level.Level.OVERWORLD;

public class OldDataMerger {
    public static int countMergeRequiredDesigns(MinecraftServer server) {
        int counter = 0;
        Iterable<ServerLevel> allLevels = server.getAllLevels();
        for (ServerLevel level: allLevels) {
            if (level.dimension().equals(OVERWORLD)) {
                continue;
            }
            Collection<DesignDataRecord> records = DesignData.getByLevel(level).index();
            counter += records.size();
        }
        return counter;
    }

    public static int mergeAllDimensionsData(MinecraftServer server) {
        int processed = 0;
        int overlapped = 0;

        Iterable<ServerLevel> allLevels = server.getAllLevels();
        ServerLevel overworld = server.getLevel(OVERWORLD);
        if (overworld == null) { return processed; }

        for (ServerLevel level: allLevels) {
            if (level.dimension().equals(OVERWORLD)) {
                continue;
            }
            // オーバーワールド以外で保存されていたデータをオーバーワールドにマージする
            Collection<DesignDataRecord> records = DesignData.getByLevel(level).index();
            for (DesignDataRecord design: records) {
                // 念の為重複チェック
                DesignDataRecord checkRecord = DesignData.getByLevel(overworld).get(design.uuid());
                if (checkRecord == null) {
                    DesignData.getByLevel(overworld).addDesign(design);
                    processed++;
                } else {
                    overlapped++;
                }
            }
        }

        DesignData.get(overworld).setVersion(DesignDataVersion.OLD_DATA_MERGED.getVersion());


        Tailormade.LOGGER.info("重複データが{}件ありました", overlapped);
        return processed;
    }

    public static int cleanUp(MinecraftServer server) {
        int processed = 0;

        // 古いデータのマージ作業前なら弾く
        ServerLevel overworld = server.getLevel(OVERWORLD);
        if (overworld == null) return processed;
        int current = DesignData.get(overworld).version();
        if (current < DesignDataVersion.OLD_DATA_MERGED.getVersion()) {
            String message = Component.translatable("command.tailormade.updateAllData.clean_up_interrupted").getString();
            Tailormade.LOGGER.error(message);
            return 0;
        }

        Iterable<ServerLevel> allLevels = server.getAllLevels();
        for (ServerLevel level: allLevels) {
            if (level.dimension().equals(OVERWORLD)) {
                continue;
            }
            Collection<DesignDataRecord> records = DesignData.getByLevel(level).index();
            List<UUID> toDelete = records.stream().map(DesignDataRecord::uuid).toList();
            for (UUID uuid : toDelete) {
                DesignData.getByLevel(level).deleteDesign(uuid);
                processed++;
            }
        }
        return processed;
    }
}
