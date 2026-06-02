package com.tailormade.tailor.network;

import com.tailormade.tailor.Tailormade;
import com.tailormade.tailor.client.menu.BleachMenu;
import com.tailormade.tailor.network.payloads.ConfirmBleachPayload;
import com.tailormade.tailor.registries.ModDataComponents;
import com.tailormade.tailor.registries.ModItems;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ConfirmBleachPayloadHandler {
    public static void handle(ConfirmBleachPayload packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> handleOnMainThread(packet, ctx));
    }

    private static void handleOnMainThread(ConfirmBleachPayload packet, IPayloadContext ctx) {
        if (!(ctx.player() instanceof ServerPlayer player)) return;
        if (!(player.containerMenu instanceof BleachMenu menu)) {
            logWarn(player, "BleachMenu を開いていない");
            return;
        }

        ItemStack armorStack = menu.getSlot(BleachMenu.SLOT_ARMOR).getItem();
        if (armorStack.isEmpty() || !(armorStack.getItem() instanceof ArmorItem armorItem)) {
            logWarn(player, "防具スロットに ArmorItem がない");
            return;
        }

        ItemStack bleachStack = menu.getSlot(BleachMenu.SLOT_BLEACH).getItem();
        if (bleachStack.isEmpty() || !bleachStack.is(ModItems.BLEACH.get())) {
            logWarn(player, "漂白剤がない");
            return;
        }

        // 防具からデータ削除
        armorStack.remove(ModDataComponents.PATTERN_ID.get());
        armorStack.remove(DataComponents.CUSTOM_NAME);
        armorStack.remove(DataComponents.LORE);

        // 漂白剤を消費
        bleachStack.shrink(1);
        SimpleContainer container = menu.getSlotContainer();
        container.setItem(BleachMenu.SLOT_BLEACH, bleachStack.isEmpty() ? ItemStack.EMPTY : bleachStack);

        // できあがった防具をインベに入れる
        menu.getSlot(BleachMenu.SLOT_ARMOR).set(ItemStack.EMPTY);
        if (!player.getInventory().add(armorStack)) {
            player.drop(armorStack, false);
        }
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0F, 1.0F);

        // 同期
        player.containerMenu.broadcastChanges();
    }

    private static void logWarn(ServerPlayer player, String reason) {
        Tailormade.LOGGER.warn(
                "ConfirmTailor: {} のリクエストを却下 ({})", player.getName().getString(), reason
        );
    }
}
