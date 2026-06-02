package com.tailormade.tailor.data;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SkinDataClientCache {
    private static final Map<UUID, PixelData> SkinDataCache = new ConcurrentHashMap<UUID, PixelData>();
    public static void updateCache(UUID uuid, PixelData skin){
        SkinDataCache.put(uuid, skin);
    }
    public static PixelData get(UUID id) {
        return SkinDataCache.get(id);
    }
    public static Collection<PixelData> index() {
        return SkinDataCache.values();
    }
    public static void removeCache(UUID id) { SkinDataCache.remove(id); }
}
