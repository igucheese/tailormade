package com.tailormade.tailor.client;

import com.tailormade.tailor.client.screen.CatalogScreen;
import com.tailormade.tailor.client.screen.PowderRoomScreen;
import com.tailormade.tailor.client.screen.WardrobeScreen;
import com.tailormade.tailor.entities.blockentities.MannequinEntity;
import com.tailormade.tailor.network.payloads.SyncMannequinPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.UUID;

@OnlyIn(Dist.CLIENT)
public class ClientHooks {
    public static void syncMannequin(SyncMannequinPayload payload)
    {
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
    }

    public static void openPowderRoomScreen(UUID playerId) {
        Minecraft.getInstance().setScreen(new PowderRoomScreen(playerId));
    }

    public static void openWardrobeScreen() {
        Minecraft.getInstance().setScreen(new WardrobeScreen());
    }

    public static void openCatalogScreen(UUID catalogId, boolean isFromLectern, BlockPos pos) {
        Minecraft.getInstance().setScreen(new CatalogScreen(catalogId, isFromLectern, pos));
    }
}
