package com.tailormade.tailor.network.payloads;

import com.tailormade.tailor.data.UnderwearSetting;
import com.tailormade.tailor.data.WardrobeSavedData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import static com.tailormade.tailor.Tailormade.MODID;

public record SaveUnderwarePayload (UnderwearSetting setting) implements CustomPacketPayload {

    public static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(MODID, "save_underwear");
    public static final Type<SaveUnderwarePayload> TYPE = new Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, SaveUnderwarePayload> STREAM_CODEC =
            StreamCodec.of(
                    (buf, p) -> UnderwearSetting.STREAM_CODEC.encode(buf, p.setting()),
                    buf -> new SaveUnderwarePayload(UnderwearSetting.STREAM_CODEC.decode(buf))
            );

    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(SaveUnderwarePayload packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer player)) return;

            ServerLevel overworld = player.getServer().overworld();
            WardrobeSavedData.get(overworld).setSetting(player.getUUID(), packet.setting());

            // 全員に配信
            SyncUnderwarePayload syncPacket = new SyncUnderwarePayload(player.getUUID(), packet.setting());
            PacketDistributor.sendToAllPlayers(syncPacket);
        });
    }
}