package com.tailormade.tailor.network.payloads;

import com.tailormade.tailor.data.CatalogSavedData;
import com.tailormade.tailor.data.records.CatalogData;
import com.tailormade.tailor.entities.items.CatalogBookItem;
import com.tailormade.tailor.registries.ModDataComponents;
import com.tailormade.tailor.registries.ModItems;
import com.tailormade.tailor.utils.CatalogService;
import com.tailormade.tailor.utils.SoundService;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;
import java.util.UUID;

import static com.tailormade.tailor.Tailormade.MODID;

public record GetCatalogBookPayload(UUID id) implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(MODID, "get_catalog_book");

    public static final Type<GetCatalogBookPayload> TYPE = new Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, GetCatalogBookPayload> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, GetCatalogBookPayload::id,
            GetCatalogBookPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(GetCatalogBookPayload packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer player)) return;
            CatalogData catalog = CatalogSavedData.get(player.serverLevel()).getCatalog(packet.id());
            if (catalog == null) return;

            CatalogBookItem book = ModItems.CATALOG_BOOK.get();
            ItemStack bookStack = new ItemStack(book, 1);
            bookStack.set(ModDataComponents.CATALOG_ID.get(), packet.id().toString());
            bookStack.set(DataComponents.CUSTOM_NAME, Component.literal(catalog.name()));

            if (!player.addItem(bookStack)) {
                player.drop(bookStack, false);
            }
            SoundService.playSound(player, SoundEvents.EXPERIENCE_ORB_PICKUP, 1.0F);
        });
    }
}