package com.tailormade.tailor.entities.items;

import com.tailormade.tailor.registries.ModDataComponents;
import com.tailormade.tailor.utils.CatalogService;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.WritableBookItem;
import net.minecraft.world.level.Level;

import java.util.UUID;

public class CatalogBookItem extends WritableBookItem {
    public CatalogBookItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        if (player instanceof ServerPlayer serverPlayer) {
            String catalogIdStr = itemstack.get(ModDataComponents.CATALOG_ID.get());
            if (catalogIdStr == null || catalogIdStr.isBlank()) return InteractionResultHolder.sidedSuccess(itemstack, level.isClientSide());

            UUID catalogId = UUID.fromString(catalogIdStr);
            CatalogService.openClientScreen(serverPlayer, catalogId, false, player.getOnPos());
        }
        return InteractionResultHolder.sidedSuccess(itemstack, level.isClientSide());
    }
}
