package com.tailormade.tailor.events;

import com.tailormade.tailor.Tailormade;
import com.tailormade.tailor.data.*;
import com.tailormade.tailor.data.records.CatalogData;
import com.tailormade.tailor.data.records.DesignTemplate;
import com.tailormade.tailor.network.payloads.*;
import com.tailormade.tailor.utils.GeneralService;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class SyncDataLayers {
    public static void addMeIfAbsent(ServerPlayer player) {
        GlobalPlayer storedPlayer = GlobalPlayerSavedData.get(player.serverLevel()).get(player.getUUID());
        if (storedPlayer == null) {
            GlobalPlayer toBeStored = GeneralService.composeNewPlayer(player.getUUID(), player.getName().getString());
            GlobalPlayerSavedData.get(player.serverLevel()).savePlayer(toBeStored);
        }
    }

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

    public static void syncGlobalPlayers(ServerPlayer player) {
        Collection<GlobalPlayer> players = GlobalPlayerSavedData.get(player.serverLevel()).index();
        for (GlobalPlayer p: players) {
            PacketDistributor.sendToPlayer(player, new SyncGlobalPlayerPayload(p));
        }
        Tailormade.LOGGER.info("[SYNC_PLAYERS] " + players.size() + " players' data have been cached.");
    }

    public static void syncDesignTemplates(ServerPlayer player) {
        Map<String, DesignTemplate> templates = TemplateRegistry.getAll();
        for (DesignTemplate t: templates.values()) {
            PacketDistributor.sendToPlayer(player, new SyncDesignTemplatePayload(t));
        }
        Tailormade.LOGGER.info("[SYNC_TEMPLATES] " + templates.size() + " design templates have been cached.");
    }

    public static void syncCatalogs(ServerPlayer player) {
        List<CatalogData> catalogs = CatalogSavedData.get(player.serverLevel()).getAll();
        PacketDistributor.sendToPlayer(player, new SyncAllCatalogsPayload(catalogs));
        Tailormade.LOGGER.info("[SYNC_CATALOGS] " + catalogs.size() + " catalogs have been cached.");
    }
}
