package com.tailormade.tailor.data;

import com.tailormade.tailor.data.records.CatalogData;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CatalogDataClientCache {
    private static final Map<UUID, CatalogData> CatalogDataCache = new ConcurrentHashMap<UUID, CatalogData>();
    public static void updateCache(UUID uuid, CatalogData catalog){
        CatalogDataCache.put(uuid, catalog);
    }
    public static void bulkUpdate(List<CatalogData> catalogs){
        for (CatalogData catalog: catalogs) {
            CatalogDataCache.put(catalog.id(), catalog);
        }
    }
    public static CatalogData get(UUID id) {
        return CatalogDataCache.getOrDefault(id, null);
    }
    public static Collection<CatalogData> index() {
        return CatalogDataCache.values();
    }
    public static void removeCache(UUID id) { CatalogDataCache.remove(id); }
}
