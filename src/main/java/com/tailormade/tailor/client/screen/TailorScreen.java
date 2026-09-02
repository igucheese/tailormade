package com.tailormade.tailor.client.screen;

import com.tailormade.tailor.client.menu.TailorMenu;
import com.tailormade.tailor.client.renderer.TailorArmorRenderLayer;
import com.tailormade.tailor.client.renderer.TailorTextureCompositor;
import com.tailormade.tailor.data.PatternType;
import com.tailormade.tailor.data.PixelData;
import com.tailormade.tailor.entities.blockentities.TailorBlockEntity;
import com.tailormade.tailor.entities.items.PatternItem;
import com.tailormade.tailor.network.payloads.ConfirmTailorPayload;
import com.tailormade.tailor.registries.ModDataComponents;
import com.tailormade.tailor.utils.DyeCostCalculator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.ContainerListener;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

import java.util.EnumMap;
import java.util.Map;

import static com.tailormade.tailor.Tailormade.MODID;
import static com.tailormade.tailor.client.menu.TailorMenu.*;
import static com.tailormade.tailor.utils.DesignAccessor.getPixelDataFromId;

public class TailorScreen extends AbstractContainerScreen<TailorMenu> {
    private static final ResourceLocation GUI_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/tailor_gui.png");

    private static final int GUI_W = 384;
    private static final int GUI_H = 216;
    private static final int GUI_OFFSET_X = 64;
    private static final int GUI_OFFSET_Y = 148;

    private static final int PV_X = 242;
    private static final int PV_Y = 6;
    private static final int PV_W = 124;
    private static final int PV_H = 124;

    private EditBox nameInput;
    private EditBox serialInput;

    private static final int BTN_W = 100;
    private static final int BTN_H = 20;

    private Button confirmButton;
    private TailorTextureCompositor previewCompositor;

    public TailorScreen(TailorMenu menu, Inventory playerInv, Component title) {
        super(menu, playerInv, title);
        this.imageWidth = GUI_W;
        this.imageHeight = GUI_H;
    }

    @Override
    protected void init() {
        super.init();
        previewCompositor = TailorTextureCompositor.createForPreview();

        int btnX = leftPos + PV_X + (PV_W - BTN_W) / 2;
        int btnY = topPos + PV_Y + PV_H + 4 + 50;

        this.nameInput = new EditBox(this.font, leftPos + PV_X, topPos + PV_Y + PV_H + 5, PV_W, 20, Component.translatable("gui.tailormade.tailor.name.placeholder"));
        this.nameInput.setMaxLength(15);
        this.nameInput.setHint(Component.translatable("gui.tailormade.tailor.name.placeholder"));
        this.addRenderableWidget(this.nameInput);

        this.serialInput = new EditBox(this.font, leftPos + PV_X, topPos + PV_Y + PV_H + 30, PV_W, 20, Component.translatable("gui.tailormade.tailor.serial.placeholder"));
        this.serialInput.setMaxLength(15);
        this.serialInput.setHint(Component.translatable("gui.tailormade.tailor.serial.placeholder"));
        this.addRenderableWidget(this.serialInput);

        confirmButton = Button.builder(
                Component.translatable("gui.tailormade.modal.confirm"),
                btn -> onConfirm()
        ).pos(btnX, btnY).size(BTN_W, BTN_H).build();

        addRenderableWidget(confirmButton);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g, mouseX, mouseY, partialTick);
        super.render(g, mouseX, mouseY, partialTick);
        renderDyeCostHint(g);
        renderPreview(g, mouseX, mouseY);

        boolean canProceed = menu.canConfirm() && menu.canTailor();

        this.nameInput.active = canProceed;
        this.serialInput.active = canProceed;
        confirmButton.active = canProceed;

