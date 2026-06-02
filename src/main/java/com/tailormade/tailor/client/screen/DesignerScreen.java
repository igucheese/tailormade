package com.tailormade.tailor.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.tailormade.tailor.client.gui.ColorPalette;
import com.tailormade.tailor.client.gui.ColorPickerWidget;
import com.tailormade.tailor.client.menu.DesignerMenu;
import com.tailormade.tailor.client.renderer.TailorArmorRenderLayer;
import com.tailormade.tailor.client.renderer.TailorTextureCompositor;
import com.tailormade.tailor.data.DesignDataClientCache;
import com.tailormade.tailor.data.DesignDataRecord;
import com.tailormade.tailor.data.PatternType;
import com.tailormade.tailor.data.PixelData;
import com.tailormade.tailor.entities.items.PatternItem;
import com.tailormade.tailor.network.payloads.SaveDesignPayload;
import com.tailormade.tailor.registries.ModDataComponents;
import com.tailormade.tailor.utils.PixelCanvas;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

import java.util.*;

import static com.tailormade.tailor.Tailormade.MODID;
import static com.tailormade.tailor.data.Constants.TRANSPARENT;

/**
 * デザインテーブルの GUI スクリーン。
 *
 * レイアウト（GUI相対座標 / テクスチャ designer_gui.png に合わせて要調整）:
 *
 *   ┌─────────────────────────────────────────────┐
 *   │[S]│       2D Editor (190x145)      │Preview │
 *   │   │                                │(110x125)│
 *   │pal│                                │        │
 *   │   │                                │[P][P][P]│
 *   │   │  [R___] [G___] [B___]          │  [SAVE]│
 *   └─────────────────────────────────────────────┘
 *
 *   [S]  = メイン型紙スロット
 *   pal  = カラーパレット
 *   [P]  = プレビュー用型紙スロット ×3
 */
public class DesignerScreen extends AbstractContainerScreen<DesignerMenu> {

    // GUI テクスチャ
    private static final ResourceLocation GUI_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("tailormade", "textures/gui/designer_gui.png");
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

    // ---- レイアウト定数（テクスチャに合わせて調整） ----
    private static final int GUI_W = 384;
    private static final int GUI_H = 216;
    private static final int GUI_OFFSET_X = 64;
    private static final int GUI_OFFSET_Y = 148;

    // 2D エディタ領域（GUI相対）
    private static final int ED_X = 28;
    private static final int ED_Y = 6;
    private static final int ED_W = 188;
    private static final int ED_H = 188;

    // プレビュー領域（GUI相対）
    private static final int PV_X = 248;
    private static final int PV_Y = 14;
    private static final int PV_W = 124;
    private static final int PV_H = 148;

    // パレット領域（GUI相対）
    private static final int PAL_X = 8;
    private static final int PAL_Y = 34;

    // ツールバー
    private static final int TOOLBAR_X = 223;
    private static final int TOOLBAR_Y = 10;
    private static int BRUSH_SIZE = 1;
    private static String TOOL_MODE = "brush";

    // RGB EditBox
    private static final int RGB_Y_OFFSET = 4;  // エディタ下端からの距離
    private static final int RGB_BOX_W = 28;
    private static final int RGB_BOX_H = 10;

    private ColorPickerWidget colorPicker;

    // ---- フィールド -------------------------------------------

    private String clickedArea = null;

    private PixelCanvas canvas;
    private ColorPalette palette;

    private EditBox nameInput;

    private EditBox rBox, gBox, bBox;
    private Button saveButton;

    // 未保存チェック
    private boolean hasUnsavedChanges = false;
    private boolean showUnsavedWarning = false;
    private boolean wasPaintingStroke = false;
    private boolean isOnEditBox = false;
    private String beforeEyedropperTool = null;

    // プレビュー回転
    private float previewYaw   = 235.0f; // 正面が見える初期値
    private float previewPitch = 0.0f;
    private double lastDragX;
    private boolean draggingPreview = false;
    private double dragStartX = -1;
    private double dragStartY = -1;

    private ItemStack lastPatternStack = ItemStack.EMPTY;
    private TailorTextureCompositor previewCompositor;

    // ---- コンストラクタ ----------------------------------------

    public DesignerScreen(DesignerMenu menu, Inventory playerInv, Component title) {
        super(menu, playerInv, title);
        this.imageWidth  = GUI_W;
        this.imageHeight = GUI_H;
    }

    // ---- 初期化 -----------------------------------------------

