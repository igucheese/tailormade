package com.tailormade.tailor.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.tailormade.tailor.client.gui.ColorPalette;
import com.tailormade.tailor.client.gui.ColorPickerWidget;
import com.tailormade.tailor.client.gui.PowderRoomEditableRegions;
import com.tailormade.tailor.client.menu.DesignerMenu;
import com.tailormade.tailor.client.renderer.SkinLayerRenderLayer;
import com.tailormade.tailor.client.renderer.TailorTextureCompositor;
import com.tailormade.tailor.data.*;
import com.tailormade.tailor.entities.items.PatternItem;
import com.tailormade.tailor.network.payloads.SaveSkinLayerPayload;
import com.tailormade.tailor.registries.ModDataComponents;
import com.tailormade.tailor.utils.MannequinStylePreviewHelper;
import com.tailormade.tailor.utils.PixelCanvas;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;

import static com.tailormade.tailor.Tailormade.MODID;
import static com.tailormade.tailor.data.Constants.TRANSPARENT;

public class PowderRoomScreen extends Screen {

    private static final ResourceLocation GUI_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/powder_room_gui.png");
    private static final ResourceLocation BRUSH_1_ICON =
            ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/brush_1.png");
    private static final ResourceLocation BRUSH_2_ICON =
            ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/brush_2.png");
    private static final ResourceLocation BRUSH_3_ICON =
            ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/brush_3.png");
    private static final ResourceLocation BUCKET_ICON =
            ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/bucket.png");
    private static final ResourceLocation EYEDROPPER_ICON =
            ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/eyedropper.png");

    private static final int GUI_W = 384;
    private static final int GUI_H = 216;
    private static final int GUI_OFFSET_X = 64;
    private static final int GUI_OFFSET_Y = 148;

    // 2D エディタ領域
    private static final int ED_X = 28;
    private static final int ED_Y = 6;
    private static final int ED_W = 188;
    private static final int ED_H = 188;

    // プレビュー領域
    private static final int PV_X = 248;
    private static final int PV_Y = 6;
    private static final int PV_W = 124;
    private static final int PV_H = 172;

    // パレット
    private static final int PAL_X = 8;
    private static final int PAL_Y = 10;

    // ツールバー
    private static final int TOOLBAR_X = 223;
    private static final int TOOLBAR_Y = 10;
    private static int BRUSH_SIZE = 1;
    private static String TOOL_MODE = "brush";

    private static final int RGB_Y_OFFSET = 4;  // エディタ下端からの距離
    private static final int RGB_BOX_W = 28;
    private static final int RGB_BOX_H = 10;

    private int leftPos;
    private int topPos;
    private int imageWidth;
    private int imageHeight;

    private String clickedArea = null;

    /** 肌色テクスチャ（64×64）を編集するキャンバス */
    private PixelCanvas canvas;
    private ColorPalette palette;
    private ColorPickerWidget colorPicker;

    private EditBox rBox, gBox, bBox;
    private Button saveButton;
    private String beforeEyedropperTool = null;

    // プレビュー回転
    private float previewYaw   = 235.0f; // 正面が見える初期値
    private float previewPitch = 0.0f;
    private double lastDragX;
    private boolean draggingPreview = false;
    private double dragStartX = -1;
    private double dragStartY = -1;

    // プレビュー用の下着設定（現在選択中のもの）
    private UnderwearSetting previewUnderwear = UnderwearSetting.DEFAULT;
    private UnderwearType selectedType  = UnderwearType.MALE_BOXER;
    private DyeColor selectedColor = DyeColor.WHITE;

    private TailorTextureCompositor previewCompositor;
    private boolean hasUnsavedChanges = false;

    public PowderRoomScreen() {
        super(Component.translatable("gui.tailormade.powder_room"));
        this.imageWidth  = GUI_W;
        this.imageHeight = GUI_H;
    }

    @Override
    protected void init() {
        leftPos = (width  - GUI_W) / 2;
        topPos  = (height - GUI_H) / 2;

        previewCompositor = TailorTextureCompositor.createForPreview();

        // キャンバス（64×64 = スキン全体）
        canvas = new PixelCanvas(64, 64);

        // 既存の肌色データをロード
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            PixelData existingData = SkinDataClientCache.get(mc.player.getUUID());
            int[] existing = existingData != null ? existingData.getPixels() : null;
            if (existing != null) {
                canvas.loadPixels(existing);
            } else {
                // デフォルト: 頭部スキンから肌色をサンプリングして塗りつぶし
                fillWithSampledSkinColor(mc.player);
            }

            UnderwearSetting current = UnderwearDataClientCache.get(mc.player.getUUID());
            if (current != null) {
                selectedType  = current.type();
                selectedColor = current.color();
                this.previewUnderwear = current;
                System.out.println("[CHECK][CURRENT UNDERWARE TYPE] : " + selectedType + ", COLOR: " + selectedColor);
            }
        }

