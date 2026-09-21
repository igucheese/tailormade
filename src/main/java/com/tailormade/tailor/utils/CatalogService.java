package com.tailormade.tailor.utils;

import com.tailormade.tailor.data.CatalogDataClientCache;
import com.tailormade.tailor.data.CatalogSavedData;
import com.tailormade.tailor.data.DesignDataClientCache;
import com.tailormade.tailor.data.DesignDataRecord;
import com.tailormade.tailor.data.records.CatalogData;
import com.tailormade.tailor.data.records.CatalogEnlistData;
import com.tailormade.tailor.network.payloads.OpenCatalogPayload;
import com.tailormade.tailor.network.payloads.SyncCatalogPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CatalogService {
    public static CatalogData newRecord(UUID id, UUID playerId, String name, List<UUID> patterns) {
        return new CatalogData(id, playerId, name, patterns);
    }

    public static CatalogData getCatalog(UUID id) {
        CatalogData data = CatalogDataClientCache.get(id);
        if (data == null) return null;
        return data;
    }

    public static List<DesignDataRecord> getCatalogContents(List<UUID> patternIds) {
        List<DesignDataRecord> records = new ArrayList<>();
        for (UUID id: patternIds) {
            DesignDataRecord record = DesignDataClientCache.get(id);
            if (record != null) {
                records.add(record);
            }
        }
        return records;
    }

    public static List<CatalogEnlistData> getCombinedCustomerList(List<DesignDataRecord> designs) {
        List<CatalogEnlistData> listedDesigns = new ArrayList<>();
        for (DesignDataRecord design: designs) {
            CatalogEnlistData listedDesign = new CatalogEnlistData(design, true);
            listedDesigns.add(listedDesign);
        }
        return listedDesigns;
    }

    public static List<CatalogEnlistData> getCombinedManagementList(List<DesignDataRecord> designs, List<UUID> listed) {
        List<CatalogEnlistData> listedDesigns = new ArrayList<>();
        for (DesignDataRecord design: designs) {
            boolean isListed = listed.contains(design.uuid());
            CatalogEnlistData listedDesign = new CatalogEnlistData(design, isListed);
            listedDesigns.add(listedDesign);
        }
        return listedDesigns;
    }

    public static void openClientScreen(ServerPlayer player, UUID id, boolean isFromLectern, BlockPos pos) {
        CatalogData catalog = CatalogSavedData.get(player.serverLevel()).getCatalog(id);
        if (catalog == null) {
            // 新規作成する
            catalog = CatalogService.newRecord(id, player.getUUID(), player.getName().getString() + "'s Catalog", List.of());
            CatalogSavedData.get(player.serverLevel()).saveCatalog(catalog);
            PacketDistributor.sendToPlayer(player, new SyncCatalogPayload(catalog));
        }

        // 画面オープン！
        PacketDistributor.sendToPlayer(player, new OpenCatalogPayload(catalog.id(), isFromLectern, pos));
    }
}
