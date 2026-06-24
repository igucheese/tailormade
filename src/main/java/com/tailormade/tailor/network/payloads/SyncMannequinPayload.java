package com.tailormade.tailor.network.payloads;

import com.tailormade.tailor.client.ClientHooks;
import com.tailormade.tailor.entities.blockentities.MannequinEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import static com.tailormade.tailor.Tailormade.MODID;
import static com.tailormade.tailor.client.ClientHooks.syncMannequin;

public record SyncMannequinPayload(
        int entityId,
        int slotOrdinal,
        ItemStack stack
) implements CustomPacketPayload {

    public static final Type<SyncMannequinPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(MODID, "sync_mannequin"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncMannequinPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT, SyncMannequinPayload::entityId,
                    ByteBufCodecs.INT, SyncMannequinPayload::slotOrdinal,
                    ItemStack.OPTIONAL_STREAM_CODEC, SyncMannequinPayload::stack,
                    SyncMannequinPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SyncMannequinPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientHooks.syncMannequin(payload);
        });
    }
}