        canvas.init();
        PowderRoomEditableRegions.lockNonEditablePixels(canvas);
        canvas.setIsSkin(true);

        // パレット
        palette = new ColorPalette(leftPos + PAL_X, topPos + PAL_Y);
        colorPicker = new ColorPickerWidget(leftPos + PAL_X, topPos + PAL_Y + 8 * 9 + 4);
        colorPicker.init();

        // RGB EditBox
        int rgbBaseX = leftPos + 14;
        int rgbY     = topPos  + ED_Y + ED_H + RGB_Y_OFFSET;
        rBox = makeRgbBox(rgbBaseX, rgbY, "R");
        gBox = makeRgbBox(rgbBaseX + RGB_BOX_W + 6, rgbY, "G");
        bBox = makeRgbBox(rgbBaseX + (RGB_BOX_W * 2) + 12, rgbY, "B");
        rBox.setResponder(s -> onRgbEdited());
        gBox.setResponder(s -> onRgbEdited());
        bBox.setResponder(s -> onRgbEdited());
        addRenderableWidget(rBox);
        addRenderableWidget(gBox);
        addRenderableWidget(bBox);

        // SAVE ボタン
        int saveX = leftPos + PV_X + PV_W - 63;
        int saveY = topPos  + PV_Y + PV_H + 21;
        saveButton = Button.builder(Component.translatable("gui.tailormade.designer.save"), btn -> onSave())
                .pos(saveX, saveY)
                .size(65, 24)
                .build();
        addRenderableWidget(saveButton);
    }

    private EditBox makeRgbBox(int x, int y, String hint) {
        EditBox box = new EditBox(font, x, y, RGB_BOX_W, RGB_BOX_H, Component.literal(hint));
        box.setMaxLength(3);
        box.setValue("0");
        return box;
    }

    // ---- デフォルト肌色サンプリング ---------------------------

    /**
     * プレイヤーのスキンテクスチャから顔部分の代表色をサンプリングし、
     * 全体を塗りつぶす。
     * スキンが取得できない場合は #C8A882（デフォルト肌色）を使う。
     */
    private void fillWithSampledSkinColor(net.minecraft.client.player.AbstractClientPlayer player) {
        int skinColor = 0xFFC8A882; // フォールバック

        try {
            var texture = Minecraft.getInstance()
                    .getTextureManager()
                    .getTexture(player.getSkin().texture());
            if (texture instanceof net.minecraft.client.renderer.texture.DynamicTexture dt
                    && dt.getPixels() != null) {
                // 顔 UV: x=9, y=9 付近の1ピクセル（ABGR → ARGB 変換）
                int abgr = dt.getPixels().getPixelRGBA(9, 9);
                int a = (abgr >> 24) & 0xFF;
                int b = (abgr >> 16) & 0xFF;
                int g = (abgr >>  8) & 0xFF;
                int r =  abgr        & 0xFF;
                skinColor = (a << 24) | (r << 16) | (g << 8) | b;
            }
        } catch (Exception ignored) {}

        Arrays.fill(canvas.getPixels(), skinColor);
    }

    private void clearHeadArea() {
        PatternType head = PatternType.HEAD;
        for (PatternType.CanvasSegment seg : head.getSegments()) {
            for (int y = 0; y < seg.h(); y++) {
                for (int x = 0; x < seg.w(); x++) {
                    int px = seg.uvX() + x;
                    int py = seg.uvY() + y;
                    // canvas は 64x64 のフラット配列
                    canvas.setPixel(px, py, PixelCanvas.TRANSPARENT, 1);
                }
            }
        }
    }

    // ---- 描画 -------------------------------------------------

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBg(g, partialTick, mouseX, mouseY);
        palette.render(g, mouseX, mouseY);
        colorPicker.render(g, mouseX, mouseY);

        renderEditor(g, mouseX, mouseY);
        renderPreview(g, mouseX, mouseY);
        renderRgbLabels(g);
        renderRgbBoxes(g, mouseX, mouseY, partialTick);
        renderToolabr(g);

        if (canvas != null) canvas.uploadIfDirty();
        saveButton.render(g, mouseX, mouseY, partialTick);
    }

    private void renderToolabr(GuiGraphics g)
    {
        int toolBarX = this.leftPos + TOOLBAR_X;
        int toolBarY = this.topPos + TOOLBAR_Y;
        // ツールバー
        g.blit(BRUSH_1_ICON, toolBarX, toolBarY, 0, 0, 10, 10, 10, 10);
        g.blit(BRUSH_2_ICON, toolBarX, toolBarY + 13, 0, 0, 10, 10, 10, 10);
        g.blit(BRUSH_3_ICON, toolBarX, toolBarY + 26, 0, 0, 10, 10, 10, 10);
        g.blit(BUCKET_ICON, toolBarX, toolBarY + 39, 0, 0, 10, 10, 10, 10);
        g.blit(EYEDROPPER_ICON, toolBarX, toolBarY + 52, 0, 0, 10, 10, 10, 10);

        // アクティブ枠
        int labelBorderColor = 0xFF44FF44;
        int toolBarActiveWidth = 12;
        int tollBarActiveX = toolBarX - 1;
        int toolBarActiveY = TOOL_MODE == "eyedropper" ? toolBarY + 51 : TOOL_MODE == "bucket" ? toolBarY + 38 : BRUSH_SIZE == 1 ? toolBarY - 1 : BRUSH_SIZE == 2 ? toolBarY + 12 : BRUSH_SIZE == 3 ? toolBarY + 25 : toolBarY - 1;
        g.fill(tollBarActiveX, toolBarActiveY, tollBarActiveX + toolBarActiveWidth, toolBarActiveY + 1, labelBorderColor);
        g.fill(tollBarActiveX,  toolBarActiveY + toolBarActiveWidth - 1, tollBarActiveX + toolBarActiveWidth, toolBarActiveY + toolBarActiveWidth, labelBorderColor);
        g.fill(tollBarActiveX, toolBarActiveY, tollBarActiveX + 1, toolBarActiveY + toolBarActiveWidth, labelBorderColor);
        g.fill(tollBarActiveX + toolBarActiveWidth - 1, toolBarActiveY, tollBarActiveX + toolBarActiveWidth, toolBarActiveY + toolBarActiveWidth, labelBorderColor);
    }

    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - GUI_W) / 2;
        int y = (this.height - GUI_H) / 2;
        g.fill(0, 0, this.width, this.height, 0x80000000);
        g.blit(GUI_TEXTURE, x, y, GUI_OFFSET_X, GUI_OFFSET_Y, GUI_W, GUI_H, 512, 512);
    }

    private void renderEditor(GuiGraphics g, int mouseX, int mouseY) {
        if (canvas == null) return;

        float scale  = Math.min((float) ED_W / 64, (float) ED_H / 64);
        int renderW  = (int)(64 * scale);
        int renderH  = (int)(64 * scale);
        int renderX  = leftPos + ED_X + (ED_W - renderW) / 2;
        int renderY  = topPos  + ED_Y + (ED_H - renderH) / 2;

        RenderSystem.enableBlend();
        g.blit(canvas.getTextureLocation(), renderX, renderY, 0, 0, renderW, renderH, renderW, renderH);
        RenderSystem.disableBlend();

        if (scale >= 4.0f) drawGrid(g, renderX, renderY, renderW, renderH, scale);

        int[] hoverPx = screenToPixel(mouseX, mouseY, renderX, renderY, scale);
        if (hoverPx != null) {
            int hx = renderX + (int)(hoverPx[0] * scale);
            int hy = renderY + (int)(hoverPx[1] * scale);
            g.fill(hx, hy, hx + (int)scale, hy + (int)scale, 0x55FFFFFF);
        }
    }

    private void drawGrid(GuiGraphics g, int rx, int ry, int rw, int rh, float scale) {
        int gridColor = 0x33FFFFFF;
        for (int x = 0; x <= canvas.getWidth(); x++) {
            int lx = rx + (int)(x * scale);
            g.fill(lx, ry, lx + 1, ry + rh, gridColor);
        }
        for (int y = 0; y <= canvas.getHeight(); y++) {
            int ly = ry + (int)(y * scale);
            g.fill(rx, ly, rx + rw, ly + 1, gridColor);
        }
    }

    private void renderPreview(GuiGraphics g, int mouseX, int mouseY) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        // 肌色テクスチャを全部位で合成してプレビューセット
        if (canvas != null) {
            Map<PatternType, int[]> pixelMap = buildPreviewPixelMap();
//            int[] pixels = canvas.getPixels();
//            for (PatternType type : PatternType.values()) {
//                pixelMap.put(type, cropPixels(pixels, type));
//            }
            ResourceLocation skinTex = previewCompositor.composeForPreview(pixelMap);
            SkinLayerRenderLayer.setSkinPreview(skinTex);
        }
        SkinLayerRenderLayer.setUnderwearPreview(previewUnderwear);

        MannequinStylePreviewHelper.setHideArmor(true);

        float savedXRot  = mc.player.getXRot();
        float savedXRotO = mc.player.xRotO;
        mc.player.setXRot(previewPitch);
        mc.player.xRotO = previewPitch;

        try {
            Quaternionf pose = new Quaternionf()
                    .rotateZ((float) Math.PI)
                    .rotateY((float) Math.toRadians(previewYaw));
            Quaternionf camera = new Quaternionf()
                    .rotateX((float) Math.toRadians(previewPitch));
            int centerX = leftPos + PV_X + PV_W / 2;
            int centerY = topPos  + PV_Y + PV_H / 2 + 50;

            InventoryScreen.renderEntityInInventory(
                    g,
                    centerX,
                    centerY,
                    65,
                    new Vector3f(0, 0, 0),
                    pose,
                    null,
                    mc.player
            );
        } finally {
            mc.player.setXRot(savedXRot);
            mc.player.xRotO = savedXRotO;
            SkinLayerRenderLayer.clearPreview();
            MannequinStylePreviewHelper.setHideArmor(false);
        }
    }

    private Map<PatternType, int[]> buildPreviewPixelMap() {
        Map<PatternType, int[]> map = new EnumMap<>(PatternType.class);

        int[] pixels = canvas.getPixels();
        for (PatternType type : PatternType.values()) {
            map.put(type, cropPixels(pixels, type));
        }

        return map;
    }

    private void renderRgbLabels(GuiGraphics g) {
        int ly = topPos + ED_Y + ED_H + RGB_Y_OFFSET;
        g.drawString(font, "R", leftPos + 8, ly + 1, 0xFFFFFF, false);
        g.drawString(font, "G", leftPos + 8 + RGB_BOX_W  + 6, ly + 1, 0xFFFFFF, false);
        g.drawString(font, "B", leftPos + 8 + (RGB_BOX_W * 2) + 12, ly + 1, 0xFFFFFF, false);
    }

    private void renderRgbBoxes(GuiGraphics g, int mx, int my, float partialTick) {
        rBox.render(g, mx, my, partialTick);
        gBox.render(g, mx, my, partialTick);
        bBox.render(g, mx, my, partialTick);
    }

    // ---- マウスイベント ----------------------------------------

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (palette.mouseClicked(mx, my)) {
            clickedArea = "palette";
            syncRgbBoxes();
            if (!palette.isEraserMode()) {
                colorPicker.setBaseColor(palette.getSelectedColor());
            }
            return true;
        }
        if (colorPicker.mousePressed(mx, my)) {
            int picked = colorPicker.getSelectedColor();
            palette.setRgb(
                    (picked >> 16) & 0xFF,
                    (picked >>  8) & 0xFF,
                    picked        & 0xFF
            );
            syncRgbBoxes();
            return true;
        }
        if (button == 0 && inEditorArea(mx, my)) {
            canvas.snapshot();
            clickedArea = "editor";
            applyBrush(mx, my);
            return true;
        }
        if (button == 0 && inPreviewArea(mx, my)) {
            dragStartX = mx;
            dragStartY = my;
            clickedArea = "preview";
            return true;
        }
        if (inToolbar(mx, my)) {
            clickedArea = "toolbar";
            mouseClickedOnToolBar(mx, my);
            return true;
        }

        clickedArea = null;
        return super.mouseClicked(mx, my, button);
    }

    private void mouseClickedOnToolBar(double mx, double my) {
        if (mx >= (this.leftPos + TOOLBAR_X) && mx <= (this.leftPos + TOOLBAR_X + 10) && my >= (this.topPos + TOOLBAR_Y) && my <= (this.topPos + TOOLBAR_Y + 10)) {
            BRUSH_SIZE = 1;
            TOOL_MODE = "brush";
        } else if (mx >= (this.leftPos + TOOLBAR_X) && mx <= (this.leftPos + TOOLBAR_X + 10) && my >= (this.topPos + TOOLBAR_Y + 13) && my <= (this.topPos + TOOLBAR_Y + 23)) {
            BRUSH_SIZE = 2;
            TOOL_MODE = "brush";
        } else if (mx >= (this.leftPos + TOOLBAR_X) && mx <= (this.leftPos + TOOLBAR_X + 10) && my >= (this.topPos + TOOLBAR_Y + 26) && my <= (this.topPos + TOOLBAR_Y + 36)) {
            BRUSH_SIZE = 3;
            TOOL_MODE = "brush";
        } else if (mx >= (this.leftPos + TOOLBAR_X) && mx <= (this.leftPos + TOOLBAR_X + 10) && my >= (this.topPos + TOOLBAR_Y + 39) && my <= (this.topPos + TOOLBAR_Y + 49)) {
            TOOL_MODE = "bucket";
        } else if (mx >= (this.leftPos + TOOLBAR_X) && mx <= (this.leftPos + TOOLBAR_X + 10) && my >= (this.topPos + TOOLBAR_Y + 52) && my <= (this.topPos + TOOLBAR_Y + 62)) {
            beforeEyedropperTool = TOOL_MODE;
            TOOL_MODE = "eyedropper";
        }
        System.out.println("[CHECK][BRUSH SIZE]" + BRUSH_SIZE + " " + TOOL_MODE);
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        if (button == 0 && inEditorArea(mx, my) && clickedArea == "editor") { applyBrush(mx, my); return true; }
        if (button == 0 && dragStartX >= 0) {
            previewYaw   += (float)(mx - dragStartX) * 1.0f;
            previewPitch  = Math.clamp(
                    previewPitch + (float)(my - dragStartY) * 0.5f,
                    -180.0f, 180.0f
            );
            dragStartX = mx;
            dragStartY = my;
            return true;
        }
        if (colorPicker.mouseDragged(mx, my)) {
            int picked = colorPicker.getSelectedColor();
            palette.setRgb(
                    (picked >> 16) & 0xFF,
                    (picked >>  8) & 0xFF,
                    picked        & 0xFF
            );
            syncRgbBoxes();
            return true;
        }
        return super.mouseDragged(mx, my, button, dx, dy);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        if (button == 0) { dragStartX = -1; dragStartY = -1; }
        clickedArea = null;
        colorPicker.mouseReleased();
        return super.mouseReleased(mx, my, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        boolean ctrl  = (modifiers & org.lwjgl.glfw.GLFW.GLFW_MOD_CONTROL) != 0;
        boolean shift = (modifiers & org.lwjgl.glfw.GLFW.GLFW_MOD_SHIFT)   != 0;
        if (ctrl && keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_Z) {
            if (shift) canvas.redo(); else canvas.undo();
            hasUnsavedChanges = true;
            return true;
        }
        // 顔の向き
        if (keyCode == GLFW.GLFW_KEY_LEFT) {
            previewYaw -= 10.0f;
        } else if (keyCode == GLFW.GLFW_KEY_RIGHT) {
            previewYaw += 10.0f;
        } else if (keyCode == GLFW.GLFW_KEY_UP) {
            previewPitch -= 10.0f;
        } else if (keyCode == GLFW.GLFW_KEY_DOWN) {
            previewPitch += 10.0f;
        }

        // エディタ系
        if (keyCode == GLFW.GLFW_KEY_P || keyCode == GLFW.GLFW_KEY_B) {
            palette.setEraserMode(false);
            return true;
        } else if (keyCode == GLFW.GLFW_KEY_E) {
            palette.setEraserMode(true);
            return true;
        } else if (keyCode == GLFW.GLFW_KEY_I) {
            TOOL_MODE = "eyedropper";
            return true;
        } else if (keyCode == GLFW.GLFW_KEY_G) {
            TOOL_MODE = "bucket";
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    // ---- ブラシ -----------------------------------------------

    private void applyBrush(double mx, double my) {
        float scale  = Math.min((float) ED_W / 64, (float) ED_H / 64);
        int renderW  = (int)(64 * scale);
        int renderH  = (int)(64 * scale);
        int renderX  = leftPos + ED_X + (ED_W - renderW) / 2;
        int renderY  = topPos  + ED_Y + (ED_H - renderH) / 2;
        int[] px = screenToPixel((int) mx, (int) my, renderX, renderY, scale);
        if (px == null) return;

        if (!PowderRoomEditableRegions.isEditable(px[0], px[1])) return;

        if (palette.isEraserMode()) {
            if (TOOL_MODE == "bucket") {
                canvas.fill(TRANSPARENT);
            } else {
                canvas.erase(px[0], px[1], BRUSH_SIZE);
            }
        } else if (TOOL_MODE == "eyedropper") {
            int color = canvas.getPixel(px[0], px[1]);
            palette.setSelectedColor(color);
            palette.syncRgbFromColor();
            System.out.println("[CHECK][EYEDROPPER] " + color + ", R : " + palette.getRValue() + ", G : " + palette.getGValue() + ", B : " + palette.getBValue());
            if (beforeEyedropperTool != null) {
                TOOL_MODE = beforeEyedropperTool;
            }
        } else {
            if (TOOL_MODE == "bucket") {
                // 塗りつぶし
                canvas.fill(palette.getSelectedColor());
            } else {
                canvas.setPixel(px[0], px[1], palette.getSelectedColor(), BRUSH_SIZE);
            }
        }
        hasUnsavedChanges = true;
    }

    private boolean isHeadArea(int px, int py) {
        for (PatternType.CanvasSegment seg : PatternType.HEAD.getSegments()) {
            if (px >= seg.uvX() && px < seg.uvX() + seg.w()
                    && py >= seg.uvY() && py < seg.uvY() + seg.h()) {
                return true;
            }
        }
        return false;
    }

    // ---- SAVE -------------------------------------------------

    private void onSave() {
        if (canvas == null) return;
        PacketDistributor.sendToServer(new SaveSkinLayerPayload(canvas.getPixels()));
        hasUnsavedChanges = false;
        onClose();
    }

    // ---- ユーティリティ ----------------------------------------

    private boolean inEditorArea(double mx, double my) {
        return mx >= leftPos + ED_X && mx < leftPos + ED_X + ED_W
                && my >= topPos  + ED_Y && my < topPos  + ED_Y + ED_H;
    }
    private boolean inPreviewArea(double mx, double my) {
        return mx >= leftPos + PV_X && mx < leftPos + PV_X + PV_W
                && my >= topPos  + PV_Y && my < topPos  + PV_Y + PV_H;
    }
    private boolean inToolbar(double mx, double my) {
        return inBox(mx, my, leftPos + TOOLBAR_X, topPos + TOOLBAR_Y, 16, 186);
    }
    private boolean inBox(double mx, double my, int bx, int by, int w, int h) {
        return mx >= bx && mx < bx + w && my >= by && my < by + h;
    }

    private int[] screenToPixel(int mx, int my, int rx, int ry, float scale) {
        int px = (int)((mx - rx) / scale);
        int py = (int)((my - ry) / scale);
        if (px < 0 || px >= 64 || py < 0 || py >= 64) return null;
        return new int[]{px, py};
    }
    private int[] cropPixels(int[] full64x64, PatternType type) {
        int canvasW = type.getCanvasW();
        int canvasH = type.getCanvasH();
        int[] canvas = new int[canvasW * canvasH];

        for (PatternType.CanvasSegment seg : type.getSegments()) {
            for (int y = 0; y < seg.h(); y++) {
                for (int x = 0; x < seg.w(); x++) {
                    int srcIdx = (seg.uvY() + y) * 64 + (seg.uvX() + x);
                    int dstIdx = (seg.canvasY() + y) * type.getCanvasW() + (seg.canvasX() + x);
                    if (srcIdx < full64x64.length && dstIdx < canvas.length) {
                        canvas[dstIdx] = full64x64[srcIdx];
                    }
                }
            }
        }
        return canvas;
    }
    private void onRgbEdited() {
        try {
            palette.setRgb(
                    Integer.parseInt(rBox.getValue()),
                    Integer.parseInt(gBox.getValue()),
                    Integer.parseInt(bBox.getValue()));
        } catch (NumberFormatException ignored) {}
    }
    private void syncRgbBoxes() {
        if (palette.isEraserMode()) return;
        int r = palette.getRValue();
        int g = palette.getGValue();
        int b = palette.getBValue();
        rBox.setValue(String.valueOf(r));
        gBox.setValue(String.valueOf(g));
        bBox.setValue(String.valueOf(b));
        System.out.println("[CHECK][syncRgbBoxesFromPalette] R: " + r + " G:" + g + " B:" + b);
    }

    @Override
    public void removed() {
        if (canvas != null) canvas.close();
        if (previewCompositor != null) previewCompositor.close();
        if (colorPicker != null) colorPicker.close();
        super.removed();
    }

    @Override
    public boolean isPauseScreen() { return false; }
}