    @Override
    protected void init() {
        super.init();

        // パレット
        palette = new ColorPalette(leftPos + PAL_X, topPos + PAL_Y);

        // キャンバス（型紙スロットに型紙がある場合のみ初期化）
        refreshCanvas();

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

        colorPicker = new ColorPickerWidget(leftPos + PAL_X, topPos + PAL_Y + 8 * 9 + 4);
        colorPicker.init();

        this.nameInput = new EditBox(this.font, leftPos + PV_X + PV_W - 70, topPos  + PV_Y + PV_H - 6, 72, 20, Component.translatable("gui.tailormade.tailor.pattern_name.placeholder"));
        this.nameInput.setMaxLength(15);
        this.nameInput.setHint(Component.translatable("gui.tailormade.tailor.pattern_name.placeholder"));
        this.addRenderableWidget(this.nameInput);

        // SAVE ボタン
        int saveX = leftPos + PV_X + PV_W - 63;
        int saveY = topPos  + PV_Y + PV_H + 21;
        saveButton = Button.builder(Component.translatable("gui.tailormade.designer.save"), btn -> onSave())
                .pos(saveX, saveY)
                .size(65, 24)
                .build();
        addRenderableWidget(saveButton);

        previewCompositor = TailorTextureCompositor.createForPreview();
    }

    private EditBox makeRgbBox(int x, int y, String hint) {
        EditBox box = new EditBox(font, x, y, RGB_BOX_W, RGB_BOX_H, Component.literal(hint));
        box.setMaxLength(3);
        box.setValue("0");
        return box;
    }

    @Override
    protected void containerTick() {
        super.containerTick();

        ItemStack mainStack = menu.getPatternContainer().getItem(DesignerMenu.MAIN_SLOT);

        if (!ItemStack.matches(this.lastPatternStack, mainStack)) {
            this.lastPatternStack = mainStack.copy();
            this.refreshCanvas();
        }
    }

    /**
     * メインスロットの型紙に合わせてキャンバスを初期化（または破棄）する。
     * スロット変更時にも呼ぶ。
     */
    private void refreshCanvas() {
        ItemStack mainStack = menu.getPatternContainer().getItem(DesignerMenu.MAIN_SLOT);

        if (mainStack.isEmpty() || !(mainStack.getItem() instanceof PatternItem patternItem)) {
            if (canvas != null) { canvas.close(); canvas = null; }
            return;
        }

        PatternType type  = patternItem.getPatternType(mainStack);
        int[] size        = type.getTextureSize();

        // すでに同じサイズのキャンバスがあれば作り直さない
        if (canvas != null && canvas.getWidth() == size[0] && canvas.getHeight() == size[1]) return;

        if (canvas != null) canvas.close();
        canvas = new PixelCanvas(size[0], size[1]);

        // 既存データのロード
        int[] existing = patternItem.getPixelData(mainStack);
        if (existing != null) canvas.loadPixels(existing);

        canvas.init();
    }

