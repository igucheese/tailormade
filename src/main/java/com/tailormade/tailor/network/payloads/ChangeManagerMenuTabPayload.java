package com.tailormade.tailor.network.payloads;

import com.tailormade.tailor.Tailormade;
import com.tailormade.tailor.client.menu.ManagerMenu;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import static com.tailormade.tailor.Tailormade.MODID;

public record ChangeManagerMenuTabPayload(int tabId) implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(MODID, "change_manager_menu_tab");
    public static final Type<ChangeManagerMenuTabPayload> TYPE = new Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, ChangeManagerMenuTabPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, ChangeManagerMenuTabPayload::tabId,
            ChangeManagerMenuTabPayload::new
    );

    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(ChangeManagerMenuTabPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            if (!(player.containerMenu instanceof ManagerMenu menu)) {
                Tailormade.LOGGER.warn("型紙マネージャーが開かれていません");
                return;
            }
            menu.setTab(payload.tabId());
        });
    }
}