        renderTooltip(g, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        g.blit(GUI_TEXTURE, x, y, GUI_OFFSET_X, GUI_OFFSET_Y, imageWidth, imageHeight, 512, 512);
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        int tankR = menu.getBlockEntity().getTankR();
        int tankG = menu.getBlockEntity().getTankG();
        int tankB = menu.getBlockEntity().getTankB();
        int max = TailorBlockEntity.TANK_MAX;

        int xBuffer = 42;
        g.drawString(this.font, Component.literal("/" + tankR), SLOT_DYE_R_X + xBuffer, SLOT_DYE_R_Y + 5, 0x444444, false);
        g.drawString(this.font, Component.literal("/" + tankG), SLOT_DYE_G_X + xBuffer, SLOT_DYE_G_Y + 5, 0x444444, false);
        g.drawString(this.font, Component.literal("/" + tankB), SLOT_DYE_B_X + xBuffer, SLOT_DYE_B_Y + 5, 0x444444, false);
    }

    private void renderDyeCostHint(GuiGraphics g) {
        ItemStack patternStack = menu.getSlot(0).getItem();
        DyeCostCalculator.DyeCost cost;
        if (patternStack.isEmpty() || !(patternStack.getItem() instanceof PatternItem)) {
            cost = new DyeCostCalculator.DyeCost(0, 0, 0);
        } else {
            PixelData pd = getPixelDataFromId(patternStack.get(ModDataComponents.PATTERN_ID.get()));
            if (pd == null) {
                cost = new DyeCostCalculator.DyeCost(0, 0, 0);
            } else {
                cost = DyeCostCalculator.calculate(pd.pixels());
            }
        }

        int hintX = this.leftPos + SLOT_DYE_R_X + 26;
        int hintYBuffer = this.topPos + 5;
        g.drawString(font, String.valueOf(cost.red()),   hintX, hintYBuffer + SLOT_DYE_R_Y, 0xFFFF4444, false);
        g.drawString(font, String.valueOf(cost.green()), hintX, hintYBuffer + SLOT_DYE_G_Y, 0xFF44FF44, false);
        g.drawString(font, String.valueOf(cost.blue()),  hintX, hintYBuffer + SLOT_DYE_B_Y, 0xFF4444FF, false);
    }

    private void renderPreview(GuiGraphics g, int mouseX, int mouseY) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        ItemStack patternStack = menu.getSlot(0).getItem();
        ItemStack armorStack = menu.getSlot(1).getItem();

        if (patternStack.isEmpty() || armorStack.isEmpty()) return;
        if (!(patternStack.getItem() instanceof PatternItem patternItem)) return;

        PixelData pd = getPixelDataFromId(patternStack.get(ModDataComponents.PATTERN_ID.get()));
        if (pd == null) return;

        Map<PatternType, int[]> pixelMap = new EnumMap<>(PatternType.class);
        PatternType type = patternItem.getPatternType(patternStack);
        pixelMap.put(type, pd.pixels());

        ResourceLocation previewTex = previewCompositor.composeForPreview(pixelMap);
        if (previewTex != null) {
            TailorArmorRenderLayer.setPreviewOverride(previewTex);
        }

        try {
            InventoryScreen.renderEntityInInventoryFollowsMouse(
                    g,
                    leftPos + PV_X,
                    topPos + PV_Y,
                    leftPos + PV_X + PV_W,
                    topPos + PV_Y + PV_H,
                    65,
                    0.0625f,
                    (float) mouseX,
                    (float) mouseY,
                    mc.player
            );
        } finally {
            TailorArmorRenderLayer.clearPreviewOverride();
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.nameInput.isFocused() || this.serialInput.isFocused()) {
            if (this.nameInput.keyPressed(keyCode, scanCode, modifiers) || this.serialInput.keyPressed(keyCode, scanCode, modifiers)) {
                return true;
            }
            if (keyCode != GLFW.GLFW_KEY_ESCAPE) {
                return false;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void onConfirm() {
        if (!menu.canConfirm() || !menu.canTailor()) return;
        String name = this.nameInput.getValue();
        String serial = this.serialInput.getValue();
        String playerName = minecraft.player.getName().getString();
        PacketDistributor.sendToServer(new ConfirmTailorPayload(name, serial, playerName));
    }

    @Override
    public void removed() {
        if (previewCompositor != null) previewCompositor.close();
        super.removed();
    }
}
