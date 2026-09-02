package com.tailormade.tailor.network.payloads;

import com.tailormade.tailor.data.DesignData;
import com.tailormade.tailor.data.DesignDataRecord;
import com.tailormade.tailor.utils.files.DesignDataExportSender;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

import static com.tailormade.tailor.Tailormade.MODID;

public record ExportDesignDataStartPayload(UUID id) implements CustomPacketPayload {
    public static final Type<ExportDesignDataStartPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(MODID, "export_design_data_start"));

    public static final StreamCodec<ByteBuf, ExportDesignDataStartPayload> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, ExportDesignDataStartPayload::id,
            ExportDesignDataStartPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ExportDesignDataStartPayload packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> handleOnMainThread(packet, ctx));
    }

    private static void handleOnMainThread(ExportDesignDataStartPayload packet, IPayloadContext ctx) {
        if (!(ctx.player() instanceof ServerPlayer player)) return;
        ServerLevel level = player.serverLevel();

        // デザインレコード取得
        DesignDataRecord record = DesignData.get(level).get(packet.id());
        if (record == null) return;

        // エクスポート開始
        DesignDataExportSender.sendToClient(player, record);
    }
}
