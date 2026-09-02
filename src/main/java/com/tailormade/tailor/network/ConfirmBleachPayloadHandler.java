package com.tailormade.tailor.network;

import com.tailormade.tailor.Tailormade;
import com.tailormade.tailor.client.menu.BleachMenu;
import com.tailormade.tailor.network.payloads.ConfirmBleachPayload;
import com.tailormade.tailor.registries.ModDataComponents;
import com.tailormade.tailor.registries.ModItems;
import com.tailormade.tailor.utils.ChatService;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
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
            logWarn(player, "BleachMenu が開かれていません！");
            return;
        }

        ItemStack armorStack = menu.getSlot(BleachMenu.SLOT_ARMOR).getItem();
        if (armorStack.isEmpty() || !(armorStack.getItem() instanceof ArmorItem armorItem)) {
            logWarn(player, "防具スロットに ArmorItem がありません！");
            ChatService.showMessage(player, Component.translatable("message.tailormade.pattern_manager.armor_blank"), true);
            return;
        }

        ItemStack bleachStack = menu.getSlot(BleachMenu.SLOT_BLEACH).getItem();
        if (bleachStack.isEmpty() || !bleachStack.is(ModItems.BLEACH.get())) {
            logWarn(player, "漂白剤がありません！");
            ChatService.showMessage(player, Component.translatable("message.tailormade.pattern_manager.no_bleach"), true);
            return;
        }

        // 漂白するだけなら別にデザインのロック状態は関係ないので
        // アクセス権チェックは行わない

        armorStack.remove(ModDataComponents.PATTERN_ID.get());
        armorStack.remove(DataComponents.CUSTOM_NAME);
        armorStack.remove(DataComponents.LORE);

        bleachStack.shrink(1);
        SimpleContainer container = menu.getSlotContainer();
        container.setItem(BleachMenu.SLOT_BLEACH, bleachStack.isEmpty() ? ItemStack.EMPTY : bleachStack);

        menu.getSlot(BleachMenu.SLOT_ARMOR).set(ItemStack.EMPTY);
        if (!player.getInventory().add(armorStack)) {
            player.drop(armorStack, false);
        }
        ChatService.showMessage(player, Component.translatable("message.tailormade.pattern_manager.bleached"), true);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0F, 1.0F);

        player.containerMenu.broadcastChanges();
    }

    private static void logWarn(ServerPlayer player, String reason) {
        Tailormade.LOGGER.warn(
                "ConfirmTailor: {} を処理できませんでした - {}", player.getName().getString(), reason
        );
    }
}
