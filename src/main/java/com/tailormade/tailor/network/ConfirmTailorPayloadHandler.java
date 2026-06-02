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

        // 1. TailorMenu を開いているか
        if (!(player.containerMenu instanceof TailorMenu menu)) {
            logWarn(player, "TailorMenu を開いていない");
            return;
        }

        // 2. ブロックエンティティ取得
        TailorBlockEntity be = menu.getBlockEntity();
        if (be == null || be.getLevel() == null || be.getLevel().isClientSide()) {
            // BlockPos をメニューから取得してサーバーワールドから引き直す
            // TailorMenu に getBlockPos() を追加しておく必要がある（下記参照）
            BlockPos pos = menu.getBlockPos();
            if (pos == null) { logWarn(player, "BlockPos が不明"); return; }
            if (!(player.serverLevel().getBlockEntity(pos) instanceof TailorBlockEntity serverBe)) {
                logWarn(player, "サーバー側 BlockEntity が見つからない");
                return;
            }
            be = serverBe;
        }

        // 3. 型紙スロットのチェック
        ItemStack patternStack = menu.getSlot(TailorMenu.SLOT_PATTERN).getItem();
        if (patternStack.isEmpty() || !(patternStack.getItem() instanceof PatternItem patternItem)) {
            logWarn(player, "型紙スロットに PatternItem がない");
            return;
        }

        boolean hasPixelData = true;
        PixelData pixelData = null;
        String id = patternStack.get(ModDataComponents.PATTERN_ID.get());
        System.out.println("[CHECK][handleOnMainThread][id] " + id);
        if (id == null) {
            hasPixelData = false;
        } else {
            DesignDataRecord design = DesignData.get(player.serverLevel()).get(UUID.fromString(id));
            System.out.println("[CHECK][handleOnMainThread][design] " + design);
            pixelData = design != null ? design.pixelData() : null;
            if (design != null) {
                System.out.println("[CHECK][handleOnMainThread][pixelData] " + design.pixelData());
            }
        }
        if (!hasPixelData || pixelData == null) {
            logWarn(player, "型紙に PIXEL_DATA がない");
            return;
        }

        // 4. 防具スロットのチェック
        ItemStack armorStack = menu.getSlot(TailorMenu.SLOT_ARMOR).getItem();
        if (armorStack.isEmpty()) {
            logWarn(player, "防具スロットが空");
            return;
        }

        // 5. DyeCost を算出してタンクチェック
        DyeCostCalculator.DyeCost cost = DyeCostCalculator.calculate(pixelData.pixels());

        if (!cost.canAfford(be.getTankR(), be.getTankG(), be.getTankB())) {
            logWarn(player, "タンクが不足: need=(%d,%d,%d) have=(%d,%d,%d)".formatted(
                    cost.red(), cost.green(), cost.blue(),
                    be.getTankR(), be.getTankG(), be.getTankB()
            ));
            return;
        }

        // 6. タンクからコストを差し引く
        if (!be.consumeDye(cost.red(), cost.green(), cost.blue())) {
            logWarn(player, "consumeDye に失敗");
            return;
        }

        // 7. 防具に PIXEL_DATA を書き込む
//        armorStack.set(ModDataComponents.PIXEL_DATA.get(), pixelData);
        armorStack.set(ModDataComponents.PATTERN_ID.get(), id);
        // 名前とフレーバーテキスト
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

        // 8. できあがった防具をインベに入れる
        menu.getSlot(TailorMenu.SLOT_ARMOR).set(ItemStack.EMPTY);
        if (!player.getInventory().add(armorStack)) {
            player.drop(armorStack, false);
        }
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0F, 1.0F);

        // 9. 同期
        player.containerMenu.broadcastChanges();
    }

    private static void logWarn(ServerPlayer player, String reason) {
        Tailormade.LOGGER.warn(
                "ConfirmTailor: {} のリクエストを却下 ({})", player.getName().getString(), reason
        );
    }
}