    // ---- 描画 -------------------------------------------------

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g, mouseX, mouseY, partialTick);
        super.render(g, mouseX, mouseY, partialTick);

        palette.render(g, mouseX, mouseY);
        colorPicker.render(g, mouseX, mouseY);

        renderEditor(g, mouseX, mouseY);
        renderPreview(g, mouseX, mouseY);
        renderRgbLabels(g);
        renderToolabr(g);

        if (showUnsavedWarning) renderUnsavedWarning(g);

        this.isOnEditBox = this.nameInput.isFocused();

        renderTooltip(g, mouseX, mouseY);

        if (canvas != null) canvas.uploadIfDirty();
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        g.blit(GUI_TEXTURE, x, y, GUI_OFFSET_X, GUI_OFFSET_Y, imageWidth, imageHeight, 512, 512);
//        g.blit(GUI_TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
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

    /** 2D エディタ領域を描画する */
    private void renderEditor(GuiGraphics g, int mouseX, int mouseY) {
        if (canvas == null) {
            g.drawCenteredString(font, Component.translatable("gui.tailormade.designer.start").getString(),
                    leftPos + ED_X + ED_W / 2, topPos + ED_Y + ED_H / 2 - 4, 0xFFFFFFFF);
            return;
        }

        // キャンバスをエディタ領域にフィット（アスペクト比維持）
        float scale   = fitScale();
        int renderW   = (int)(canvas.getWidth()  * scale);
        int renderH   = (int)(canvas.getHeight() * scale);
        int renderX   = leftPos + ED_X + (ED_W - renderW) / 2;
        int renderY   = topPos  + ED_Y + (ED_H - renderH) / 2;

        // DynamicTexture を描画
        RenderSystem.enableBlend();
        g.blit(canvas.getTextureLocation(), renderX, renderY, 0, 0, renderW, renderH, renderW, renderH);
        RenderSystem.disableBlend();

        // グリッド線（スケールが4以上の場合のみ）
        if (scale >= 4.0f) {
            drawGrid(g, renderX, renderY, renderW, renderH, scale);
        }

        // ホバーピクセルのハイライト
        int[] hoverPx = screenToPixel(mouseX, mouseY, renderX, renderY, scale);
        if (hoverPx != null) {
            int hx = renderX + (int)(hoverPx[0] * scale);
            int hy = renderY + (int)(hoverPx[1] * scale);
            g.fill(hx, hy, hx + (int)scale, hy + (int)scale, 0x55FFFFFF);
        }

        renderSegmentBorders(g, renderX, renderY, scale);
    }

    private void renderSegmentBorders(GuiGraphics g, int renderX, int renderY, float scale) {
        if (canvas == null) return;

        ItemStack mainStack = menu.getPatternContainer().getItem(DesignerMenu.MAIN_SLOT);
        if (mainStack.isEmpty() || !(mainStack.getItem() instanceof PatternItem patternItem)) return;

        PatternType type = patternItem.getPatternType(mainStack);
        PatternType.CanvasSegment[] segments = type.getSegments();
        if (segments.length <= 1) return; // 単一セグメントなら不要

        String[] labels = segmentLabels(type);

        for (int i = 0; i < segments.length; i++) {
            PatternType.CanvasSegment seg = segments[i];

            // セグメント開始位置に縦線（最初のセグメントは不要）
            if (i > 0) {
                int lx = renderX + (int)(seg.canvasX() * scale);
                int ly = renderY;
                int lh = (int)(canvas.getHeight() * scale);
                g.fill(lx, ly, lx + 1, ly + lh, 0xAAFFFF00); // 黄色の境界線
            }

            // セグメント中央にラベル
            if (labels != null && i < labels.length) {
                int labelX = renderX + (int)((seg.canvasX() + seg.w() / 2.0f) * scale) - 10;
                int labelY = renderY - 8;
                g.drawString(font, labels[i], labelX, labelY, 0xFFAAAAAA, false);
            }
        }
    }

    private String[] segmentLabels(PatternType type) {
        return switch (type) {
            case CHEST -> new String[]{"Body", "R.Arm", "L.Arm"};
            case LEGS  -> new String[]{"R.Leg", "L.Leg"};
            case FEET  -> new String[]{"R.Boot", "L.Boot"};
            default    -> null;
        };
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

        Map<PatternType, int[]> pixelMap = buildPreviewPixelMap();
        if (!pixelMap.isEmpty()) {
            ResourceLocation tex = previewCompositor.composeForPreview(pixelMap);
            TailorArmorRenderLayer.setPreviewOverride(tex);
        }

        float savedXRot  = mc.player.getXRot();
        float savedXRotO = mc.player.xRotO;
        mc.player.setXRot(previewPitch);
        mc.player.xRotO = previewPitch;

        try {
            // pose: Z軸π回転（バニラ必須のフリップ）＋ Y軸でボディ回転
            // cameraOrientation: X軸で頭の上下を制御
            // この2つを分けることで「ボディが回転しても頭は自然な向き」になる
            Quaternionf pose = new Quaternionf()
                    .rotateZ((float) Math.PI)
                    .rotateY((float) Math.toRadians(previewYaw));
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
            TailorArmorRenderLayer.clearPreviewOverride();
        }
    }

    /**
     * メインスロットのキャンバスと、プレビュースロットの型紙データを合わせて
     * PatternType → int[] マップを構築する。
     */
    private Map<PatternType, int[]> buildPreviewPixelMap() {
        Map<PatternType, int[]> map = new EnumMap<>(PatternType.class);

        // メインスロット: 現在編集中のキャンバスを使う
        if (canvas != null) {
            ItemStack mainStack = menu.getPatternContainer().getItem(DesignerMenu.MAIN_SLOT);
            if (!mainStack.isEmpty() && mainStack.getItem() instanceof PatternItem patternItem) {
                PatternType type = patternItem.getPatternType(mainStack);
                map.put(type, canvas.getPixels()); // 保存前のライブデータ
            }
        }

        // プレビュースロット 1,2,3: 保存済みの型紙データを使う
        int[] previewSlots = {
                DesignerMenu.PREVIEW_SLOT1,
                DesignerMenu.PREVIEW_SLOT2,
                DesignerMenu.PREVIEW_SLOT3
        };
        for (int slotIdx : previewSlots) {
            ItemStack stack = menu.getPatternContainer().getItem(slotIdx);
            if (stack.isEmpty() || !(stack.getItem() instanceof PatternItem patternItem)) continue;

            String patternId = stack.get(ModDataComponents.PATTERN_ID.get());
            PixelData pixelData = null;
            if (patternId != null && !patternId.isBlank()) {
                DesignDataRecord designData = DesignDataClientCache.get(UUID.fromString(patternId));
                if (designData != null) {
                    pixelData = designData.pixelData();
                }
            }
            if (pixelData == null) continue;

            PatternType type = patternItem.getPatternType(stack);
            map.putIfAbsent(type, pixelData.pixels()); // メインスロットと被る部位は上書きしない
        }

        return map;
    }

    private void renderRgbLabels(GuiGraphics g) {
        int ly = topPos + ED_Y + ED_H + RGB_Y_OFFSET;
        g.drawString(font, "R", leftPos + 8, ly + 1, 0xFFFFFF, false);
        g.drawString(font, "G", leftPos + 8 + RGB_BOX_W  + 6, ly + 1, 0xFFFFFF, false);
        g.drawString(font, "B", leftPos + 8 + (RGB_BOX_W * 2) + 12, ly + 1, 0xFFFFFF, false);
    }

    private void renderUnsavedWarning(GuiGraphics g) {
        int wx = leftPos + imageWidth  / 2 - 64;
        int wy = topPos  + imageHeight / 2 - 22;
        // 背景
        g.fill(wx - 4, wy - 4, wx + 132, wy + 48, 0xDD000000);
        g.drawString(font, Component.translatable("gui.tailormade.designer.warning.title").getString(), wx, wy, 0xFF5555, false);
        g.drawString(font, Component.translatable("gui.tailormade.designer.warning.description").getString(), wx, wy + 12, 0xFFFFFF, false);
        // Closeボタン
        g.fill(wx,      wy + 26, wx + 58,  wy + 38, 0xFF4444);
        g.drawString(font, Component.translatable("gui.tailormade.modal.close").getString(),  wx + 4,  wy + 28, 0xFFFFFF, false);
        // Cancelボタン
        g.fill(wx + 64, wy + 26, wx + 128, wy + 38, 0x444444);
        g.drawString(font, Component.translatable("gui.tailormade.modal.cancel").getString(), wx + 68, wy + 28, 0xFFFFFF, false);
    }

    // ---- マウスイベント ----------------------------------------

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (showUnsavedWarning) return handleWarningClick(mx, my);
        if (palette.mouseClicked(mx, my)) {
            clickedArea = "palette";
            syncRgbBoxesFromPalette();
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
            syncRgbBoxesFromPalette();
            return true;
        }
        if (canvas != null && inEditorArea(mx, my)) {
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
        if (button == 0 && canvas != null && inEditorArea(mx, my) && clickedArea == "editor") {
            applyBrush(mx, my);
            return true;
        }
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
            syncRgbBoxesFromPalette();
            return true;
        }
        return super.mouseDragged(mx, my, button, dx, dy);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.isOnEditBox) {
            return super.keyPressed(keyCode, scanCode, modifiers);
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

        boolean ctrl  = (modifiers & GLFW.GLFW_MOD_CONTROL) != 0;
        boolean shift = (modifiers & GLFW.GLFW_MOD_SHIFT)   != 0;
        if (ctrl && keyCode == GLFW.GLFW_KEY_Z) {
            System.out.println("[CHECK][KEY PRESSSED]" + "Z!");
            if (canvas == null) return true;
            System.out.println("[CHECK][KEY PRESSSED]" + "Z! 1");
            if (shift) {
                System.out.println("[CHECK][KEY PRESSSED]" + "Z! 2");
                canvas.redo();
            } else {
                System.out.println("[CHECK][KEY PRESSSED]" + "Z! 3");
                canvas.undo();
            }
            hasUnsavedChanges = canvas.canUndo(); // Undo 履歴が空なら未保存フラグも落とす
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        if (button == 0) {
            dragStartX = -1;
            dragStartY = -1;
        }
        clickedArea = null;
        colorPicker.mouseReleased();
        return super.mouseReleased(mx, my, button);
    }

    private boolean handleWarningClick(double mx, double my) {
        int wx = leftPos + imageWidth  / 2 - 64;
        int wy = topPos  + imageHeight / 2 - 22;
        // Close
        if (inBox(mx, my, wx, wy + 26, 58, 12)) {
            hasUnsavedChanges = false;
            super.onClose();
            return true;
        }
        // Cancel
        if (inBox(mx, my, wx + 64, wy + 26, 64, 12)) {
            showUnsavedWarning = false;
            return true;
        }
        return true; // ダイアログ外クリックを吸収
    }

    // ---- 描画ユーティリティ ------------------------------------

    private void applyBrush(double mx, double my) {
        if (canvas == null) return;
        float scale  = fitScale();
        int renderW  = (int)(canvas.getWidth()  * scale);
        int renderH  = (int)(canvas.getHeight() * scale);
        int renderX  = leftPos + ED_X + (ED_W - renderW) / 2;
        int renderY  = topPos  + ED_Y + (ED_H - renderH) / 2;

        int[] px = screenToPixel((int) mx, (int) my, renderX, renderY, scale);
        if (px == null) return;

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
        wasPaintingStroke  = true;
    }

    /** スクリーン座標 → キャンバスのピクセル座標。範囲外なら null。 */
    private int[] screenToPixel(int mx, int my, int renderX, int renderY, float scale) {
        int px = (int)((mx - renderX) / scale);
        int py = (int)((my - renderY) / scale);
        if (px < 0 || px >= canvas.getWidth() || py < 0 || py >= canvas.getHeight()) return null;
        return new int[]{px, py};
    }

    /** エディタ領域にキャンバスをフィットさせるスケールを返す */
    private float fitScale() {
        if (canvas == null) return 1.0f;
        return Math.min((float) ED_W / canvas.getWidth(), (float) ED_H / canvas.getHeight());
    }

    private boolean inEditorArea(double mx, double my) {
        return inBox(mx, my, leftPos + ED_X, topPos + ED_Y, ED_W, ED_H);
    }

    private boolean inPreviewArea(double mx, double my) {
        return inBox(mx, my, leftPos + PV_X, topPos + PV_Y, PV_W, PV_H);
    }

    private boolean inBox(double mx, double my, int bx, int by, int w, int h) {
        return mx >= bx && mx < bx + w && my >= by && my < by + h;
    }

    private boolean inToolbar(double mx, double my) {
        return inBox(mx, my, leftPos + TOOLBAR_X, topPos + TOOLBAR_Y, 16, 186);
    }

    // ---- RGB EditBox / パレット同期 ----------------------------

    private void onRgbEdited() {
        try {
            int r = Integer.parseInt(rBox.getValue());
            int g = Integer.parseInt(gBox.getValue());
            int b = Integer.parseInt(bBox.getValue());
            palette.setRgb(r, g, b);
        } catch (NumberFormatException ignored) {
            // 入力途中は無視
        }
    }

    private void syncRgbBoxesFromPalette() {
        if (palette.isEraserMode()) return;
        int r = palette.getRValue();
        int g = palette.getGValue();
        int b = palette.getBValue();
        rBox.setValue(String.valueOf(r));
        gBox.setValue(String.valueOf(g));
        bBox.setValue(String.valueOf(b));
        System.out.println("[CHECK][syncRgbBoxesFromPalette] R: " + r + " G:" + g + " B:" + b);
    }

    // ---- SAVE / CLOSE -----------------------------------------

    private void onSave() {
        if (canvas == null) return;
        ItemStack mainStack = menu.getPatternContainer().getItem(DesignerMenu.MAIN_SLOT);
        if (mainStack.isEmpty() || !(mainStack.getItem() instanceof PatternItem)) return;

        PacketDistributor.sendToServer(
                new SaveDesignPayload(DesignerMenu.MAIN_SLOT, new PixelData(canvas.getPixels()), this.nameInput.getValue())
        );

        hasUnsavedChanges = false;
        this.onClose();
    }

    @Override
    public void onClose() {
        // 未保存データがあれば警告ダイアログを出す
        if (hasUnsavedChanges && !showUnsavedWarning) {
            showUnsavedWarning = true;
            return;
        }
        if (canvas != null) { canvas.close(); canvas = null; }
        super.onClose();
    }

    @Override
    public void removed() {
        if (canvas != null) canvas.close();
        if (previewCompositor != null) previewCompositor.close();
        if (colorPicker != null) colorPicker.close();
        super.removed();
    }

    // ---- その他オーバーライド ----------------------------------

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        // デフォルトのタイトルラベルは表示しない
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}