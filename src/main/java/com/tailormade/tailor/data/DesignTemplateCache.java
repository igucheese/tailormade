package com.tailormade.tailor.data;

import com.tailormade.tailor.data.records.DesignTemplate;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DesignTemplateCache {
    private static List<DesignTemplate> DesignTemplateCache = new ArrayList<>();
    public static void setCache(List<DesignTemplate> cache){
        DesignTemplateCache = cache;
    }
    public static void addCache(DesignTemplate cache) {
        DesignTemplateCache.add(cache);
    }
    public static List<DesignTemplate> getByType(String type) {
        return DesignTemplateCache.stream().filter(d -> d.type().equals(type)).toList();
    }
    public static List<DesignTemplate> index() {
        return DesignTemplateCache;
    }
}
