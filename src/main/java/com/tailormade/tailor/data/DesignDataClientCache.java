package com.tailormade.tailor.data;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class DesignDataClientCache {
    private static final Map<UUID, DesignDataRecord> DesignDataCache = new ConcurrentHashMap<UUID, DesignDataRecord>();
    public static void updateCache(UUID uuid, DesignDataRecord design){
        DesignDataCache.put(uuid, design);
    }
    public static DesignDataRecord get(UUID id) {
        return DesignDataCache.get(id);
    }
    public static Collection<DesignDataRecord> index() {
        return DesignDataCache.values();
    }
    public static void removeCache(UUID id) { DesignDataCache.remove(id); }
}
