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

import java.util.EnumMap;
import java.util.Map;

import static com.tailormade.tailor.Tailormade.MODID;
import static com.tailormade.tailor.client.menu.TailorMenu.*;
import static com.tailormade.tailor.utils.DesignAccessor.getPixelDataFromId;

public class TailorScreen extends AbstractContainerScreen<TailorMenu> {

    // テクスチャ
    private static final ResourceLocation GUI_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/tailor_gui.png");

    // GUI サイズ（テクスチャに合わせて調整）
    private static final int GUI_W = 384;
    private static final int GUI_H = 216;
    private static final int GUI_OFFSET_X = 64;
    private static final int GUI_OFFSET_Y = 148;

    // プレビュー領域（右半分）
    private static final int PV_X = 242;
    private static final int PV_Y = 6;
    private static final int PV_W = 124;
    private static final int PV_H = 124;

    private EditBox nameInput;
    private EditBox serialInput;

    // 確定ボタン
    private static final int BTN_W = 100;
    private static final int BTN_H = 20;

    // タンクゲージ（各染料スロットの右横）
    private static final int GAUGE_W  = 4;
    private static final int GAUGE_H  = 14;
    private static final int GAUGE_OX = 20; // スロットX からの相対オフセット

    // ---- フィールド -------------------------------------------

    private Button confirmButton;
    private TailorTextureCompositor previewCompositor;

    // ---- コンストラクタ ----------------------------------------

    public TailorScreen(TailorMenu menu, Inventory playerInv, Component title) {
        super(menu, playerInv, title);
        this.imageWidth  = GUI_W;
        this.imageHeight = GUI_H;
    }

    // ---- 初期化 -----------------------------------------------

    @Override
    protected void init() {
        super.init();

        previewCompositor = TailorTextureCompositor.createForPreview();

        int btnX = leftPos + PV_X + (PV_W - BTN_W) / 2;
        int btnY = topPos  + PV_Y + PV_H + 4 + 50;

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

    // ---- 描画 -------------------------------------------------

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g, mouseX, mouseY, partialTick);
        super.render(g, mouseX, mouseY, partialTick);

//        renderDyeGauges(g);
        renderDyeCostHint(g);
        renderPreview(g, mouseX, mouseY);

        this.nameInput.active = menu.canConfirm();
        this.serialInput.active = menu.canConfirm();

        confirmButton.active = menu.canConfirm();

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

    // ---- 染料ゲージ -------------------------------------------

    private void renderDyeGauges(GuiGraphics g) {
        if (menu.getBlockEntity() == null) return;

        int maxTank = com.tailormade.tailor.entities.blockentities.TailorBlockEntity.TANK_MAX;
        renderGauge(g, 3, menu.getBlockEntity().getTankR(), maxTank, 0xFFFF4444,
                leftPos + 62 + GAUGE_OX, topPos + 54);
        renderGauge(g, 4, menu.getBlockEntity().getTankG(), maxTank, 0xFF44FF44,
                leftPos + 62 + GAUGE_OX, topPos + 72);
        renderGauge(g, 5, menu.getBlockEntity().getTankB(), maxTank, 0xFF4444FF,
                leftPos + 62 + GAUGE_OX, topPos + 90);
    }

    private void renderGauge(GuiGraphics g, int slotOffset, int current, int max,
                             int color, int x, int y) {
        // 背景（黒）
        g.fill(x, y, x + GAUGE_W, y + GAUGE_H, 0xFF000000);
        // 充填（下から上）
        int fillH = (int)((float) current / max * GAUGE_H);
        if (fillH > 0) {
            g.fill(x, y + GAUGE_H - fillH, x + GAUGE_W, y + GAUGE_H, color);
        }
    }

    // ---- 必要コストのヒント -----------------------------------

    /** 型紙スロットに型紙が入っているとき、必要な染料コストをスロット横に表示 */
    private void renderDyeCostHint(GuiGraphics g) {
        ItemStack patternStack = menu.getSlot(0).getItem();
        DyeCostCalculator.DyeCost cost;
        if (patternStack.isEmpty() || !(patternStack.getItem() instanceof PatternItem)) {
            cost = new DyeCostCalculator.DyeCost(0, 0, 0);
        } else {
//            PixelData pd = patternStack.get(ModDataComponents.PIXEL_DATA.get());
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

    // ---- 3D プレビュー ----------------------------------------

    private void renderPreview(GuiGraphics g, int mouseX, int mouseY) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        ItemStack patternStack = menu.getSlot(0).getItem();
        ItemStack armorStack   = menu.getSlot(1).getItem();

        // 型紙と防具が両方揃っているときのみプレビュー表示
        if (patternStack.isEmpty() || armorStack.isEmpty()) return;
        if (!(patternStack.getItem() instanceof PatternItem patternItem)) return;

//        PixelData pd = patternStack.get(ModDataComponents.PIXEL_DATA.get());
        PixelData pd = getPixelDataFromId(patternStack.get(ModDataComponents.PATTERN_ID.get()));
        if (pd == null) return;

        // プレビュー用ピクセルマップ構築
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
                    topPos  + PV_Y,
                    leftPos + PV_X + PV_W,
                    topPos  + PV_Y + PV_H,
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

    // ---- 確定 -------------------------------------------------

    private void onConfirm() {
        if (!menu.canConfirm()) return;
        String name = this.nameInput.getValue();
        String serial = this.serialInput.getValue();
        String playerName = minecraft.player.getName().getString();
        PacketDistributor.sendToServer(new ConfirmTailorPayload(name, serial, playerName));
    }

    // ---- ライフサイクル ----------------------------------------

    @Override
    public void removed() {
        if (previewCompositor != null) previewCompositor.close();
        super.removed();
    }
}
