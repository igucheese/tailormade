package com.tailormade.tailor.utils;

import com.tailormade.tailor.Tailormade;
import com.tailormade.tailor.data.DesignData;
import com.tailormade.tailor.data.DesignDataRecord;
import com.tailormade.tailor.data.PatternType;
import com.tailormade.tailor.data.PixelData;
import com.tailormade.tailor.entities.items.PatternItem;
import com.tailormade.tailor.network.payloads.SyncDesignPayload;
import com.tailormade.tailor.registries.ModDataComponents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.UUID;

public class PatternDataSaver {
    public static boolean isValidSize(ItemStack stack, PixelData pixelData, PatternItem patternItem) {
        int[] incomingPixels = pixelData.pixels();
        int[] expectedSize = patternItem.getPatternType(stack).getTextureSize();
        int expectedLen = expectedSize[0] * expectedSize[1];
        if (incomingPixels.length != expectedLen) {
            Tailormade.LOGGER.warn(
                    "SavePattern: ピクセルデータのサイズが不正です (期待値={}, 実際の値={})",
                    expectedLen, incomingPixels.length
            );
            return false;
        }
        return true;
    }

    public static DesignDataRecord saveDeign(ServerLevel level, ItemStack stack, PixelData pixelData, PatternItem patternItem, UUID designer, String patternName) {
        return saveDeign(level, stack, pixelData, patternItem, designer, patternName, designer);
    }

    public static DesignDataRecord saveDeign(ServerLevel level, ItemStack stack, PixelData pixelData, PatternItem patternItem, UUID designer, String patternName, UUID originalDesigner) {
        try {
            int[] incomingPixels = pixelData.pixels();

            String existingUuid = stack.get(ModDataComponents.PATTERN_ID.get());
            UUID uuid;
            if (existingUuid == null || existingUuid.isBlank()) {
                uuid = UUID.randomUUID();
            } else {
                uuid = UUID.fromString(existingUuid);
            }
            PatternType type = patternItem.getPatternType(stack);
            DesignDataRecord newDesign = new DesignDataRecord(
                    uuid, new PixelData(incomingPixels), designer, patternName, type.getType(), false, originalDesigner
            );
            DesignData.get(level).addDesign(newDesign);

            stack.set(ModDataComponents.PATTERN_ID.get(), newDesign.uuid().toString());
            if (!patternName.isBlank()) {
                stack.set(ModDataComponents.PATTERN_NAME.get(), patternName);
                stack.set(DataComponents.CUSTOM_NAME, Component.literal(patternName));
            }

            return newDesign;
        } catch (Exception e) {
            Tailormade.LOGGER.error("[PatternDataSaver.saveDesign] エラーが発生しました: {}", e.getMessage());
            return null;
        }
    }
}
