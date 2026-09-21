package com.tailormade.tailor.client.screen.ui;

import com.tailormade.tailor.data.records.CatalogEnlistData;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

import static com.tailormade.tailor.Tailormade.MODID;

public class CatalogScrollWidget {
    private static final ResourceLocation LOCKED = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/pattern_locked_status.png");
    private static final ResourceLocation TYPE_HAT = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/catalog/pattern_type_label_hat.png");
    private static final ResourceLocation TYPE_SHIRT = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/catalog/pattern_type_label_shirt.png");
    private static final ResourceLocation TYPE_PANTS = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/catalog/pattern_type_label_pants.png");
    private static final ResourceLocation TYPE_SHOES = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/catalog/pattern_type_label_shoes.png");

    public static ScrollableWidget.ItemRenderer<CatalogEnlistData> getRenderer(boolean isManaging) {
        ScrollableWidget.ItemRenderer<CatalogEnlistData> renderer = (g, record, x, y, itemHeight, hovered) -> {
            //  ロック状態と型紙種類
            int labelOffsetX = 8;
            if (record.design().isLocked()) {
                labelOffsetX = 28;
                g.blit(LOCKED, x + 8, y, 0, 0, 16, 16, 16, 16);
            }
            ResourceLocation TYPE_ICON = null;
            switch (record.design().type()) {
                case "head" -> TYPE_ICON = TYPE_HAT;
                case "chest" -> TYPE_ICON = TYPE_SHIRT;
                case "legs" -> TYPE_ICON = TYPE_PANTS;
                case "feet" -> TYPE_ICON = TYPE_SHOES;
            }
            if (TYPE_ICON != null) {
                g.blit(TYPE_ICON, x + labelOffsetX, y, 0, 0, 48, 16, 48, 16);
            }

            // 型紙名
            String title = record.design().name();
            g.drawString(Minecraft.getInstance().font, title, x + 8, y + 16, 0xffdfc9a3, false);

            // アクティブ枠
            if (record.isListed() && isManaging) {
                int labelBorderColor = 0xFF44FF44;
                int w = 184;
                int h = 30;
                g.fill(x, y, x + w, y + 1, labelBorderColor);
                g.fill(x, y + h - 1, x + w, y + h, labelBorderColor);
                g.fill(x, y, x + 1, y + h, labelBorderColor);
                g.fill(x + w - 1, y, x + w, y + h, labelBorderColor);
            }
        };
        return renderer;
    }
}
