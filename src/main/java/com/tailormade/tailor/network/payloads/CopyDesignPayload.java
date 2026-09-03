package com.tailormade.tailor.network.payloads;

import com.tailormade.tailor.Tailormade;
import com.tailormade.tailor.client.menu.DesignerMenu;
import com.tailormade.tailor.client.menu.ManagerMenu;
import com.tailormade.tailor.data.DesignData;
import com.tailormade.tailor.data.DesignDataRecord;
import com.tailormade.tailor.data.PixelData;
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
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

import static com.tailormade.tailor.Tailormade.MODID;

public record CopyDesignPayload(int slotIndex, UUID patternId) implements CustomPacketPayload {
    public static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(MODID, "copy_design_pattern");

    public static final Type<CopyDesignPayload> TYPE =
            new Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, CopyDesignPayload> STREAM_CODEC =
            StreamCodec.of(
                    CopyDesignPayload::encode,
                    CopyDesignPayload::decode
            );

    private static void encode(FriendlyByteBuf buf, CopyDesignPayload packet) {
        buf.writeVarInt(packet.slotIndex());
        buf.writeUUID(packet.patternId());
    }

    private static CopyDesignPayload decode(FriendlyByteBuf buf) {
        int slotIndex = buf.readVarInt();
        UUID patternId = buf.readUUID();
        return new CopyDesignPayload(slotIndex, patternId);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(CopyDesignPayload packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> handleOnMainThread(packet, ctx));
    }

    private static void handleOnMainThread(CopyDesignPayload packet, IPayloadContext ctx) {
        if (!(ctx.player() instanceof ServerPlayer player)) return;

        if (!(player.containerMenu instanceof ManagerMenu menu)) {
            Tailormade.LOGGER.warn(
                    "SavePattern: ManagerMenu を開いていません"
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
            ChatService.showMessage(player, Component.translatable("message.tailormade.pattern_manager.incompatible"), true);
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
        boolean isValidSize = PatternDataSaver.isValidSize(stack, orgData.pixelData(), patternItem);
        if (!isValidSize) { return; }

        // 保存実行
        // 所有者をコピーした人に変更
        DesignDataRecord newDesign = PatternDataSaver.saveDeign((ServerLevel) player.level(), stack, orgData.pixelData(), patternItem, player.getUUID(), orgData.name(), orgData.designerId(), true, false);
        if (newDesign != null) {
            Tailormade.LOGGER.warn(
                    "SavePattern: コピーしたよ！もとのID: {}、コピー品のID: {}",
                    orgData.uuid(), newDesign.uuid()
            );
            ChatService.showMessage(player, Component.translatable("message.tailormade.pattern_manager.copied"), true);
            PacketDistributor.sendToAllPlayers(new SyncDesignPayload(newDesign.uuid(), newDesign));

            ItemStack itemStack = menu.getSlot(ManagerMenu.SLOT_COPY_PATTERN).getItem();
            if (!itemStack.isEmpty()) {
                ItemStack toReturn = itemStack.copy();
                menu.getSlot(ManagerMenu.SLOT_COPY_PATTERN).set(ItemStack.EMPTY);
                player.getInventory().placeItemBackInInventory(toReturn);
            }

            player.containerMenu.broadcastChanges();
        }

        SoundService.playSound(ctx.player(), SoundEvents.EXPERIENCE_ORB_PICKUP, 1.0F);
    }
}