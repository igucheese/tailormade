package com.tailormade.tailor.network.payloads;

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

    public static void handle(
            SyncMannequinPayload payload,
            IPayloadContext context) {

        context.enqueueWork(() -> {
            Level level = Minecraft.getInstance().level;
            if (level == null) return;

            Entity entity = level.getEntity(payload.entityId());
            if (!(entity instanceof MannequinEntity mannequin)) return;

            EquipmentSlot slot = EquipmentSlot.values()[payload.slotOrdinal()];

            switch (slot) {
                case FEET     -> mannequin.armorItems.set(0, payload.stack());
                case LEGS     -> mannequin.armorItems.set(1, payload.stack());
                case CHEST    -> mannequin.armorItems.set(2, payload.stack());
                case HEAD     -> mannequin.armorItems.set(3, payload.stack());
                case MAINHAND -> mannequin.handItems.set(0, payload.stack());
                case OFFHAND  -> mannequin.handItems.set(1, payload.stack());
            }
        });
    }
}