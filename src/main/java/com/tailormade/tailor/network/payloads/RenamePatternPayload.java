package com.tailormade.tailor.network.payloads;

import com.tailormade.tailor.Tailormade;
import com.tailormade.tailor.client.menu.ManagerMenu;
import com.tailormade.tailor.data.DesignData;
import com.tailormade.tailor.data.DesignDataRecord;
import com.tailormade.tailor.entities.items.PatternItem;
import com.tailormade.tailor.registries.ModDataComponents;
import com.tailormade.tailor.utils.ChatService;
import com.tailormade.tailor.utils.SoundService;
import net.minecraft.core.component.DataComponents;
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

public record RenamePatternPayload(UUID patternId, String name) implements CustomPacketPayload {
    public static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(MODID, "rename_pattern");

    public static final Type<RenamePatternPayload> TYPE =
            new Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, RenamePatternPayload> STREAM_CODEC =
            StreamCodec.of(
                    RenamePatternPayload::encode,
                    RenamePatternPayload::decode
            );

    private static void encode(FriendlyByteBuf buf, RenamePatternPayload packet) {
        buf.writeUUID(packet.patternId);
        buf.writeUtf(packet.name);
    }

    private static RenamePatternPayload decode(FriendlyByteBuf buf) {
        UUID patternId = buf.readUUID();
        String name = buf.readUtf();
        return new RenamePatternPayload(patternId, name);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(RenamePatternPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer player)) return;
            if (!(player.containerMenu instanceof ManagerMenu menu)) {
                Tailormade.LOGGER.warn(
                        "SavePattern: ManagerMenu を開いていません"
                );
                return;
            }
            ItemStack stack = menu.getPatternContainer().getItem(ManagerMenu.SLOT_PATTERN);
            if (stack.isEmpty() || !(stack.getItem() instanceof PatternItem patternItem)) {
                Tailormade.LOGGER.warn(
                        "SavePattern: スロット {} に PatternItem がありません",
                        ManagerMenu.SLOT_PATTERN
                );
                ChatService.showMessage(player, Component.translatable("message.tailormade.pattern_manager.incompatible"), true);
                return;
            }

            ServerLevel level = (ServerLevel) ctx.player().level();
            DesignDataRecord design = DesignData.get(level).get(payload.patternId());
            if (design == null) { return; }
            // ロックされていて、かつ player = design.player ではない場合は弾く
            if (design.isLocked() && !player.getUUID().equals(design.userId())) {
                ChatService.showMessage(player, Component.translatable("message.tailormade.pattern_manager.guarded"), true);
                return;
            }
            DesignDataRecord newDesign = design.withName(payload.name());
            DesignData.get(level).updateDesign(payload.patternId(), newDesign);

            stack.set(DataComponents.CUSTOM_NAME, Component.literal(payload.name()));
            stack.set(ModDataComponents.PATTERN_NAME, payload.name());

            System.out.println("[CHECK][RenamePatternPayload.handle] saved design name: " + newDesign);
            ChatService.showMessage(player, Component.translatable("message.tailormade.pattern_manager.renamed"), true);
            PacketDistributor.sendToAllPlayers(new SyncDesignPayload(payload.patternId(), newDesign));

            SoundService.playSound(ctx.player(), SoundEvents.EXPERIENCE_ORB_PICKUP, 1.0F);
        });
    }
}