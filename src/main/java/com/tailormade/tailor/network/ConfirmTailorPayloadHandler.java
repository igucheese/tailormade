package com.tailormade.tailor.network;

import com.tailormade.tailor.Tailormade;
import com.tailormade.tailor.client.menu.TailorMenu;
import com.tailormade.tailor.data.DesignData;
import com.tailormade.tailor.data.DesignDataRecord;
import com.tailormade.tailor.data.PatternType;
import com.tailormade.tailor.data.PixelData;
import com.tailormade.tailor.entities.blockentities.TailorBlockEntity;
import com.tailormade.tailor.entities.items.PatternItem;
import com.tailormade.tailor.network.payloads.ConfirmTailorPayload;
import com.tailormade.tailor.registries.ModDataComponents;
import com.tailormade.tailor.utils.ChatService;
import com.tailormade.tailor.utils.DyeCostCalculator;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ConfirmTailorPayloadHandler {
    public static void handle(ConfirmTailorPayload packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> handleOnMainThread(packet, ctx));
    }

    private static void handleOnMainThread(ConfirmTailorPayload packet, IPayloadContext ctx) {
        if (!(ctx.player() instanceof ServerPlayer player)) return;

        if (!(player.containerMenu instanceof TailorMenu menu)) {
            logWarn(player, "TailorMenu が開かれていません！");
            return;
        }

        TailorBlockEntity be = menu.getBlockEntity();
        if (be == null || be.getLevel() == null || be.getLevel().isClientSide()) {
            BlockPos pos = menu.getBlockPos();
            if (pos == null) { logWarn(player, "BlockPos が不明です。"); return; }
            if (!(player.serverLevel().getBlockEntity(pos) instanceof TailorBlockEntity serverBe)) {
                logWarn(player, "サーバー側 BlockEntity が見つかりません！");
                return;
            }
            be = serverBe;
        }

        ItemStack patternStack = menu.getSlot(TailorMenu.SLOT_PATTERN).getItem();
        if (patternStack.isEmpty() || !(patternStack.getItem() instanceof PatternItem patternItem)) {
            logWarn(player, "型紙スロットに PatternItem がありません！");
            ChatService.showMessage(player, Component.translatable("message.tailormade.pattern_manager.incompatible"), true);
            return;
        }

        boolean hasPixelData = true;
        PixelData pixelData = null;
        String id = patternStack.get(ModDataComponents.PATTERN_ID.get());
        if (id == null) {
            hasPixelData = false;
        } else {
            DesignDataRecord design = DesignData.get(player.serverLevel()).get(UUID.fromString(id));
            pixelData = design != null ? design.pixelData() : null;
            if (design != null && design.isLocked() && !design.userId().equals(player.getUUID())) {
                logWarn(player, "使用できない型紙です");
                ChatService.showMessage(player, Component.translatable("message.tailormade.pattern_manager.guarded"), true);
                return;
            }
        }
        if (!hasPixelData || pixelData == null) {
            logWarn(player, "型紙に PIXEL_DATA がありません！");
            ChatService.showMessage(player, Component.translatable("message.tailormade.pattern_manager.no_pixel_data"), true);
            return;
        }

        ItemStack armorStack = menu.getSlot(TailorMenu.SLOT_ARMOR).getItem();
        ItemEnchantments existingEnchantments = armorStack.get(DataComponents.ENCHANTMENTS);
        if (armorStack.isEmpty()) {
            logWarn(player, "防具スロットが空です。");
            ChatService.showMessage(player, Component.translatable("message.tailormade.pattern_manager.armor_blank"), true);
            return;
        }

        DyeCostCalculator.DyeCost cost = DyeCostCalculator.calculate(pixelData.pixels());

        if (!cost.canAfford(be.getTankR(), be.getTankG(), be.getTankB())) {
            logWarn(player, "タンク残量が不足しています: 必要量=(%d,%d,%d) 残存量=(%d,%d,%d)".formatted(
                    cost.red(), cost.green(), cost.blue(),
                    be.getTankR(), be.getTankG(), be.getTankB()
            ));
            ChatService.showMessage(player, Component.translatable("message.tailormade.pattern_manager.insufficient_inks"), true);
            return;
        }

        if (!be.consumeDye(cost.red(), cost.green(), cost.blue())) {
            logWarn(player, "consumeDye に失敗しました");
            ChatService.showMessage(player, Component.translatable("message.tailormade.pattern_manager.cosume_ink_failed"), true);
            return;
        }

        armorStack.set(ModDataComponents.PATTERN_ID.get(), id);
        String patternName = patternStack.get(ModDataComponents.PATTERN_NAME.get());
        String thisName = packet.name();
        String thisSerial = packet.serial();
        String tailorName = packet.tailorName();
        if ((patternName == null || patternName.isBlank()) && (thisName == null || thisName.isBlank())) {
            PatternType type = patternItem.getPatternType(patternStack);
            Component customName = Component.translatable("item.tailormade.tailored.item").withStyle(ChatFormatting.GOLD);
            switch (type) {
                case HEAD -> customName = Component.translatable("item.tailormade.tailored.helmet").withStyle(ChatFormatting.GOLD);
                case CHEST -> customName = Component.translatable("item.tailormade.tailored.chestplate").withStyle(ChatFormatting.GOLD);
                case LEGS -> customName = Component.translatable("item.tailormade.tailored.leggings").withStyle(ChatFormatting.GOLD);
                case FEET -> customName = Component.translatable("item.tailormade.tailored.boots").withStyle(ChatFormatting.GOLD);
            }
            armorStack.set(DataComponents.CUSTOM_NAME, customName);
        } else {
            if (!thisName.isBlank()) {
                armorStack.set(DataComponents.CUSTOM_NAME, Component.literal(thisName).withStyle(ChatFormatting.GOLD));
            } else {
                armorStack.set(DataComponents.CUSTOM_NAME, Component.literal(patternName).withStyle(ChatFormatting.GOLD));
            }
        }
        List<Component> flavorTexts = new ArrayList<>();
        if (tailorName != null && !tailorName.isBlank()) {
            flavorTexts.add(Component.translatable("item.tailormade.tailored.tailor", tailorName).withStyle(ChatFormatting.GRAY));
        }
        if (thisSerial != null && !thisSerial.isBlank()) {
            flavorTexts.add(Component.translatable("item.tailormade.tailored.serial", thisSerial).withStyle(ChatFormatting.GRAY));
        }
        armorStack.set(DataComponents.LORE, new ItemLore(flavorTexts));

        if (existingEnchantments != null && !existingEnchantments.isEmpty()) {
            armorStack.set(DataComponents.ENCHANTMENTS, existingEnchantments);
        }

        menu.getSlot(TailorMenu.SLOT_ARMOR).set(ItemStack.EMPTY);
        if (!player.getInventory().add(armorStack)) {
            player.drop(armorStack, false);
        }
        ChatService.showMessage(player, Component.translatable("message.tailormade.pattern_manager.tailored"), true);
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
