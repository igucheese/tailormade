package com.tailormade.tailor.client.screen;

import com.tailormade.tailor.data.records.DesignTemplate;
import net.minecraft.client.Minecraft;

public class TemplateScrollWidget {
    public static ScrollableWidget.ItemRenderer<DesignTemplate> getRenderer() {
        ScrollableWidget.ItemRenderer<DesignTemplate> renderer = (g, template, x, y, itemHeight, hovered) -> {
            g.drawString(Minecraft.getInstance().font, template.name(), x, y + 5, 0xff4d3535, false);
        };
        return renderer;
    }
}
