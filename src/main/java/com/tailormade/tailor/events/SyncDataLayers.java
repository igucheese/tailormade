package com.tailormade.tailor.events;

import com.tailormade.tailor.Tailormade;
import com.tailormade.tailor.data.*;
import com.tailormade.tailor.network.payloads.SyncAllDesignsPayload;
import com.tailormade.tailor.network.payloads.SyncDesignPayload;
import com.tailormade.tailor.network.payloads.SyncSkinLayerPayload;
import com.tailormade.tailor.network.payloads.SyncUnderwarePayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import org.apache.logging.log4j.core.jmx.Server;

import java.util.Collection;

import static com.tailormade.tailor.client.renderer.SkinLayerRenderLayer.getSkinPixels;
import static com.tailormade.tailor.client.renderer.SkinLayerRenderLayer.getUnderwearSetting;

public class SyncDataLayers {
    public static void syncSkinLayer(ServerPlayer player) {
        PixelData savedSkin = PowderRoomSavedData.get((ServerLevel) player.level()).getSkinLayer(player.getUUID());
        int[] skinPixels = savedSkin != null ? savedSkin.getPixels() : getSkinPixels(player.getUUID());
        if (skinPixels != null) {
            Tailormade.LOGGER.info("[SYNC_SKINS] Sync Player's Skin: " + player.getName().getString());
            PacketDistributor.sendToPlayer(player, new SyncSkinLayerPayload(player.getUUID(), skinPixels));
        } else {
            Tailormade.LOGGER.info("[SYNC_SKINS] Sync Player's Skin has been skipped.");
        }
    }

    public static void syncUnderwearLayer(ServerPlayer player) {
        UnderwearSetting underwearSetting = WardrobeSavedData.get((ServerLevel) player.level()).getSetting(player.getUUID());
        if (underwearSetting != null) {
            Tailormade.LOGGER.info("[SYNC_UNDERWEAR] Sync Player's Underwear: " + player.getName().getString() + " body: " + underwearSetting);
            PacketDistributor.sendToPlayer(player, new SyncUnderwarePayload(player.getUUID(), underwearSetting));
        } else {
            Tailormade.LOGGER.info("[SYNC_UNDERWEAR] Sync Player's Underwear has been skipped.");
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
