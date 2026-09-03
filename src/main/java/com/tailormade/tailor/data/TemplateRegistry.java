package com.tailormade.tailor.data;

import com.tailormade.tailor.data.records.DesignTemplate;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class TemplateRegistry {

    private static final Map<String, DesignTemplate> TEMPLATES = new LinkedHashMap<>();

    private TemplateRegistry() {}

    public static void clear() {
        TEMPLATES.clear();
    }

    public static void register(String id, DesignTemplate template) {
        TEMPLATES.put(id, template);
    }

    public static DesignTemplate get(String id) {
        return TEMPLATES.get(id);
    }

    public static Map<String, DesignTemplate> getAll() {
        return Collections.unmodifiableMap(TEMPLATES);
    }
}
