package com.tailormade.tailor.network;

import com.tailormade.tailor.Tailormade;
import com.tailormade.tailor.client.menu.DesignerMenu;
import com.tailormade.tailor.data.DesignData;
import com.tailormade.tailor.data.DesignDataRecord;
import com.tailormade.tailor.data.PatternType;
import com.tailormade.tailor.data.PixelData;
import com.tailormade.tailor.entities.items.PatternItem;
import com.tailormade.tailor.network.payloads.SaveDesignPayload;
import com.tailormade.tailor.network.payloads.SyncDesignPayload;
import com.tailormade.tailor.registries.ModDataComponents;
import com.tailormade.tailor.utils.PatternDataSaver;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Objects;
import java.util.UUID;

public class SaveDesignPayloadHandler {
    public static void handle(SaveDesignPayload packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> handleOnMainThread(packet, ctx));
    }

    private static void handleOnMainThread(SaveDesignPayload packet, IPayloadContext ctx) {
        if (!(ctx.player() instanceof ServerPlayer player)) return;
        ServerLevel level = player.serverLevel();

        if (!(player.containerMenu instanceof DesignerMenu menu)) {
            Tailormade.LOGGER.warn(
                    "SavePattern: DesignerMenu を開いていません"
            );
            return;
        }

        int slotIndex = packet.slotIndex();
        ItemStack stack = menu.getPatternContainer().getItem(slotIndex);
        if (stack.isEmpty() || !(stack.getItem() instanceof PatternItem patternItem)) {
            Tailormade.LOGGER.warn(
                    "SavePattern: スロット {} に PatternItem がありません",
                    slotIndex
            );
            return;
        }
        // 既存データチェック
        if (stack.has(ModDataComponents.PATTERN_ID.get())) {
            String patternId = stack.get(ModDataComponents.PATTERN_ID.get());
            DesignDataRecord savedDesign = DesignData.get(level).get(UUID.fromString(patternId));
            if (savedDesign != null && savedDesign.isLocked() && !savedDesign.userId().equals(player.getUUID())) {
                Tailormade.LOGGER.warn("SavePattern: ロックされた型紙を編集しようとしています");
                return;
            }
        }

        boolean isValidSize = PatternDataSaver.isValidSize(stack, packet.pixelData(), patternItem);
        if (!isValidSize) { return; }

        // 保存実行
        DesignDataRecord newDesign = PatternDataSaver.saveDeign(level, stack, packet.pixelData(), patternItem, player.getUUID(), packet.name());
        if (newDesign != null) {
            player.containerMenu.broadcastChanges();
            PacketDistributor.sendToAllPlayers(new SyncDesignPayload(newDesign.uuid(), newDesign));
        }
    }
}
