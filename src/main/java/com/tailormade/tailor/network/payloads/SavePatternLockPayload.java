package com.tailormade.tailor.network.payloads;

import com.tailormade.tailor.Tailormade;
import com.tailormade.tailor.data.DesignData;
import com.tailormade.tailor.data.DesignDataClientCache;
import com.tailormade.tailor.data.DesignDataRecord;
import com.tailormade.tailor.data.PixelData;
import com.tailormade.tailor.utils.SoundService;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.apache.logging.log4j.core.jmx.Server;

import java.util.UUID;

import static com.tailormade.tailor.Tailormade.MODID;

public record SavePatternLockPayload(UUID patternId, boolean isLocked) implements CustomPacketPayload {
    public static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(MODID, "save_pattern_locked_status");

    public static final Type<SavePatternLockPayload> TYPE =
            new Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, SavePatternLockPayload> STREAM_CODEC =
            StreamCodec.of(
                    SavePatternLockPayload::encode,
                    SavePatternLockPayload::decode
            );

    private static void encode(FriendlyByteBuf buf, SavePatternLockPayload packet) {
        buf.writeUUID(packet.patternId);
        buf.writeBoolean(packet.isLocked());
    }

    private static SavePatternLockPayload decode(FriendlyByteBuf buf) {
        UUID patternId = buf.readUUID();
        boolean isLocked = buf.readBoolean();
        return new SavePatternLockPayload(patternId, isLocked);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SavePatternLockPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            // ロックステータスを保存する
            ServerLevel level = (ServerLevel) ctx.player().level();
            DesignDataRecord design = DesignData.get(level).get(payload.patternId());
            if (design == null) { return; }
            DesignDataRecord newDesign = payload.isLocked() ? design.withLocked() : design.withUnlocked();
            DesignData.get(level).updateDesign(payload.patternId(), newDesign);

            System.out.println("[CHECK][SavePatternLockPayload.handle] saved design lock status: " + newDesign);
            PacketDistributor.sendToAllPlayers(new SyncDesignPayload(payload.patternId(), newDesign));

            SoundService.playSound(ctx.player(), SoundEvents.EXPERIENCE_ORB_PICKUP, 1.0F);
        });
    }
}