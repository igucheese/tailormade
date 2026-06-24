package com.tailormade.tailor.events;

import com.tailormade.tailor.Tailormade;
import com.tailormade.tailor.data.*;
import com.tailormade.tailor.network.payloads.SyncDesignPayload;
import com.tailormade.tailor.network.payloads.SyncSkinLayerPayload;
import com.tailormade.tailor.network.payloads.SyncUnderwearPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Collection;

public class SyncDataLayers {
    public static void syncSkinLayer(ServerPlayer player) {
        Collection<PixelData> skins = PowderRoomSavedData.get((ServerLevel) player.level()).index();
        for (PixelData skin : skins) {
            if (skin.getPixels() != null) {
                Tailormade.LOGGER.info("[SYNC_SKINS] Sync Player's Skin: " + player.getName().getString());
                PacketDistributor.sendToPlayer(player, new SyncSkinLayerPayload(player.getUUID(), skin.getPixels()));
            } else {
                Tailormade.LOGGER.info("[SYNC_SKINS] Sync Player's Skin has been skipped.");
            }
        }
    }

    public static void syncUnderwearLayer(ServerPlayer player) {
        Collection<UnderwearSetting> settings = WardrobeSavedData.get((ServerLevel) player.level()).index();
        for (UnderwearSetting setting : settings) {
            Tailormade.LOGGER.info("[SYNC_UNDERWEAR] Sync Player's Underwear: " + player.getName().getString() + " body: " + setting);
            PacketDistributor.sendToPlayer(player, new SyncUnderwearPayload(player.getUUID(), setting));
        }
    }

    public static void syncDesignData(ServerPlayer player) {
        Tailormade.LOGGER.info("[SYNC_DESIGN] Sync All Masterpieces!");
        DesignData data = DesignData.get(player.serverLevel());
        Collection<DesignDataRecord> designs = data.index();
//        PacketDistributor.sendToPlayer(player, new SyncAllDesignsPayload(designs)); // いずれこうしたいね
        for (DesignDataRecord design : designs) {
            Tailormade.LOGGER.info("[SYNC_DESIGN] " + design.uuid() + ", name: " + design.name() + ", data: " + design.pixelData());
            PacketDistributor.sendToPlayer(player, new SyncDesignPayload(design.uuid(), design));
        }
        Tailormade.LOGGER.info("[SYNC_DESIGN] " + designs.size() + " designs have been cached.");
    }
}
