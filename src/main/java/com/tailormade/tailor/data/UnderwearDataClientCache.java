package com.tailormade.tailor.data;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class UnderwearDataClientCache {
    private static final Map<UUID, UnderwearSetting> UnderwearDataCache = new ConcurrentHashMap<UUID, UnderwearSetting>();
    public static void updateCache(UUID uuid, UnderwearSetting setting){
        UnderwearDataCache.put(uuid, setting);
    }
    public static UnderwearSetting get(UUID id) {
        return UnderwearDataCache.get(id);
    }
    public static Collection<UnderwearSetting> index() {
        return UnderwearDataCache.values();
    }
    public static void removeCache(UUID id) { UnderwearDataCache.remove(id); }
}
