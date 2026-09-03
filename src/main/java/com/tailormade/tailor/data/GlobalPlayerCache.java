package com.tailormade.tailor.data;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class GlobalPlayerCache {
    private static final Map<UUID, GlobalPlayer> GlobalPlayerCache = new ConcurrentHashMap<UUID, GlobalPlayer>();
    public static void updateCache(UUID uuid, GlobalPlayer player){
        GlobalPlayerCache.put(uuid, player);
    }
    public static GlobalPlayer get(UUID id) {
        return GlobalPlayerCache.get(id);
    }
    public static Collection<GlobalPlayer> index() {
        return GlobalPlayerCache.values();
    }
    public static void removeCache(UUID id) { GlobalPlayerCache.remove(id); }
}
