package com.tailormade.tailor.network.payloads;

import com.tailormade.tailor.Tailormade;
import com.tailormade.tailor.client.menu.ManagerMenu;
import com.tailormade.tailor.client.menu.TailorMenu;
import com.tailormade.tailor.data.DesignData;
import com.tailormade.tailor.data.DesignDataRecord;
import com.tailormade.tailor.data.PatternType;
import com.tailormade.tailor.entities.items.PatternItem;
import com.tailormade.tailor.utils.ChatService;
import com.tailormade.tailor.utils.PatternDataSaver;
import com.tailormade.tailor.utils.SoundService;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

import static com.tailormade.tailor.Tailormade.MODID;

public record ExtractDesignPayload(UUID patternId) implements CustomPacketPayload {
    public static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(MODID, "extract_design_pattern");

    public static final Type<ExtractDesignPayload> TYPE =
            new Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, ExtractDesignPayload> STREAM_CODEC =
            StreamCodec.of(
                    ExtractDesignPayload::encode,
                    ExtractDesignPayload::decode
            );

    private static void encode(FriendlyByteBuf buf, ExtractDesignPayload packet) {
        buf.writeUUID(packet.patternId());
    }

    private static ExtractDesignPayload decode(FriendlyByteBuf buf) {
        UUID patternId = buf.readUUID();
        return new ExtractDesignPayload(patternId);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ExtractDesignPayload packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> handleOnMainThread(packet, ctx));
    }

    private static void handleOnMainThread(ExtractDesignPayload packet, IPayloadContext ctx) {
        if (!(ctx.player() instanceof ServerPlayer player)) return;

        if (!(player.containerMenu instanceof ManagerMenu menu)) {
            Tailormade.LOGGER.warn(
                    "SavePattern: ManagerMenu を開いていません"
            );
            return;
        }

        ItemStack armorStack = menu.getPatternContainer().getItem(ManagerMenu.SLOT_TAILORED_ARMOUR);
        ItemStack patternStack = menu.getPatternContainer().getItem(ManagerMenu.SLOT_EXTRACT_PATTERN);
        if (armorStack.isEmpty() || !(armorStack.getItem() instanceof ArmorItem armorItem)) {
            ChatService.showMessage(player, Component.translatable("message.tailormade.pattern_manager.armor_blank"), true);
            Tailormade.LOGGER.warn("防具がセットされていません！");
            return;
        }
        if (patternStack.isEmpty() || !(patternStack.getItem() instanceof PatternItem patternItem)) {
            ChatService.showMessage(player, Component.translatable("message.tailormade.pattern_manager.not_blank"), true);
            Tailormade.LOGGER.warn("空の型紙がセットされていません！");
            return;
        }
        PatternType type = patternItem.getPatternType(patternStack);
        if (armorItem.getEquipmentSlot() != TailorMenu.patternTypeToEquipmentSlot(type)) {
            ChatService.showMessage(player, Component.translatable("message.tailormade.pattern_manager.incompatible"), true);
            Tailormade.LOGGER.warn("対応する型紙がセットされていません！");
            return;
        }
        ServerLevel level = player.serverLevel();

        // 元データ
        DesignDataRecord orgData = DesignData.get(level).get(packet.patternId());
        if (orgData == null) return;
        // ロックされていて、かつ player = org.player ではない場合は弾く
        if (orgData.isLocked() && !player.getUUID().equals(orgData.userId())) {
            ChatService.showMessage(player, Component.translatable("message.tailormade.pattern_manager.guarded"), true);
            return;
        }
        boolean isValidSize = PatternDataSaver.isValidSize(patternStack, orgData.pixelData(), patternItem);
        if (!isValidSize) { return; }

        // 保存実行
        // 所有者を抽出した人に更新
        DesignDataRecord newDesign = PatternDataSaver.saveDeign((ServerLevel) player.level(), patternStack, orgData.pixelData(), orgData.layers(), patternItem, player.getUUID(), orgData.name(), orgData.designerId(), false, true, false);
        if (newDesign != null) {
            Tailormade.LOGGER.warn(
                    "SavePattern: 抽出したよ！もとのID: {}、抽出品のID: {}",
                    orgData.uuid(), newDesign.uuid()
            );
            PacketDistributor.sendToAllPlayers(new SyncDesignPayload(newDesign.uuid(), newDesign));
            ChatService.showMessage(player, Component.translatable("message.tailormade.pattern_manager.extracted"), true);

            ItemStack itemStack = menu.getSlot(ManagerMenu.SLOT_EXTRACT_PATTERN).getItem();
            if (!patternStack.isEmpty()) {
                ItemStack toReturn = itemStack.copy();
                menu.getSlot(ManagerMenu.SLOT_EXTRACT_PATTERN).set(ItemStack.EMPTY);
                player.getInventory().placeItemBackInInventory(toReturn);
            }

            player.containerMenu.broadcastChanges();
        }

        SoundService.playSound(ctx.player(), SoundEvents.EXPERIENCE_ORB_PICKUP, 1.0F);
    }
}