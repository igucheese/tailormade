package com.tailormade.tailor.network.payloads;

import com.tailormade.tailor.Tailormade;
import com.tailormade.tailor.client.menu.DesignerMenu;
import com.tailormade.tailor.client.menu.ManagerMenu;
import com.tailormade.tailor.data.*;
import com.tailormade.tailor.entities.items.PatternItem;
import com.tailormade.tailor.registries.ModDataComponents;
import com.tailormade.tailor.utils.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
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

public record ImportDesignPayload(DesignDataRecord imported) implements CustomPacketPayload {
    public static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(MODID, "import_design_pattern");

    public static final Type<ImportDesignPayload> TYPE =
            new Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, ImportDesignPayload> STREAM_CODEC =
            StreamCodec.of(
                    ImportDesignPayload::encode,
                    ImportDesignPayload::decode
            );

    private static void encode(RegistryFriendlyByteBuf buf, ImportDesignPayload packet) {
        DesignDataRecord.STREAM_CODEC.encode(buf, packet.imported());
    }

    private static ImportDesignPayload decode(RegistryFriendlyByteBuf buf) {
        DesignDataRecord imported = DesignDataRecord.STREAM_CODEC.decode(buf);
        return new ImportDesignPayload(imported);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ImportDesignPayload packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> handleOnMainThread(packet, ctx));
    }

    private static void handleOnMainThread(ImportDesignPayload packet, IPayloadContext ctx) {
        if (!(ctx.player() instanceof ServerPlayer player)) return;
        ServerLevel level = player.serverLevel();

        if (!(player.containerMenu instanceof ManagerMenu menu)) {
            Tailormade.LOGGER.warn(
                    "SavePattern: ManagerMenu を開いていません"
            );
            return;
        }

        ItemStack stack = menu.getPatternContainer().getItem(ManagerMenu.SLOT_IMPORT_PATTERN);
        if (stack.isEmpty() || !(stack.getItem() instanceof PatternItem patternItem)) {
            Tailormade.LOGGER.warn("SavePattern: スロットに PatternItem がありません");
            ChatService.showMessage(player, Component.translatable("message.tailormade.pattern_manager.incompatible"), true);
            return;
        }
        boolean isValidSize = PatternDataSaver.isValidSize(stack, packet.imported().pixelData(), patternItem);
        if (!isValidSize) { return; }

        // 更新用の名前
        String name = Component.translatable("item.tailormade.pattern.imported", packet.imported().name()).getString();

        // 保存実行
        DesignDataRecord newDesign = PatternDataSaver.saveDeign(level, stack, packet.imported().pixelData(), patternItem, player.getUUID(), packet.imported().name(), packet.imported().designerId(), false, false, true);
        if (newDesign != null) {
            ChatService.showMessage(player, Component.translatable("message.tailormade.pattern_manager.design_imported"), true);
            PacketDistributor.sendToAllPlayers(new SyncDesignPayload(newDesign.uuid(), newDesign));

            ItemStack itemStack = menu.getSlot(ManagerMenu.SLOT_IMPORT_PATTERN).getItem();
            if (!itemStack.isEmpty()) {
                ItemStack toReturn = itemStack.copy();
                menu.getSlot(ManagerMenu.SLOT_IMPORT_PATTERN).set(ItemStack.EMPTY);
                player.getInventory().placeItemBackInInventory(toReturn);
            }

            // インポートしたデザインのデザイナーが GlobalPlayer データとして保存されていない場合は取得を試みる
            GlobalPlayer playerData = GlobalPlayerSavedData.get(level).get(packet.imported().designerId());
            if (playerData == null) {
                MojangApiAccessor.fetchUsernameFromUUID(packet.imported().designerId()).thenAccept(fetchedName -> {
                    if (fetchedName != null) {
                        GlobalPlayer newPlayer = GeneralService.composeNewPlayer(packet.imported().designerId(), fetchedName);
                        GlobalPlayerSavedData.get(level).savePlayer(newPlayer);
                        PacketDistributor.sendToAllPlayers(new SyncGlobalPlayerPayload(newPlayer));
                    } else {
                        Tailormade.LOGGER.warn("UUID {} に該当するユーザーが見つかりませんでした。", packet.imported().designerId());
                    }
                });
            }
            player.containerMenu.broadcastChanges();
            SoundService.playSound(ctx.player(), SoundEvents.EXPERIENCE_ORB_PICKUP, 1.0F);
        }
    }
}