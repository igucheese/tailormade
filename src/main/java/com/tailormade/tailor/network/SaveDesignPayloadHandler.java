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
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
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

        // 1. DesignerMenu を開いているか
        if (!(player.containerMenu instanceof DesignerMenu menu)) {
            Tailormade.LOGGER.warn(
                    "SavePattern: {} は DesignerMenu を開いていないのにパケットを送信した",
                    player.getName().getString()
            );
            return;
        }

        // 2. 指定スロットに PatternItem があるか
        int slotIndex     = packet.slotIndex();
        ItemStack stack   = menu.getPatternContainer().getItem(slotIndex);

        if (stack.isEmpty() || !(stack.getItem() instanceof PatternItem patternItem)) {
            Tailormade.LOGGER.warn(
                    "SavePattern: スロット {} に PatternItem がない",
                    slotIndex
            );
            return;
        }

        // 3. ピクセルデータのサイズが型紙の部位サイズと一致するか
        int[] incomingPixels = packet.pixelData().pixels();
        int[] expectedSize   = patternItem.getPatternType(stack).getTextureSize();
        int expectedLen      = expectedSize[0] * expectedSize[1];

        if (incomingPixels.length != expectedLen) {
            Tailormade.LOGGER.warn(
                    "SavePattern: ピクセルデータのサイズが不正 (expected={}, got={})",
                    expectedLen, incomingPixels.length
            );
            return;
        }

        String existingUuid = stack.get(ModDataComponents.PATTERN_ID.get());
        UUID uuid;
        if (existingUuid == null || existingUuid.isBlank()) {
            uuid = UUID.randomUUID();
        } else {
            uuid = UUID.fromString(existingUuid);
        }
        PatternType type = patternItem.getPatternType(stack);
        DesignDataRecord newDesign = new DesignDataRecord(
            uuid, new PixelData(incomingPixels), player.getUUID(), packet.name(), type.getType()
        );
        // デザイン保存
        DesignData.get(player.serverLevel()).addDesign(newDesign);

        // 4. DataComponent に書き込む
//        stack.set(ModDataComponents.PIXEL_DATA.get(), new PixelData(incomingPixels));
        stack.set(ModDataComponents.PATTERN_ID.get(), newDesign.uuid().toString());
        if (!packet.name().isBlank()) {
            stack.set(ModDataComponents.PATTERN_NAME.get(), packet.name());
            stack.set(DataComponents.CUSTOM_NAME, Component.literal(packet.name()));
        }

        // 5. スロット変更をクライアントに通知（インベントリ同期）
        player.containerMenu.broadcastChanges();

        // キャッシュ通知
        PacketDistributor.sendToAllPlayers(new SyncDesignPayload(uuid, newDesign));
    }
}
