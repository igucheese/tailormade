package com.tailormade.tailor.client.screen;

import com.tailormade.tailor.client.menu.DesignerMenu;
import com.tailormade.tailor.client.menu.ManagerMenu;
import com.tailormade.tailor.client.renderer.TailorArmorRenderLayer;
import com.tailormade.tailor.client.renderer.TailorTextureCompositor;
import com.tailormade.tailor.data.DesignDataRecord;
import com.tailormade.tailor.data.PatternType;
import com.tailormade.tailor.data.PixelData;
import com.tailormade.tailor.entities.blockentities.TailorBlockEntity;
import com.tailormade.tailor.entities.items.PatternItem;
import com.tailormade.tailor.network.payloads.*;
import com.tailormade.tailor.registries.ModDataComponents;
import com.tailormade.tailor.utils.DyeCostCalculator;
import com.tailormade.tailor.utils.files.DesignDataNbtImporter;
import com.tailormade.tailor.utils.files.NativeFileChooser;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

import static com.tailormade.tailor.Tailormade.MODID;
import static com.tailormade.tailor.client.menu.ManagerMenu.*;
import static com.tailormade.tailor.utils.DesignAccessor.getPixelDataFromId;

public class ManagerScreen extends AbstractContainerScreen<ManagerMenu> {
    private static final ResourceLocation GUI_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/manager_gui.png");
    private static final ResourceLocation BG_1 =
            ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/manager_gui_1.png");
    private static final ResourceLocation BG_2 =
            ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/manager_gui_2.png");
    private static final ResourceLocation BG_3 =
            ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/manager_gui_3.png");
    private static final ResourceLocation LOCKED_ICON =
            ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/pattern_locked_status.png");

    private static final int GUI_W = 224;
    private static final int GUI_H = 224;
    private static final int GUI_OFFSET_X = 144;
    private static final int GUI_OFFSET_Y = 144;

    private static final int LOCK_ICON_X = 16;
    private static final int LOCK_ICON_Y = 48;

    private static final int PV_X = 242;
    private static final int PV_Y = 6;
    private static final int PV_W = 124;
    private static final int PV_H = 124;

    private static final int BTN_W = 96;
    private static final int BTN_H = 16;

    private Button copyButton;
    private Button lockButton;

    // タブ用のもろもろ
    private int currentPage = 0;
    private static final int PAGE_COUNT = 3;
    private static final int COPY_BUTTON_X = 128;
    private static final int COPY_BUTTON_Y = 55;
    private static final int RENAME_BUTTON_X = 174;
    private static final int RENAME_BUTTON_Y = 85;
    private static final int LOCK_BUTTON_X = 103;
    private static final int LOCK_BUTTON_Y = 103;
    private static final int EXPORT_BUTTON_X = 170;
    private static final int EXPORT_BUTTON_Y = 103;
    private static final int SELECT_BUTTON_X = 24;
    private static final int SELECT_BUTTON_Y = 79;
    private static final int EXTRACT_BUTTON_X = 128;
    private static final int EXTRACT_BUTTON_Y = 96;
    private static final int MiniButtonWidth = 32;
    private static final int MiniButtonHeight = 9;
    private static final ResourceLocation BUTTON_COPY =
            ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/tmb_copy.png");
    private static final ResourceLocation BUTTON_COPY_ACTIVE =
            ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/tmb_copy_a.png");
    private static final ResourceLocation BUTTON_RENAME =
            ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/tmb_rename.png");
    private static final ResourceLocation BUTTON_RENAME_ACTIVE =
            ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/tmb_rename_a.png");
    private static final ResourceLocation BUTTON_LOCK =
            ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/tmb_lock.png");
    private static final ResourceLocation BUTTON_LOCK_ACTIVE =
            ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/tmb_lock_a.png");
    private static final ResourceLocation BUTTON_UNLOCK =
            ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/tmb_unlock.png");
    private static final ResourceLocation BUTTON_UNLOCK_ACTIVE =
            ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/tmb_unlock_a.png");
    private static final ResourceLocation BUTTON_EXPORT =
            ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/tmb_export.png");
    private static final ResourceLocation BUTTON_EXPORT_ACTIVE =
            ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/tmb_export_a.png");
    private static final ResourceLocation BUTTON_EXTRACT =
            ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/tmb_extract.png");
    private static final ResourceLocation BUTTON_EXTRACT_ACTIVE =
            ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/tmb_extract_a.png");
    private static final ResourceLocation BUTTON_SELECT =
            ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/tmb_select.png");
    private static final ResourceLocation BUTTON_SELECT_ACTIVE =
            ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/tmb_select_a.png");
    private static final ResourceLocation BUTTON_IMPORT =
            ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/tmb_import.png");
    private static final ResourceLocation BUTTON_IMPORT_ACTIVE =
            ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/tmb_import_a.png");

