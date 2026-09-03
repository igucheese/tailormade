package com.tailormade.tailor.client.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class ScrollableWidget<T> extends AbstractWidget {
    public interface ItemRenderer<T> {
        void render(GuiGraphics g, T item, int x, int y, int itemHeight, boolean hovered);
    }
    private final int itemHeight;
    private final int maxVisible;
    private List<T> items;
    private final Consumer<T> onSelect;
    private final ItemRenderer<T> itemRenderer;

    private int scrollOffset = 0;
    private int selectedIndex = -1;

    private int backgroundColor = 0xFF222222;
    private int hoverColor = 0xFF555555;
    private boolean shouldRenderOutline = true;
    private int borderColor = 0xFF888888;
    private int scrollBarColor = 0xFF888888;

    public ScrollableWidget(
            int x,
            int y,
            int width,
            int height,
            String placeholder,
            int itemHeight,
            int maxVisible,
            List<T> items,
            Consumer<T> onSelect,
            ItemRenderer<T> itemRenderer
    ) {
        super(x, y, width, height, Component.literal(placeholder));
        this.itemHeight = itemHeight;
        this.maxVisible = maxVisible;
        this.items = items;
        this.onSelect = onSelect;
        this.itemRenderer = itemRenderer;
    }

    public void setItems(List<T> items) {
        this.items = items;
        this.scrollOffset = 0;
        this.selectedIndex = -1;
    }

    public void setBackgroundColor(int color) {
        this.backgroundColor = color;
    }
    public void setOutlineVisible(boolean show) {
        this.shouldRenderOutline = show;
    }
    public void setBorderColor(int color) {
        this.borderColor = color;
    }
    public void setHoverColor(int color) {
        this.hoverColor = color;
    }
    public void setScrollBarColor(int color) {
        this.scrollBarColor = color;
    }

    @Override
    public void renderWidget(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        g.fill(getX(), getY(), getX() + width, getY() + height, this.backgroundColor);
        if (this.shouldRenderOutline) {
            g.renderOutline(getX(), getY(), width, height, this.borderColor);
        }

        int dropY = getY();
        int visibleCount = Math.min(items.size(), maxVisible);
        int dropHeight = visibleCount * itemHeight + 2;

        for (int i = 0; i < maxVisible; i++) {
            int optionIndex = i + scrollOffset;
            if (optionIndex >= items.size()) break;

            int itemY = dropY + 1 + i * itemHeight;
            boolean hovered = mouseX >= getX() && mouseX <= getX() + width
                    && mouseY >= itemY && mouseY < itemY + itemHeight;

            if (hovered) {
                g.fill(getX(), itemY, getX() + width, itemY + itemHeight, this.hoverColor);
            }

            itemRenderer.render(g, items.get(optionIndex), getX() + 4, itemY, itemHeight, hovered);
        }

        if (items.size() > maxVisible) {
            int barHeight = dropHeight * maxVisible / items.size();
            int barY = dropY + scrollOffset * dropHeight / items.size();
            g.fill(getX() + width - 3, barY, getX() + width - 1, barY + barHeight, this.scrollBarColor);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        System.out.println("[CHECK][ScrollableWidget.mouseClicked] mouseClicked A");
        if (!this.active || !this.visible) return false;
        System.out.println("[CHECK][ScrollableWidget.mouseClicked] mouseClicked B");

        int dropY = getY();
        int visibleCount = Math.min(items.size(), maxVisible);
        for (int i = 0; i < visibleCount; i++) {
            int optionIndex = i + scrollOffset;
            int itemY = dropY + 1 + i * itemHeight;
            if (mouseX >= getX() && mouseX <= getX() + width
                    && mouseY >= itemY && mouseY < itemY + itemHeight) {
                selectedIndex = optionIndex;
                onSelect.accept(items.get(optionIndex));
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        scrollOffset = Math.max(0, Math.min(scrollOffset - (int) scrollY, Math.max(0, items.size() - maxVisible)));
        return true;
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        int dropBottom = getY() + 1 + Math.min(items.size(), maxVisible) * itemHeight + 2;
        return mouseX >= getX() && mouseX <= getX() + width
                && mouseY >= getY() && mouseY <= dropBottom;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        defaultButtonNarrationText(output);
    }

    public T getSelected() {
        return selectedIndex >= 0 && selectedIndex < items.size() ? items.get(selectedIndex) : null;
    }
}