    private EditBox renameForm;

    public ManagerScreen(ManagerMenu menu, Inventory playerInv, Component title) {
        super(menu, playerInv, title);
        this.imageWidth = GUI_W;
        this.imageHeight = GUI_H;
    }

    @Override
    protected void init() {
        super.init();

        this.renameForm = new EditBox(this.font, leftPos + 84, topPos + 81, 88, 14, Component.translatable("gui.mezcraft.envelope.input.destination.title"));
        this.addRenderableWidget(this.renameForm);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g, mouseX, mouseY, partialTick);
        super.render(g, mouseX, mouseY, partialTick);
        renderTooltip(g, mouseX, mouseY);
        renderForms(g);
        renderButton(g);

        mouseMoved(g, partialTick, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
//        g.blit(GUI_TEXTURE, x, y, GUI_OFFSET_X, GUI_OFFSET_Y, imageWidth, imageHeight, 512, 512);
        if (this.currentPage == 0) {
            g.blit(BG_1, x, y, GUI_OFFSET_X, GUI_OFFSET_Y, imageWidth, imageHeight, 512, 512);
        } else if (this.currentPage == 1) {
            g.blit(BG_2, x, y, GUI_OFFSET_X, GUI_OFFSET_Y, imageWidth, imageHeight, 512, 512);
        } else if (this.currentPage == 2) {
            g.blit(BG_3, x, y, GUI_OFFSET_X, GUI_OFFSET_Y, imageWidth, imageHeight, 512, 512);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        if (this.currentPage == 0) {
            if (this.menu.isPatternSet() && this.menu.isLocked()) {
                g.blit(LOCKED_ICON, LOCK_ICON_X, LOCK_ICON_Y, 0, 0, 16, 16, 16, 16);
            }
        } else if (this.currentPage == 2) {
            if (this.menu.hasImport()) {
                DesignDataRecord imported = this.menu.getImported();
                String importedPatternName = imported.name();
                if (importedPatternName.length() > 6) {
                    importedPatternName = importedPatternName.substring(0, 6) + "...";
                }
                int textWidth = this.font.width(importedPatternName);
                g.drawString(this.font, importedPatternName, 40 - (textWidth / 2), 56, 0xdfc9a3, false);
                String typeName = imported.type();
                int typeWidth = this.font.width(typeName);
                g.drawString(this.font, typeName, 40 - (typeWidth / 2), 96, 0xdfc9a3, false);
            }
        }
    }

    private void renderForms(GuiGraphics g) {
        if (this.currentPage == 0) {
            this.renameForm.visible = true;
            this.renameForm.active = this.menu.isPatternSet();
        } else {
            this.renameForm.visible = false;
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.renameForm.isFocused()) {
            if (this.renameForm.keyPressed(keyCode, scanCode, modifiers)) {
                return true;
            }
            if (keyCode != GLFW.GLFW_KEY_ESCAPE) {
                return false;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void renderButton(GuiGraphics g) {
        if (this.currentPage == 0) {
            // コピー、エクスポート、ロック、リネーム
            if (this.menu.canCopy()) {
                g.blit(BUTTON_COPY, leftPos + COPY_BUTTON_X, topPos + COPY_BUTTON_Y, 0, 0, MiniButtonWidth, MiniButtonHeight, MiniButtonWidth, MiniButtonHeight);
            }
            g.blit(BUTTON_RENAME, leftPos + RENAME_BUTTON_X, topPos + RENAME_BUTTON_Y, 0, 0, MiniButtonWidth, MiniButtonHeight, MiniButtonWidth, MiniButtonHeight);
            g.blit(BUTTON_EXPORT, leftPos + EXPORT_BUTTON_X, topPos + EXPORT_BUTTON_Y, 0, 0, MiniButtonWidth, MiniButtonHeight, MiniButtonWidth, MiniButtonHeight);
            if (this.menu.isLocked()) {
                g.blit(BUTTON_UNLOCK, leftPos + LOCK_BUTTON_X, topPos + LOCK_BUTTON_Y, 0, 0, MiniButtonWidth, MiniButtonHeight, MiniButtonWidth, MiniButtonHeight);
            } else {
                g.blit(BUTTON_LOCK, leftPos + LOCK_BUTTON_X, topPos + LOCK_BUTTON_Y, 0, 0, MiniButtonWidth, MiniButtonHeight, MiniButtonWidth, MiniButtonHeight);
            }
        } else if (this.currentPage == 1) {
            g.blit(BUTTON_EXTRACT, leftPos + EXTRACT_BUTTON_X, topPos + EXTRACT_BUTTON_Y, 0, 0, MiniButtonWidth, MiniButtonHeight, MiniButtonWidth, MiniButtonHeight);
        } else if (this.currentPage == 2) {
            g.blit(BUTTON_SELECT, leftPos + SELECT_BUTTON_X, topPos + SELECT_BUTTON_Y, 0, 0, MiniButtonWidth, MiniButtonHeight, MiniButtonWidth, MiniButtonHeight);
            if (this.menu.hasImport() && this.menu.canImport()) {
                g.blit(BUTTON_IMPORT, leftPos + EXTRACT_BUTTON_X, topPos + EXTRACT_BUTTON_Y, 0, 0, MiniButtonWidth, MiniButtonHeight, MiniButtonWidth, MiniButtonHeight);
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int actualLeft = (this.width - 224) / 2;
        int actualTop = (this.height - 224) / 2;

        int startX = actualLeft;
        int startY = actualTop + 16;
        int columnWidth = 75;
        int rowHeight = 16;

        for (int i = 0; i < PAGE_COUNT; i++) {
            if (button == 0 && inBox(mouseX, mouseY, startX + (columnWidth * i), startY, columnWidth, rowHeight)) {
                this.navigate(i);
                return true;
            }
        }
        if (this.currentPage == 0) {
            if (button == 0 && inCopyBox(mouseX, mouseY)) {
                this.onCopy();
                return true;
            }
            if (button == 0 && inLockBox(mouseX, mouseY)) {
                this.onToggleLockedStatus();
                return true;
            }
            if (button == 0 && inRenameBox(mouseX, mouseY)) {
                this.onRenamePattern();
                return true;
            }
            if (button == 0 && inExportBox(mouseX, mouseY)) {
                this.onExport();
                return true;
            }
        } else if (this.currentPage == 1) {
            if (button == 0 && inExtractBox(mouseX, mouseY)) {
                this.onExtract();
                return true;
            }
        } else if (this.currentPage == 2) {
            if (button == 0 && inSelectBox(mouseX, mouseY)) {
                this.onSelect();
                return true;
            }
            if (button == 0 && inExtractBox(mouseX, mouseY) && this.menu.canImport()) {
                this.onImport();
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean mouseMoved(GuiGraphics g, float partialTick, int mx, int my) {
        int left = (this.width - this.imageWidth) / 2;
        int top = (this.height - this.imageHeight) / 2;

        if (this.currentPage == 0) {
            if (this.menu.canCopy()) {
                if (inCopyBox(mx, my)) {
                    g.blit(BUTTON_COPY_ACTIVE, leftPos + COPY_BUTTON_X, topPos + COPY_BUTTON_Y, 0, 0, MiniButtonWidth, MiniButtonHeight, MiniButtonWidth, MiniButtonHeight);
                    return true;
                } else {
                    g.blit(BUTTON_COPY, leftPos + COPY_BUTTON_X, topPos + COPY_BUTTON_Y, 0, 0, MiniButtonWidth, MiniButtonHeight, MiniButtonWidth, MiniButtonHeight);
                }
            }
            if (inRenameBox(mx, my)) {
                g.blit(BUTTON_RENAME_ACTIVE, leftPos + RENAME_BUTTON_X, topPos + RENAME_BUTTON_Y, 0, 0, MiniButtonWidth, MiniButtonHeight, MiniButtonWidth, MiniButtonHeight);
                return true;
            } else {
                g.blit(BUTTON_RENAME, leftPos + RENAME_BUTTON_X, topPos + RENAME_BUTTON_Y, 0, 0, MiniButtonWidth, MiniButtonHeight, MiniButtonWidth, MiniButtonHeight);
            }
            if (this.menu.isLocked()) {
                if (inLockBox(mx, my)) {
                    g.blit(BUTTON_UNLOCK_ACTIVE, leftPos + LOCK_BUTTON_X, topPos + LOCK_BUTTON_Y, 0, 0, MiniButtonWidth, MiniButtonHeight, MiniButtonWidth, MiniButtonHeight);
                    return true;
                } else {
                    g.blit(BUTTON_UNLOCK, leftPos + LOCK_BUTTON_X, topPos + LOCK_BUTTON_Y, 0, 0, MiniButtonWidth, MiniButtonHeight, MiniButtonWidth, MiniButtonHeight);
                }
            } else {
                if (inLockBox(mx, my)) {
                    g.blit(BUTTON_LOCK_ACTIVE, leftPos + LOCK_BUTTON_X, topPos + LOCK_BUTTON_Y, 0, 0, MiniButtonWidth, MiniButtonHeight, MiniButtonWidth, MiniButtonHeight);
                    return true;
                } else {
                    g.blit(BUTTON_LOCK, leftPos + LOCK_BUTTON_X, topPos + LOCK_BUTTON_Y, 0, 0, MiniButtonWidth, MiniButtonHeight, MiniButtonWidth, MiniButtonHeight);
                }
            }
            if (inExportBox(mx, my)) {
                g.blit(BUTTON_EXPORT_ACTIVE, leftPos + EXPORT_BUTTON_X, topPos + EXPORT_BUTTON_Y, 0, 0, MiniButtonWidth, MiniButtonHeight, MiniButtonWidth, MiniButtonHeight);
                return true;
            } else {
                g.blit(BUTTON_EXPORT, leftPos + EXPORT_BUTTON_X, topPos + EXPORT_BUTTON_Y, 0, 0, MiniButtonWidth, MiniButtonHeight, MiniButtonWidth, MiniButtonHeight);
            }
        } else if (this.currentPage == 1) {
            if (inExtractBox(mx, my)) {
                g.blit(BUTTON_EXTRACT_ACTIVE, leftPos + EXTRACT_BUTTON_X, topPos + EXTRACT_BUTTON_Y, 0, 0, MiniButtonWidth, MiniButtonHeight, MiniButtonWidth, MiniButtonHeight);
                return true;
            } else {
                g.blit(BUTTON_EXTRACT, leftPos + EXTRACT_BUTTON_X, topPos + EXTRACT_BUTTON_Y, 0, 0, MiniButtonWidth, MiniButtonHeight, MiniButtonWidth, MiniButtonHeight);
            }
        } else if (this.currentPage == 2) {
            if (inSelectBox(mx, my)) {
                g.blit(BUTTON_SELECT_ACTIVE, leftPos + SELECT_BUTTON_X, topPos + SELECT_BUTTON_Y, 0, 0, MiniButtonWidth, MiniButtonHeight, MiniButtonWidth, MiniButtonHeight);
                return true;
            } else {
                g.blit(BUTTON_SELECT, leftPos + SELECT_BUTTON_X, topPos + SELECT_BUTTON_Y, 0, 0, MiniButtonWidth, MiniButtonHeight, MiniButtonWidth, MiniButtonHeight);
            }
            if (this.menu.canImport()) {
                if (inExtractBox(mx, my)) {
                    g.blit(BUTTON_IMPORT_ACTIVE, leftPos + EXTRACT_BUTTON_X, topPos + EXTRACT_BUTTON_Y, 0, 0, MiniButtonWidth, MiniButtonHeight, MiniButtonWidth, MiniButtonHeight);
                    return true;
                } else {
                    g.blit(BUTTON_IMPORT, leftPos + EXTRACT_BUTTON_X, topPos + EXTRACT_BUTTON_Y, 0, 0, MiniButtonWidth, MiniButtonHeight, MiniButtonWidth, MiniButtonHeight);
                }
            }
        }
        return true;
    }

    private boolean inCopyBox(double mx, double my) {
        return inBox(mx, my, leftPos + COPY_BUTTON_X, topPos + COPY_BUTTON_Y, MiniButtonWidth, MiniButtonHeight);
    }
    private boolean inRenameBox(double mx, double my) {
        return inBox(mx, my, leftPos + RENAME_BUTTON_X, topPos + RENAME_BUTTON_Y, MiniButtonWidth, MiniButtonHeight);
    }
    private boolean inLockBox(double mx, double my) {
        return inBox(mx, my, leftPos + LOCK_BUTTON_X, topPos + LOCK_BUTTON_Y, MiniButtonWidth, MiniButtonHeight);
    }
    private boolean inExportBox(double mx, double my) {
        return inBox(mx, my, leftPos + EXPORT_BUTTON_X, topPos + EXPORT_BUTTON_Y, MiniButtonWidth, MiniButtonHeight);
    }
    private boolean inExtractBox(double mx, double my) {
        return inBox(mx, my, leftPos + EXTRACT_BUTTON_X, topPos + EXTRACT_BUTTON_Y, MiniButtonWidth, MiniButtonHeight);
    }
    private boolean inSelectBox(double mx, double my) {
        return inBox(mx, my, leftPos + SELECT_BUTTON_X, topPos + SELECT_BUTTON_Y, MiniButtonWidth, MiniButtonHeight);
    }

    private boolean inBox(double mx, double my, int bx, int by, int w, int h) {
        return mx >= bx && mx < bx + w && my >= by && my < by + h;
    }

    private void onCopy() {
        UUID currentPatternId = this.menu.getCurrentOriginalPatternId();
        if (currentPatternId == null) { return; }
//        PacketDistributor.sendToServer(new SavePatternLockPayload());
        ItemStack mainStack = menu.getPatternContainer().getItem(ManagerMenu.SLOT_COPY_PATTERN);
        if (mainStack.isEmpty() || !(mainStack.getItem() instanceof PatternItem)) return;

        PacketDistributor.sendToServer(
                new CopyDesignPayload(ManagerMenu.SLOT_COPY_PATTERN, currentPatternId)
        );
//        this.onClose();
    }

    private void onToggleLockedStatus() {
        UUID currentPatternId = this.menu.getCurrentOriginalPatternId();
        if (currentPatternId == null) { return; }
        PacketDistributor.sendToServer(
                new SavePatternLockPayload(currentPatternId, !this.menu.isLocked())
        );
    }

    private void onRenamePattern() {
        UUID currentPatternId = this.menu.getCurrentOriginalPatternId();
        String newName = this.renameForm.getValue();
        if (currentPatternId == null || newName.isBlank()) { return; }
        PacketDistributor.sendToServer(
                new RenamePatternPayload(currentPatternId, newName)
        );
    }

    private void onExport() {
        UUID currentPatternId = this.menu.getCurrentOriginalPatternId();
        if (currentPatternId == null) { return; }
        PacketDistributor.sendToServer(
                new ExportDesignDataStartPayload(currentPatternId)
        );
    }

    private void onExtract() {
        if (!this.menu.canExtract()) {
            return;
        }
        UUID currentPatternId = this.menu.getCurrentExtractPatternId();
        if (currentPatternId == null) { return; }
        PacketDistributor.sendToServer(
                new ExtractDesignPayload(currentPatternId)
        );
    }

    private void onSelect() {
        NativeFileChooser.openNbtFileDialogAsync(selected -> {
            if (selected == null) {
                return; // キャンセル or エラー
            }
            try {
                CompoundTag tagFromFile = NbtIo.read(selected);
                if (tagFromFile == null) { return; }
                DesignDataRecord importedRecord = DesignDataNbtImporter.fromNbt(tagFromFile);
                this.menu.setImportedData(importedRecord);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void onImport() {
        DesignDataRecord imported = this.menu.getImported();
        if (!this.menu.canImport() || imported == null) {
            return;
        }
        PacketDistributor.sendToServer(new ImportDesignPayload(imported));
    }

    @Override
    public void removed() {
        super.removed();
    }

    // タブ切り替え
    public void navigate(int content) {
        this.currentPage = content;
        PacketDistributor.sendToServer(new ChangeManagerMenuTabPayload(content));
    }

    @Override
    protected void containerTick() {
        super.containerTick();

        // 型紙スロットが空ならリネームフォームを空にする
        if (!this.menu.isPatternSet()) {
            this.renameForm.setValue("");
            this.renameForm.setFocused(false);
        }
    }
}
