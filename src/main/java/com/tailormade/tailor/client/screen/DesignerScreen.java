package com.tailormade.tailor.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.tailormade.tailor.client.gui.ColorPalette;
import com.tailormade.tailor.client.gui.ColorPickerWidget;
import com.tailormade.tailor.client.gui.HueBarWidget;
import com.tailormade.tailor.client.menu.DesignerMenu;
import com.tailormade.tailor.client.renderer.TailorArmorRenderLayer;
import com.tailormade.tailor.client.renderer.TailorTextureCompositor;
import com.tailormade.tailor.data.*;
import com.tailormade.tailor.data.records.DesignTemplate;
import com.tailormade.tailor.data.records.LayerData;
import com.tailormade.tailor.entities.items.PatternItem;
import com.tailormade.tailor.network.payloads.SaveDesignPayload;
import com.tailormade.tailor.registries.ModDataComponents;
import com.tailormade.tailor.utils.DesignAccessor;
import com.tailormade.tailor.utils.editor.ColorService;
import com.tailormade.tailor.utils.editor.LayerService;
import com.tailormade.tailor.utils.editor.LayerThumbnailManager;
import com.tailormade.tailor.utils.PixelCanvas;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.MemoryUtil;

import java.util.*;

import static com.tailormade.tailor.Tailormade.MODID;
import static com.tailormade.tailor.data.Constants.TRANSPARENT;

public class DesignerScreen extends AbstractContainerScreen<DesignerMenu> {
    private static final ResourceLocation GUI_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("tailormade", "textures/gui/designer_gui_2.png");
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
    private static final ResourceLocation ERASER_ICON =
            ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/eraser.png");
    private static final ResourceLocation LAYER_ICON =
            ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/layer_button.png");
    private static final ResourceLocation LAYER_ADD_ICON =
            ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/layer_add.png");
    private static final ResourceLocation LAYER_VISIBLE_ICON =
            ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/layer_visible.png");
    private static final ResourceLocation LAYER_INVISIBLE_ICON =
            ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/layer_invisible.png");
    private static final ResourceLocation POPUP_BG_TEMPLATE =
            ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/designer_gui_overlay_t.png");

    private static final ResourceLocation TOOL_ICON_BRUSH =
            ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/editor/editor_tools_brush.png");
    private static final ResourceLocation TOOL_ICON_ERASER =
            ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/editor/editor_tools_eraser.png");
    private static final ResourceLocation TOOL_ICON_BUCKET =
            ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/editor/editor_tools_bucket.png");
    private static final ResourceLocation TOOL_ICON_EYEDROPPER =
            ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/editor/editor_tools_eyedropper.png");
    private static final ResourceLocation TOOL_ICON_SELECTION =
            ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/editor/editor_tools_selection.png");
    private static final ResourceLocation TOOL_ICON_SIZE_1 =
            ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/editor/brush_size_1.png");
    private static final ResourceLocation TOOL_ICON_SIZE_2 =
            ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/editor/brush_size_2.png");
    private static final ResourceLocation TOOL_ICON_SIZE_3 =
            ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/editor/brush_size_3.png");

    private static final ResourceLocation TEMPLATE_BUTTON =
            ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/editor/template_btn.png");
    private static final ResourceLocation SEG_BUTTON_R =
            ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/editor/seg_btn_regular.png");
    private static final ResourceLocation SEG_BUTTON_S =
            ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/editor/seg_btn_slim.png");
    private static final ResourceLocation SEG_BUTTON_N =
            ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/editor/seg_btn_off.png");

    private static final int GUI_W = 384;
    private static final int GUI_H = 224;
    private static final int GUI_OFFSET_X = 64;
    private static final int GUI_OFFSET_Y = 144;

    private static final int ED_X = 28;
    private static final int ED_Y = 6;
    private static final int ED_W = 188;
    private static final int ED_H = 188;
    private static final int PV_X = 248;
    private static final int PV_Y = 14;
    private static final int PV_W = 124;
    private static final int PV_H = 148;
    private static final int PAL_X = 8;
    private static final int PAL_Y = 34;
    private static final int TOOLBAR_X = 223;
    private static final int TOOLBAR_Y = 10;
    private static final int LAYER_BAR_Y = 180;
    private static int BRUSH_SIZE = 1;
    private static String TOOL_MODE = "brush";
    private static boolean IS_CONTROLLING_LAYER = false;
    private static final int SEG_BTN_X = 52;
    private static final int SEG_BTN_Y = 210;

    private static final int RGB_Y_OFFSET = 4;  // エディタ下端からの距離
    private static final int RGB_BOX_W = 28;
    private static final int RGB_BOX_H = 10;
    private int labelBorderColor = 0xFFFF4444;

    private float zoomScale = 1.0f;
    private float panOffsetX = 0f;
    private float panOffsetY = 0f;
    private double rightDragStartX = -1;
    private double rightDragStartY = -1;

    private String clickedArea = null;
    private PixelCanvas canvas;
    private ColorPalette palette;
    private EditBox nameInput;
    private EditBox rBox, gBox, bBox;
    private Button saveButton;
    private ColorPickerWidget colorPicker;
    private HueBarWidget hueBar;

    private boolean hasUnsavedChanges = false;
    private boolean showUnsavedWarning = false;
    private boolean wasPaintingStroke = false;
    private String beforeEyedropperTool = null;

    private float previewYaw = 235.0f;
    private float previewPitch = 0.0f;
    private double lastDragX;
    private boolean draggingPreview = false;
    private double dragStartX = -1;
    private double dragStartY = -1;

    // 範囲選択用
    private int moveOffsetX = 0;
    private int moveOffsetY = 0;

    // ポップアップ系
    private String popUpMode = null;
    private List<DesignTemplate> templates;
    private ScrollableWidget<DesignTemplate> scrollableTemplates;

    // レイヤー
    private LayerThumbnailManager thumbnailManager;

    private ItemStack lastPatternStack = ItemStack.EMPTY;
    private TailorTextureCompositor previewCompositor;

    private static final int FACE_LINE_COLOR = 0x3300DDFF;
    private static final int FACE_LABEL_COLOR = 0x7700DDFF;

    private boolean isPatternSet = false;
    private String guideMode = "regular";

    private long window;
    private long CURSOR_DEFAULT;
    private long CURSOR_CROSSHAIR;
    private long CURSOR_HAND;

    public DesignerScreen(DesignerMenu menu, Inventory playerInv, Component title) {
        super(menu, playerInv, title);
        this.imageWidth = GUI_W;
        this.imageHeight = GUI_H;
    }

    @Override
    protected void init() {
        super.init();

        this.window = Minecraft.getInstance().getWindow().getWindow();
        this.CURSOR_DEFAULT = GLFW.glfwCreateStandardCursor(GLFW.GLFW_CURSOR);
        this.CURSOR_CROSSHAIR = GLFW.glfwCreateStandardCursor(GLFW.GLFW_CROSSHAIR_CURSOR);
        this.CURSOR_HAND = GLFW.glfwCreateStandardCursor(GLFW.GLFW_HAND_CURSOR);

        palette = new ColorPalette(leftPos + PAL_X, topPos + PAL_Y);
        refreshCanvas();

        int rgbBaseX = leftPos + 14;
        int rgbY = topPos + ED_Y + ED_H + RGB_Y_OFFSET;
        rBox = makeRgbBox(rgbBaseX, rgbY, "R");
        gBox = makeRgbBox(rgbBaseX + RGB_BOX_W + 6, rgbY, "G");
        bBox = makeRgbBox(rgbBaseX + (RGB_BOX_W * 2) + 12, rgbY, "B");
        rBox.setResponder(s -> onRgbEdited());
        gBox.setResponder(s -> onRgbEdited());
        bBox.setResponder(s -> onRgbEdited());
        addRenderableWidget(rBox);
        addRenderableWidget(gBox);
        addRenderableWidget(bBox);

        hueBar = new HueBarWidget(leftPos + PAL_X, topPos + PAL_Y + 8 * 9);
        hueBar.setH(44);
        hueBar.init();
        colorPicker = new ColorPickerWidget(leftPos + PAL_X, topPos + PAL_Y + 8 * 9 + 44);
        colorPicker.setH(43);
        colorPicker.init();
        colorPicker.setBaseColor(hueBar.getSelectedBaseColor());

        this.nameInput = new EditBox(this.font, leftPos + PV_X + PV_W - 70, topPos + PV_Y + PV_H - 6, 72, 19, Component.translatable("gui.tailormade.tailor.pattern_name.placeholder"));
        this.nameInput.setMaxLength(15);
        this.nameInput.setHint(Component.translatable("gui.tailormade.tailor.pattern_name.placeholder"));
        this.addRenderableWidget(this.nameInput);

        int saveX = leftPos + PV_X + PV_W - 63;
        int saveY = topPos + PV_Y + PV_H + 15;
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

    private void refreshCanvas() {
        this.isPatternSet = false;
        ItemStack mainStack = menu.getPatternContainer().getItem(DesignerMenu.MAIN_SLOT);
        if (mainStack.isEmpty() || !(mainStack.getItem() instanceof PatternItem patternItem)) {
            if (canvas != null) { canvas.close(); canvas = null; }
            return;
        }

        PatternType type = patternItem.getPatternType(mainStack);
        int[] size = type.getTextureSize();

        if (canvas != null && canvas.getWidth() == size[0] && canvas.getHeight() == size[1]) return;
        if (canvas != null) canvas.close();
        canvas = new PixelCanvas(size[0], size[1]);
        thumbnailManager = new LayerThumbnailManager(canvas.getWidth(), canvas.getHeight());

        boolean hasLayersLoaded = false;
        String patternId = mainStack.get(ModDataComponents.PATTERN_ID.get());
        if (patternId != null) {
            DesignDataRecord record = DesignAccessor.getDesignDataFromId(patternId);
            if (record != null && !record.layers().isEmpty()) {
                canvas.loadLayers(record.layers());
                hasLayersLoaded = true;

                for (LayerData layer : record.layers()) {
                    thumbnailManager.update(layer.id(), layer.pixelData().pixels());
                }
            }
        }

        if (!hasLayersLoaded) {
            int[] existing = patternItem.getPixelData(mainStack);
            if (existing != null) {
                canvas.loadPixels(existing);
                thumbnailManager.update(0, existing);
            }
        }
        canvas.init();
        this.isPatternSet = true;
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g, mouseX, mouseY, partialTick);
        super.render(g, mouseX, mouseY, partialTick);

        palette.render(g, mouseX, mouseY);
        hueBar.render(g, mouseX, mouseY);
        colorPicker.render(g, mouseX, mouseY);

        renderEditor(g, mouseX, mouseY);
        renderPreview(g, mouseX, mouseY);
        renderRgbLabels(g);
        renderToolabr(g);
        renderSegmentBar(g);
        renderLayers(g);
        renderPopups(g, mouseX, mouseY, partialTick);
        setMouseCursor(mouseX, mouseY);

        if (showUnsavedWarning) renderUnsavedWarning(g);
        renderTooltip(g, mouseX, mouseY);
        if (canvas != null) canvas.uploadIfDirty();
    }

    private void setMouseCursor(double mx, double my) {
        if (canvas != null && TOOL_MODE.equals("selection")) {
            int renderX = currentRenderXY()[0];
            int renderY = currentRenderXY()[1];
            int[] px = screenToPixel((int) mx, (int) my, renderX, renderY, currentScale());
            if (canvas.isSelectionSet() && px != null && canvas.isInSelection(px[0], px[1])) {
                GLFW.glfwSetCursor(window, CURSOR_HAND);
            } else {
                GLFW.glfwSetCursor(window, CURSOR_CROSSHAIR);
            }
        } else {
            GLFW.glfwSetCursor(window, CURSOR_DEFAULT);
        }
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        g.blit(GUI_TEXTURE, x, y, GUI_OFFSET_X, GUI_OFFSET_Y, imageWidth, imageHeight, 512, 512);
    }

    private void renderToolabr(GuiGraphics g)
    {
        int toolBarX = this.leftPos + TOOLBAR_X;
        int toolBarY = this.topPos + TOOLBAR_Y;
        renderHalfSize(g, TOOL_ICON_BRUSH, toolBarX, toolBarY, 20, 20);
        renderHalfSize(g, TOOL_ICON_ERASER, toolBarX, toolBarY + 12, 20, 20);
        renderHalfSize(g, TOOL_ICON_BUCKET, toolBarX, toolBarY + 24, 20, 20);
        renderHalfSize(g, TOOL_ICON_EYEDROPPER, toolBarX, toolBarY + 36, 20, 20);
        renderHalfSize(g, TOOL_ICON_SELECTION, toolBarX, toolBarY + 48, 20, 20);

        // アクティブ枠
        int toolBarActiveWidth = 12;
        int tollBarActiveX = toolBarX - 1;
        int toolBarActiveY;
        switch (TOOL_MODE) {
            case "brush": toolBarActiveY = toolBarY - 1; break;
            case "eraser": toolBarActiveY = toolBarY + 11; break;
            case "bucket": toolBarActiveY = toolBarY + 23; break;
            case "eyedropper": toolBarActiveY = toolBarY + 35; break;
            case "selection": toolBarActiveY = toolBarY + 47; break;
            default: toolBarActiveY = toolBarY - 1;
        }
        g.fill(tollBarActiveX, toolBarActiveY, tollBarActiveX + toolBarActiveWidth, toolBarActiveY + 1, labelBorderColor);
        g.fill(tollBarActiveX,  toolBarActiveY + toolBarActiveWidth - 1, tollBarActiveX + toolBarActiveWidth, toolBarActiveY + toolBarActiveWidth, labelBorderColor);
        g.fill(tollBarActiveX, toolBarActiveY, tollBarActiveX + 1, toolBarActiveY + toolBarActiveWidth, labelBorderColor);
        g.fill(tollBarActiveX + toolBarActiveWidth - 1, toolBarActiveY, tollBarActiveX + toolBarActiveWidth, toolBarActiveY + toolBarActiveWidth, labelBorderColor);

        // ブラシサイズ選択
        renderHalfSize(g, TOOL_ICON_SIZE_1, toolBarX, toolBarY + 60, 20, 12);
        renderHalfSize(g, TOOL_ICON_SIZE_2, toolBarX, toolBarY + 66, 20, 12);
        renderHalfSize(g, TOOL_ICON_SIZE_3, toolBarX, toolBarY + 72, 20, 12);

        // ブラシサイズ表示
        switch (BRUSH_SIZE) {
            case 1: toolBarActiveY = toolBarY + 59; break;
            case 2: toolBarActiveY = toolBarY + 65; break;
            case 3: toolBarActiveY = toolBarY + 71; break;
            default: toolBarActiveY = toolBarY + 59;
        }
        g.fill(tollBarActiveX, toolBarActiveY, tollBarActiveX + toolBarActiveWidth, toolBarActiveY + 1, labelBorderColor);
        g.fill(tollBarActiveX,  toolBarActiveY + 7, tollBarActiveX + toolBarActiveWidth, toolBarActiveY + 8, labelBorderColor);
        g.fill(tollBarActiveX, toolBarActiveY, tollBarActiveX + 1, toolBarActiveY + 8, labelBorderColor);
        g.fill(tollBarActiveX + toolBarActiveWidth - 1, toolBarActiveY, tollBarActiveX + toolBarActiveWidth, toolBarActiveY + 8, labelBorderColor);
    }

    private void renderSegmentBar(GuiGraphics g) {
        int x = this.leftPos + SEG_BTN_X;
        int y = this.topPos + SEG_BTN_Y;
        if (!this.isPatternSet) return;

        g.blit(TEMPLATE_BUTTON, this.leftPos + 6, y, 0, 0, 43, 9, 43, 9);
        if (guideMode.equals("regular")) {
            g.blit(SEG_BUTTON_R, x, y, 0, 0, 64, 9, 64, 9);
        } else if (guideMode.equals("slim")) {
            g.blit(SEG_BUTTON_S, x, y, 0, 0, 64, 9, 64, 9);
        } else {
            g.blit(SEG_BUTTON_N, x, y, 0, 0, 64, 9, 64, 9);
        }
    }

    private void renderHalfSize(GuiGraphics g, ResourceLocation texture, int x, int y, int w, int h) {
        g.pose().pushPose();
        g.pose().translate(x, y, 0);
        g.pose().scale(0.5F, 0.5F, 1.0f);
        g.blit(texture, 0, 0, 0, 0, w, h, w, h);
        g.pose().popPose();
    }

    private void renderLayers(GuiGraphics g) {
        if (canvas == null) return;
        int toolBarX = this.leftPos + TOOLBAR_X;
        int toolBarY = this.topPos + LAYER_BAR_Y;
        for (int i = 0; i < canvas.getLayers().size(); i++) {
            g.blit(LAYER_ICON, toolBarX, toolBarY - (i * 12), 0, 0, 10, 10, 10, 10);

            // レイヤー枠に縮小表示
            if (thumbnailManager == null) continue;
            ResourceLocation thumbLoc = thumbnailManager.getLocation(i);
            if (thumbLoc != null) {
                int slotSize = 8;
                int texW = canvas.getWidth();
                int texH = canvas.getHeight();
                float scale = (float) slotSize / Math.max(texW, texH);

                int drawX = toolBarX + 1;
                int drawY = toolBarY - (i * 12) + 1;

                g.pose().pushPose();
                g.pose().translate(drawX, drawY, 0);
                g.pose().scale(scale, scale, 1.0f);
                g.blit(thumbLoc, 0, 0, 0, 0, texW, texH, texW, texH);
                g.pose().popPose();
            }

            // 非表示中なら非表示アイコンを出す
            if (!canvas.isLayerVisible(i)) {
                g.blit(LAYER_INVISIBLE_ICON, toolBarX, toolBarY - (i * 12), 0, 0, 10, 10, 10, 10);
            } else {
                g.blit(LAYER_VISIBLE_ICON, toolBarX, toolBarY - (i * 12), 0, 0, 10, 10, 10, 10);
            }
        }
        if (canvas.canAddLayer()) {
            g.blit(LAYER_ADD_ICON, toolBarX, toolBarY - (canvas.getLayers().size() * 12), 0, 0, 10, 10, 10, 10);
        }

        // アクティブ枠
        int toolBarActiveWidth = 12;
        int tollBarActiveX = toolBarX - 1;
        int toolBarActiveY = toolBarY - (canvas.getActiveLayerIndex() * 12) - 1;
        g.fill(tollBarActiveX, toolBarActiveY, tollBarActiveX + toolBarActiveWidth, toolBarActiveY + 1, labelBorderColor);
        g.fill(tollBarActiveX,  toolBarActiveY + toolBarActiveWidth - 1, tollBarActiveX + toolBarActiveWidth, toolBarActiveY + toolBarActiveWidth, labelBorderColor);
        g.fill(tollBarActiveX, toolBarActiveY, tollBarActiveX + 1, toolBarActiveY + toolBarActiveWidth, labelBorderColor);
        g.fill(tollBarActiveX + toolBarActiveWidth - 1, toolBarActiveY, tollBarActiveX + toolBarActiveWidth, toolBarActiveY + toolBarActiveWidth, labelBorderColor);
    }

    private void renderEditor(GuiGraphics g, int mouseX, int mouseY) {
        if (canvas == null) {
            g.drawCenteredString(font, Component.translatable("gui.tailormade.designer.start").getString(), leftPos + ED_X + ED_W / 2, topPos + ED_Y + ED_H / 2 - 4, 0xFFFFFFFF);
            return;
        }

        float scale = currentScale();
        int[] rxy = currentRenderXY();
        int renderW = (int)(canvas.getWidth()  * scale);
        int renderH = (int)(canvas.getHeight() * scale);
        int renderX = rxy[0];
        int renderY = rxy[1];

        int clipX = leftPos + ED_X;
        int clipY = topPos + ED_Y;
        g.enableScissor(clipX, clipY, clipX + ED_W, clipY + ED_H);

        RenderSystem.enableBlend();
        g.blit(canvas.getTextureLocation(), renderX, renderY, 0, 0, renderW, renderH, renderW, renderH);
        RenderSystem.disableBlend();

        if (scale >= 4.0f) {
            drawGrid(g, renderX, renderY, renderW, renderH, scale);
        }

        int[] hoverPx = screenToPixel(mouseX, mouseY, renderX, renderY, scale);
        if (hoverPx != null) {
            int hoverPixelColor = canvas.getPixel(hoverPx[0], hoverPx[1]);
            float brightness = ColorService.getBrightness(hoverPixelColor);
            int hoverColor = brightness >= 0.5 ? 0x55000000 : 0x55ffffff;
            int hx = getRenderingX(hoverPx[0]);
            int hy = getRenderingY(hoverPx[1]);
            if (BRUSH_SIZE == 3) {
                hx -= (int)scale;
                hy -= (int)scale;
            }
            int maxHx = hx + (int)(scale * BRUSH_SIZE);
            int maxHy = hy + (int)(scale * BRUSH_SIZE);
            g.fill(hx, hy, maxHx, maxHy, hoverColor);

            if (canDrawLine()) {
                canvas.calculateLine(hoverPx[0], hoverPx[1]);
                for (int[] line: canvas.getPreviewLinePixels()) {
                    int lineHoverPixelColor = canvas.getPixel(line[0], line[1]);
                    float lineBrightness = ColorService.getBrightness(lineHoverPixelColor);
                    int lineHoverColor = lineBrightness >= 0.5 ? 0x55000000 : 0x55ffffff;
                    int lX = getRenderingX(line[0]);
                    int lY = getRenderingY(line[1]);
                    int maxLX = lX + (int)(scale * 1);
                    int maxLY = lY + (int)(scale * 1);
                    g.fill(lX, lY, maxLX, maxLY, lineHoverColor);
                }
            }
        }

        renderFaceGuidelines(g, renderX, renderY, scale);
        renderSegmentBorders(g, renderX, renderY, scale);
        renderSelectionFrame(g);
        g.disableScissor();
    }

    private int getRenderingX(int original) {
        return currentRenderXY()[0] + (int)(original * currentScale());
    }
    private int getRenderingY(int original) {
        return currentRenderXY()[1] + (int)(original * currentScale());
    }

    private void renderSelectionFrame(GuiGraphics g) {
        if (canvas == null || !TOOL_MODE.equals("selection")) { return; }
        if (!canvas.isSelectionStarted() && !canvas.isSelectionSet()) { return; }

        int[] start;
        int[] end;
        if (canvas.isSelectionStarted()) {
            start = canvas.getTempSelectionStart();
            end = canvas.getTempSelectionEnd();
        } else {
            start = canvas.getSelectionStart();
            end = canvas.getSelectionEnd();
        }
        if (start == null || end == null) return;
        int minX = getRenderingX(start[0] + moveOffsetX);
        int minY = getRenderingY(start[1] + moveOffsetY);
        int maxX = getRenderingX(end[0] + moveOffsetX);
        int maxY = getRenderingY(end[1] + moveOffsetY);
        // セレクト枠表示
        g.fill(minX, minY, maxX, minY + 1, 0x55ffffff);
        g.fill(minX,  maxY - 1, maxX, maxY, 0x55ffffff);
        g.fill(minX, minY, minX + 1, maxY, 0x55ffffff);
        g.fill(maxX - 1, minY, maxX, maxY, 0x55ffffff);
        g.fill(minX + 1, minY + 1, maxX - 1, minY + 2, 0x55000000);
        g.fill(minX + 1,  maxY - 2, maxX - 1, maxY - 1, 0x55000000);
        g.fill(minX + 1, minY + 1, minX + 2, maxY - 1, 0x55000000);
        g.fill(maxX - 2, minY + 1, maxX - 1, maxY - 1, 0x55000000);
    }

    private void renderSegmentBorders(GuiGraphics g, int renderX, int renderY, float scale) {
        if (canvas == null) return;

        ItemStack mainStack = menu.getPatternContainer().getItem(DesignerMenu.MAIN_SLOT);
        if (mainStack.isEmpty() || !(mainStack.getItem() instanceof PatternItem patternItem)) return;

        PatternType type = patternItem.getPatternType(mainStack);
        PatternType.CanvasSegment[] segments = type.getSegments();
        if (segments.length <= 1) return;

        Set<Integer> rowBoundaries = new TreeSet<>();
        Set<Integer> colBoundaries = new TreeSet<>();
        for (PatternType.CanvasSegment seg : segments) {
            rowBoundaries.add(seg.canvasY());
            colBoundaries.add(seg.canvasX());
        }

        for (int cy : rowBoundaries) {
            if (cy == 0) continue;
            int ly = renderY + (int)(cy * scale);
            g.fill(renderX, ly, renderX + (int)(type.getCanvasW() * scale), ly + 1, 0xAAFFFF00);
        }
        for (int cx : colBoundaries) {
            if (cx == 0) continue;
            int lx = renderX + (int)(cx * scale);
            g.fill(lx, renderY, lx + 1, renderY + (int)(type.getCanvasH() * scale), 0xAAFFFF00);
        }

        String[][] labels = segmentLabels2D(type);
        if (labels != null) {
            for (PatternType.CanvasSegment seg : segments) {
                int col = getColIndex(segments, seg.canvasX());
                int row = getRowIndex(segments, seg.canvasY());
                if (row < labels.length && col < labels[row].length) {
                    String label = labels[row][col];
                    int lx = renderX + (int)(seg.canvasX() * scale) + 2;
                    int ly = renderY + (int)(seg.canvasY() * scale) + 2;
                    g.drawString(font, label, lx, ly, 0xFFAAAAAA, false);
                }
            }
        }
    }

    private String[][] segmentLabels2D(PatternType type) {
        return switch (type) {
            case CHEST -> new String[][]{
                    {"Body", "R.Arm", "L.Arm"},
                    {"BodyOv", "R.ArmOv", "L.ArmOv"}
            };
            case LEGS -> new String[][]{
                    {"R.Leg", "L.Leg"},
                    {"R.LegOv", "L.LegOv"}
            };
            case FEET -> new String[][]{
                    {"R.Sole", "L.Sole"},
                    {"R.Side", "L.Side"}
            };
            default -> null;
        };
    }

    private int getColIndex(PatternType.CanvasSegment[] segs, int canvasX) {
        List<Integer> cols = new ArrayList<>();
        for (PatternType.CanvasSegment s : segs) {
            if (!cols.contains(s.canvasX())) cols.add(s.canvasX());
        }
        Collections.sort(cols);
        return cols.indexOf(canvasX);
    }

    private int getRowIndex(PatternType.CanvasSegment[] segs, int canvasY) {
        List<Integer> rows = new ArrayList<>();
        for (PatternType.CanvasSegment s : segs) {
            if (!rows.contains(s.canvasY())) rows.add(s.canvasY());
        }
        Collections.sort(rows);
        return rows.indexOf(canvasY);
    }

    private void drawGrid(GuiGraphics g, int rx, int ry, int rw, int rh, float scale) {
        int gridColor = 0x33FFFFFF;
        int gridSubColor = 0x33000000;
        for (int x = 0; x <= canvas.getWidth(); x++) {
            int lx = rx + (int)(x * scale);
            g.fill(lx, ry, lx + 1, ry + rh, gridColor);
            g.fill(lx - 1, ry, lx, ry + rh, gridSubColor);
        }
        for (int y = 0; y <= canvas.getHeight(); y++) {
            int ly = ry + (int)(y * scale);
            g.fill(rx, ly, rx + rw, ly + 1, gridColor);
            g.fill(rx, ly - 1, rx + rw, ly, gridSubColor);
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

        float savedXRot = mc.player.getXRot();
        float savedXRotO = mc.player.xRotO;
        mc.player.setXRot(previewPitch);
        mc.player.xRotO = previewPitch;

        try {
            Quaternionf pose = new Quaternionf()
                    .rotateZ((float) Math.PI)
                    .rotateY((float) Math.toRadians(previewYaw));
            int centerX = leftPos + PV_X + PV_W / 2;
            int centerY = topPos + PV_Y + PV_H / 2 + 50;

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

    private Map<PatternType, int[]> buildPreviewPixelMap() {
        Map<PatternType, int[]> map = new EnumMap<>(PatternType.class);

        if (canvas != null) {
            ItemStack mainStack = menu.getPatternContainer().getItem(DesignerMenu.MAIN_SLOT);
            if (!mainStack.isEmpty() && mainStack.getItem() instanceof PatternItem patternItem) {
                PatternType type = patternItem.getPatternType(mainStack);
                map.put(type, canvas.getPixels()); // 保存前のライブデータ
            }
        }

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
            map.putIfAbsent(type, pixelData.pixels());
        }

        return map;
    }

    private void renderRgbLabels(GuiGraphics g) {
        int ly = topPos + ED_Y + ED_H + RGB_Y_OFFSET;
        g.drawString(font, "R", leftPos + 8, ly + 1, 0xFFFFFF, false);
        g.drawString(font, "G", leftPos + 8 + RGB_BOX_W + 6, ly + 1, 0xFFFFFF, false);
        g.drawString(font, "B", leftPos + 8 + (RGB_BOX_W * 2) + 12, ly + 1, 0xFFFFFF, false);
    }

    private void renderUnsavedWarning(GuiGraphics g) {
        int wx = leftPos + imageWidth  / 2 - 64;
        int wy = topPos + imageHeight / 2 - 22;

        g.fill(wx - 4, wy - 4, wx + 132, wy + 48, 0xDD000000);
        g.drawString(font, Component.translatable("gui.tailormade.designer.warning.title").getString(), wx, wy, 0xFF5555, false);
        g.drawString(font, Component.translatable("gui.tailormade.designer.warning.description").getString(), wx, wy + 12, 0xFFFFFF, false);

        g.fill(wx,      wy + 26, wx + 58,  wy + 38, 0xFF4444);
        g.drawString(font, Component.translatable("gui.tailormade.modal.close").getString(),  wx + 4,  wy + 28, 0xFFFFFF, false);

        g.fill(wx + 64, wy + 26, wx + 128, wy + 38, 0x444444);
        g.drawString(font, Component.translatable("gui.tailormade.modal.cancel").getString(), wx + 68, wy + 28, 0xFFFFFF, false);
    }

    private void renderPopups(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        if (popUpMode == null) return;
        int x = leftPos + 5;
        int y = topPos + 4;
        int w = 114;
        int h = 192;

        if (popUpMode.equals("template")) {
            g.blit(POPUP_BG_TEMPLATE, x, y, 0, 0, w, h, w, h);

            // scrollable
            if (this.scrollableTemplates != null) {
                this.scrollableTemplates.render(g, mouseX, mouseY, partialTick);
            }
        }
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (showUnsavedWarning) return handleWarningClick(mx, my);
        if (popUpMode != null) {
            if (popUpMode.equals("template")) {
                return handleTemplateClick(mx, my);
            }
        }

        IS_CONTROLLING_LAYER = false;

        if (palette.mouseClicked(mx, my)) {
            clickedArea = "palette";
            syncRgbBoxesFromPalette();
            if (!palette.isEraserMode()) {
                colorPicker.setBaseColor(palette.getSelectedColor());
            }
            return true;
        }
        if (hueBar.mousePressed(mx, my)) {
            colorPicker.setBaseColor(hueBar.getSelectedBaseColor());
            return true;
        }
        if (colorPicker.mousePressed(mx, my)) {
            int picked = colorPicker.getSelectedColor();
            palette.setRgb(
                    (picked >> 16) & 0xFF,
                    (picked >>  8) & 0xFF,
                    picked & 0xFF
            );
            syncRgbBoxesFromPalette();
            return true;
        }
        if (button == 1 && inEditorArea(mx, my) && zoomScale > 1.0f) {
            rightDragStartX = mx;
            rightDragStartY = my;
            return true;
        }
        if (canvas != null && inEditorArea(mx, my)) {
            canvas.snapshot();
            clickedArea = "editor";
            applyBrush(mx, my);
            return true;
        }
        if (button == 0 && inTemplateButton(mx, my)) {
            this.prepareTemplates();
            return true;
        }
        if (button == 0 && inSegmentButton(mx, my)) {
            mouseClickedOnSegBar(mx, my);
            return true;
        }
        if (button == 0 && inLayerAddButton(mx, my)) {
            if (canvas == null) return true;
            if (!canvas.canAddLayer()) return true;
            IS_CONTROLLING_LAYER = true;
            canvas.addLayer();
            return true;
        }
        if (button == 0 && inLayerArea(mx, my)) {
            if (canvas == null) return true;
            IS_CONTROLLING_LAYER = true;
            int index = getLayerIndex(mx, my);
            canvas.setActiveLayerIndex(index);
            if (inLayerActionButton(mx, my)) {
                canvas.toggleVisibility(canvas.getActiveLayerIndex());
            }
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
        if (inBrushSizebar(mx, my)) {
            clickedArea = "brushsizebar";
            mouseClickedOnBrushSizeBar(mx, my);
            return true;
        }
        clickedArea = null;
        return super.mouseClicked(mx, my, button);
    }

    private void mouseClickedOnToolBar(double mx, double my) {
        if (mx >= (this.leftPos + TOOLBAR_X) && mx <= (this.leftPos + TOOLBAR_X + 10) && my >= (this.topPos + TOOLBAR_Y) && my <= (this.topPos + TOOLBAR_Y + 10)) {
            setToolMode("brush");
        } else if (mx >= (this.leftPos + TOOLBAR_X) && mx <= (this.leftPos + TOOLBAR_X + 10) && my >= (this.topPos + TOOLBAR_Y + 12) && my <= (this.topPos + TOOLBAR_Y + 20)) {
            setToolMode("eraser");
        } else if (mx >= (this.leftPos + TOOLBAR_X) && mx <= (this.leftPos + TOOLBAR_X + 10) && my >= (this.topPos + TOOLBAR_Y + 24) && my <= (this.topPos + TOOLBAR_Y + 34)) {
            setToolMode("bucket");
        } else if (mx >= (this.leftPos + TOOLBAR_X) && mx <= (this.leftPos + TOOLBAR_X + 10) && my >= (this.topPos + TOOLBAR_Y + 36) && my <= (this.topPos + TOOLBAR_Y + 46)) {
            setToolMode("eyedropper");
        } else if (mx >= (this.leftPos + TOOLBAR_X) && mx <= (this.leftPos + TOOLBAR_X + 10) && my >= (this.topPos + TOOLBAR_Y + 48) && my <= (this.topPos + TOOLBAR_Y + 58)) {
            setToolMode("selection");
        }
    }

    private void mouseClickedOnBrushSizeBar(double mx, double my) {
        int startY = this.topPos + TOOLBAR_Y + 60;
        if (mx >= (this.leftPos + TOOLBAR_X) && mx <= (this.leftPos + TOOLBAR_X + 10) && my >= (startY) && my <= (startY + 5)) {
            BRUSH_SIZE = 1;
        } else if (mx >= (this.leftPos + TOOLBAR_X) && mx <= (this.leftPos + TOOLBAR_X + 10) && my >= (startY + 6) && my <= (startY + 11)) {
            BRUSH_SIZE = 2;
        } else if (mx >= (this.leftPos + TOOLBAR_X) && mx <= (this.leftPos + TOOLBAR_X + 10) && my >= (startY + 12) && my <= (startY + 17)) {
            BRUSH_SIZE = 3;
        }
    }

    private void mouseClickedOnSegBar(double mx, double my) {
        int startX = this.leftPos + SEG_BTN_X + 17;
        if (mx >= startX && mx <= startX + 13) {
            guideMode = "regular";
        } else if (mx >= startX + 14 && mx <= startX + 31) {
            guideMode = "slim";
        } else if (mx >= startX + 32 && mx <= startX + 46) {
            guideMode = "none";
        }
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        if (button == 0 && canvas != null && inEditorArea(mx, my) && clickedArea != null && clickedArea.equals("editor")) {
            applyBrush(mx, my);
            return true;
        }
        if (button == 0 && dragStartX >= 0) {
            previewYaw += (float)(mx - dragStartX) * 1.0f;
            previewPitch = Math.clamp(
                    previewPitch + (float)(my - dragStartY) * 0.5f,
                    -180.0f, 180.0f
            );
            dragStartX = mx;
            dragStartY = my;
            return true;
        }
        if (button == 1 && rightDragStartX >= 0 && canvas != null) {
            panOffsetX += (float)(mx - rightDragStartX);
            panOffsetY += (float)(my - rightDragStartY);
            rightDragStartX = mx;
            rightDragStartY = my;
            return true;
        }
        if (hueBar.mouseDragged(mx, my)) {
            colorPicker.setBaseColor(hueBar.getSelectedBaseColor());
            int base = hueBar.getSelectedBaseColor();
            palette.setRgb((base >> 16) & 0xFF, (base >> 8) & 0xFF, base & 0xFF);
            syncRgbBoxesFromPalette();
            return true;
        }
        if (colorPicker.mouseDragged(mx, my)) {
            int picked = colorPicker.getSelectedColor();
            palette.setRgb(
                    (picked >> 16) & 0xFF,
                    (picked >>  8) & 0xFF,
                    picked & 0xFF
            );
            syncRgbBoxesFromPalette();
            return true;
        }
        return super.mouseDragged(mx, my, button, dx, dy);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double dx, double dy) {
        if (!inEditorArea(mx, my) || canvas == null) return super.mouseScrolled(mx, my, dx, dy);
        if (popUpMode != null) {
            if (popUpMode.equals("template")) {
                return super.mouseScrolled(mx, my, dx, dy);
            }
        }

        float oldScale = currentScale();
        float minZoom = 1.0f;
        float newZoom = Math.max(minZoom, zoomScale + (dy > 0 ? 0.25f : -0.25f));

        float maxZoom = Math.min(ED_W, ED_H) / 16.0f / fitScale();
        newZoom = Math.min(newZoom, maxZoom);

        float newScale = fitScale() * newZoom;
        float scaleDelta = newScale / oldScale;

        int[] rxy = currentRenderXY();
        panOffsetX = (float)(mx - (mx - rxy[0]) * scaleDelta - (leftPos + ED_X + (ED_W - canvas.getWidth() * newScale) / 2));
        panOffsetY = (float)(my - (my - rxy[1]) * scaleDelta - (topPos + ED_Y + (ED_H - canvas.getHeight() * newScale) / 2));

        zoomScale = newZoom;
        if (zoomScale <= 1.0f) { panOffsetX = 0; panOffsetY = 0; }

        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.nameInput.isFocused()) {
            if (this.nameInput.keyPressed(keyCode, scanCode, modifiers)) {
                return true;
            }
            if (keyCode != GLFW.GLFW_KEY_ESCAPE) {
                return false;
            }
            return super.keyPressed(keyCode, scanCode, modifiers);
        }

        // レイヤーコントロール時
        if (IS_CONTROLLING_LAYER) {
            if (canvas == null) return true;
            if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
                canvas.toggleVisibility(canvas.getActiveLayerIndex());
            } else if (keyCode == GLFW.GLFW_KEY_DELETE) {
                canvas.removeLayer(canvas.getActiveLayerIndex());
            } else if (keyCode == GLFW.GLFW_KEY_UP) {
                int nextInt = canvas.moveLayer(true);
                if (nextInt == -1) { return true; }
                thumbnailManager.update(canvas.getActiveLayerIndex(), canvas.getActiveLayerPixels());
                thumbnailManager.update(nextInt, canvas.getLayerPixels(nextInt));
                canvas.setActiveLayerIndex(nextInt);
            } else if (keyCode == GLFW.GLFW_KEY_DOWN) {
                int nextInt = canvas.moveLayer(false);
                if (nextInt == -1) { return true; }
                thumbnailManager.update(canvas.getActiveLayerIndex(), canvas.getActiveLayerPixels());
                thumbnailManager.update(nextInt, canvas.getLayerPixels(nextInt));
                canvas.setActiveLayerIndex(nextInt);
            } else if (keyCode == GLFW.GLFW_KEY_LEFT) {
                canvas.setLayerAlpha(-5);
            } else if (keyCode == GLFW.GLFW_KEY_RIGHT) {
                canvas.setLayerAlpha(5);
            }
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

        // 範囲指定
        if (canvas != null && canvas.isMovingSelection()) {
            if (keyCode == GLFW.GLFW_KEY_LEFT) {
                canvas.addMoveOffset("x", -1);
            } else if (keyCode == GLFW.GLFW_KEY_RIGHT) {
                canvas.addMoveOffset("x", 1);
            } else if (keyCode == GLFW.GLFW_KEY_UP) {
                canvas.addMoveOffset("y", -1);
            } else if (keyCode == GLFW.GLFW_KEY_DOWN) {
                canvas.addMoveOffset("y", 1);
            }
            return true;
        }

        // エディタ系
        if (keyCode == GLFW.GLFW_KEY_P || keyCode == GLFW.GLFW_KEY_B) {
            setToolMode("brush");
            return true;
        } else if (keyCode == GLFW.GLFW_KEY_E) {
            setToolMode("eraser");
            return true;
        } else if (keyCode == GLFW.GLFW_KEY_I) {
            setToolMode("eyedropper");
            return true;
        } else if (keyCode == GLFW.GLFW_KEY_G) {
            setToolMode("bucket");
            return true;
        } else if (keyCode == GLFW.GLFW_KEY_M || keyCode == GLFW.GLFW_KEY_V) {
            setToolMode("selection");
            return true;
        } else if (keyCode == GLFW.GLFW_KEY_1) {
            BRUSH_SIZE = 1;
            return true;
        } else if (keyCode == GLFW.GLFW_KEY_2) {
            BRUSH_SIZE = 2;
            return true;
        } else if (keyCode == GLFW.GLFW_KEY_3) {
            BRUSH_SIZE = 3;
            return true;
        }

        boolean ctrl = (modifiers & GLFW.GLFW_MOD_CONTROL) != 0;
        boolean shift = (modifiers & GLFW.GLFW_MOD_SHIFT) != 0;
        if (ctrl && keyCode == GLFW.GLFW_KEY_Z) {
            if (canvas == null) return true;
            if (shift) {
                canvas.redo();
            } else {
                canvas.undo();
            }
            hasUnsavedChanges = canvas.canUndo();
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        if (button == 0) {
            dragStartX = -1;
            dragStartY = -1;
            if (TOOL_MODE.equals("selection") && canvas != null) {
                if (canvas.isSelectionStarted()) {
                    canvas.setSelectionAuto();
                } else if (canvas.isMovingSelection()) {
                    canvas.commitMoveSelection();
                    canvas.clearSelection();
                    moveOffsetX = 0;
                    moveOffsetY = 0;
                } else if (!canvas.isSelectionSet()) {
                    canvas.clearSelection();
                }
            }
        }
        if (button == 1) { rightDragStartX = -1; rightDragStartY = -1; }
        if ("editor".equals(clickedArea) && canvas != null) {
            canvas.commitPendingAction();
            thumbnailManager.update(canvas.getActiveLayerIndex(), canvas.getActiveLayerPixels());
        }
        clickedArea = null;
        hueBar.mouseReleased();
        colorPicker.mouseReleased();
        return super.mouseReleased(mx, my, button);
    }

    private void setToolMode(String mode) {
        if (canvas != null) {
            // 範囲選択じゃない場合は範囲選択用設定をクリアする
            if (!mode.equals("selection")) {
                canvas.clearSelection();
            }
            // ブラシ・消しゴム以外の場合＆ツール変更の場合は直線引く用の前座標を削除する
            if (!mode.equals("eraser") && !mode.equals("brush")) {
                canvas.clearLastPixel();
            } else if (!Objects.equals(TOOL_MODE, mode)) {
                canvas.clearLastPixel();
            }
        }
        TOOL_MODE = mode;
    }

    private boolean handleWarningClick(double mx, double my) {
        int wx = leftPos + imageWidth  / 2 - 64;
        int wy = topPos + imageHeight / 2 - 22;
        if (inBox(mx, my, wx, wy + 26, 58, 12)) {
            hasUnsavedChanges = false;
            super.onClose();
            return true;
        }
        if (inBox(mx, my, wx + 64, wy + 26, 64, 12)) {
            showUnsavedWarning = false;
            return true;
        }
        return true;
    }

    private boolean handleTemplateClick(double mx, double my) {
        int x = leftPos + 5;
        int y = topPos + 4;
        int w = 114;
        int h = 192;
        if (inBox(mx, my, x, y, w, h)) {
            scrollableTemplates.mouseClicked(mx, my, 1);
            return true;
        } else {
            // ウィジェット系リセット
            this.closeTemplateModal();
            return true;
        }
    }

    private void closeTemplateModal() {
        this.scrollableTemplates.visible = false;
        this.scrollableTemplates = null;
        popUpMode = null;
    }

    private boolean canDrawLine() {
        return canvas != null && canvas.drawLine() && Screen.hasShiftDown();
    }

    private void applyBrush(double mx, double my) {
        if (canvas == null) return;
        float scale = currentScale();
        int[] rxy = currentRenderXY();
        int renderX = rxy[0];
        int renderY = rxy[1];

        int[] px = screenToPixel((int) mx, (int) my, renderX, renderY, scale);
        if (px == null) return;

        if (Objects.equals(TOOL_MODE, "eyedropper")) {
            int color = canvas.getPixel(px[0], px[1]);
            palette.setSelectedColor(color);
            palette.syncRgbFromColor();
            hueBar.setHueFromColor(color);
            colorPicker.setSelectedColor(color);
            syncRgbBoxesFromPalette();
            if (beforeEyedropperTool != null) {
                TOOL_MODE = beforeEyedropperTool;
            }
            return;
        } else if (TOOL_MODE.equals("selection")) {
            if (!canvas.isSelectionStarted() && !canvas.isSelectionSet()) {
                canvas.setSelectionStart(px[0], px[1]);
            } else if (canvas.isSelectionSet()) {
                if (canvas.isMovingSelection()) {
                    double diffX = mx - dragStartX;
                    double diffY = my - dragStartY;
                    moveOffsetX = (int) ((int) diffX / scale);
                    moveOffsetY = (int) ((int) diffY / scale);
                    canvas.updateMoveOffset(moveOffsetX, moveOffsetY);
                } else if (!canvas.isInSelection(px[0], px[1])) {
                    canvas.setSelectionStart(px[0], px[1]);
                } else if (!canvas.isMovingSelection()) {
                    dragStartX = mx;
                    dragStartY = my;
                    canvas.startMoveSelection();
                }
            } else {
                canvas.setSelectionSelectEnd(px[0], px[1]);
            }
        } else if (palette.isEraserMode()) {
            if (Objects.equals(TOOL_MODE, "bucket")) {
                canvas.fill(px[0], px[1], TRANSPARENT);
            } else {
                if (canDrawLine()) {
                    canvas.commitLine(px[0], px[1], PixelCanvas.TRANSPARENT, BRUSH_SIZE);
                } else {
                    canvas.erase(px[0], px[1], BRUSH_SIZE);
                }
                canvas.setLastPixel(px[0], px[1]);
            }
        } else {
            if (Objects.equals(TOOL_MODE, "bucket")) {
                canvas.fill(px[0], px[1], palette.getSelectedColor());
            } else {
                int color = TOOL_MODE.equals("eraser") ? TRANSPARENT : palette.getSelectedColor();
                if (canDrawLine()) {
                    canvas.commitLine(px[0], px[1], color, BRUSH_SIZE);
                } else {
                    canvas.setPixel(px[0], px[1], color, BRUSH_SIZE);
                }
                canvas.setLastPixel(px[0], px[1]);
            }
        }

        if (!TOOL_MODE.equals("selection")) {
            hasUnsavedChanges = true;
            wasPaintingStroke = true;
        }
    }

    private int[] screenToPixel(int mx, int my, int renderX, int renderY, float scale) {
        int px = (int)((mx - renderX) / scale);
        int py = (int)((my - renderY) / scale);
        if (px < 0 || px >= canvas.getWidth() || py < 0 || py >= canvas.getHeight()) return null;
        return new int[]{px, py};
    }

    private float fitScale() {
        if (canvas == null) return 1.0f;
        return Math.min((float) ED_W / canvas.getWidth(), (float) ED_H / canvas.getHeight());
    }

    private float currentScale() {
        return fitScale() * zoomScale;
    }

    private int[] currentRenderXY() {
        float scale = currentScale();
        int renderW = (int)(canvas.getWidth()  * scale);
        int renderH = (int)(canvas.getHeight() * scale);
        int baseX = leftPos + ED_X + (ED_W - renderW) / 2;
        int baseY = topPos + ED_Y + (ED_H - renderH) / 2;
        return new int[]{
                (int)(baseX + panOffsetX),
                (int)(baseY + panOffsetY)
        };
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
        return inBox(mx, my, leftPos + TOOLBAR_X, topPos + TOOLBAR_Y, 16, 60);
    }

    private boolean inBrushSizebar(double mx, double my) {
        return inBox(mx, my, leftPos + TOOLBAR_X, topPos + TOOLBAR_Y + 60, 16, 18);
    }

    private boolean inTemplateButton(double mx, double my) {
        return inBox(mx, my, leftPos + 6, topPos + 210, 43, 9);
    }

    private boolean inSegmentButton(double mx, double my) {
        return inBox(mx, my, leftPos + SEG_BTN_X, topPos + SEG_BTN_Y, 64, 9);
    }

    private boolean inLayerAddButton(double mx, double my) {
        if (canvas == null) return false;
        int y = topPos + LAYER_BAR_Y - ((canvas.getLayers().size()) * 12);
        return inBox(mx, my, leftPos + TOOLBAR_X, y, 16, 12);
    }

    private boolean inLayerArea(double mx, double my) {
        if (canvas == null) return false;
        int y = topPos + LAYER_BAR_Y - ((canvas.getLayers().size() - 1) * 12);
        return inBox(mx, my, leftPos + TOOLBAR_X, y, 16, (canvas.getLayers().size() * 12));
    }
    private boolean inLayerActionButton(double mx, double my) {
        // 各レイヤーの操作系をクリックしたかどうか
        if (canvas == null) return false;
        int y = topPos + LAYER_BAR_Y - ((canvas.getLayers().size() - 1) * 12);
        int diff = (int) my - y;
        int divided = (int) Math.ceil(diff / 12);
        return inBox(mx, my, leftPos + TOOLBAR_X, y + (12 * divided) + 6, 6, 6);
    }
    private int getLayerIndex(double mx, double my) {
        if (canvas == null) return -1;
        int y = topPos + LAYER_BAR_Y - ((canvas.getLayers().size() - 1) * 12);
        int diff = (int) my - y;
        int divided = (int) Math.ceil(diff / 12);
        return canvas.getLayers().size() - divided - 1;
    }

    private void onRgbEdited() {
        try {
            int r = Integer.parseInt(rBox.getValue());
            int g = Integer.parseInt(gBox.getValue());
            int b = Integer.parseInt(bBox.getValue());
            palette.setRgb(r, g, b);
        } catch (NumberFormatException ignored) {}
    }

    private void syncRgbBoxesFromPalette() {
        if (palette.isEraserMode()) return;
        int r = palette.getRValue();
        int g = palette.getGValue();
        int b = palette.getBValue();
        rBox.setValue(String.valueOf(r));
        gBox.setValue(String.valueOf(g));
        bBox.setValue(String.valueOf(b));
    }

    private void prepareTemplates() {
        ItemStack mainStack = menu.getPatternContainer().getItem(DesignerMenu.MAIN_SLOT);
        if (mainStack.isEmpty() || !(mainStack.getItem() instanceof PatternItem patternItem)) {
            templates = new ArrayList<>();
            return;
        }
        PatternType type = patternItem.getPatternType(mainStack);
        String typeName = type.getType();

        templates = DesignTemplateCache.getByType(typeName);

        this.scrollableTemplates = new ScrollableWidget<>(leftPos + 13, topPos + 28, 96, 140, "えらんでね", 20, 7, templates, selected -> {
            this.reflectTemplateDesignToEditor(selected);
        }, TemplateScrollWidget.getRenderer());
        this.scrollableTemplates.setOutlineVisible(false);
        this.scrollableTemplates.setBackgroundColor(0xffdfc9a3);

        popUpMode = "template";
    }

    private void reflectTemplateDesignToEditor(DesignTemplate template) {
        int[] design = template.pixelData();
        if (design != null && canvas != null) {
            // TODO: レイヤーをリセットするべきか否か…要検討
            canvas.loadPixels(design);
            canvas.init();
            if (thumbnailManager != null) {
                thumbnailManager.update(canvas.getActiveLayerIndex(), canvas.getActiveLayerPixels());
            }
        }
        this.closeTemplateModal();
    }

    private void onSave() {
        if (canvas == null) return;
        ItemStack mainStack = menu.getPatternContainer().getItem(DesignerMenu.MAIN_SLOT);
        if (mainStack.isEmpty() || !(mainStack.getItem() instanceof PatternItem)) return;

        List<PixelCanvas.Layer> layers = canvas.getLayers();
        List<LayerData> layerData = new ArrayList<>();
        for (int i = 0; i < layers.size(); i++) {
            layerData.add(
                    LayerService.newLayer(i, i, new PixelData(layers.get(i).pixelData()), layers.get(i).isVisible())
            );
        }

        PacketDistributor.sendToServer(
                new SaveDesignPayload(DesignerMenu.MAIN_SLOT, new PixelData(canvas.getPixels()), this.nameInput.getValue(), layerData)
        );

        hasUnsavedChanges = false;
        this.onClose();
    }

    @Override
    public void onClose() {
        if (hasUnsavedChanges && !showUnsavedWarning) {
            showUnsavedWarning = true;
            return;
        }
        if (canvas != null) { canvas.close(); canvas = null; }
        if (thumbnailManager != null) { thumbnailManager.closeAll(); }
        super.onClose();
    }

    @Override
    public void removed() {
        if (canvas != null) canvas.close();
        if (previewCompositor != null) previewCompositor.close();
        if (hueBar != null) hueBar.close();
        if (colorPicker != null) colorPicker.close();
        try {
            GLFW.glfwDestroyCursor(CURSOR_DEFAULT);
            GLFW.glfwDestroyCursor(CURSOR_CROSSHAIR);
            GLFW.glfwDestroyCursor(CURSOR_HAND);
        } catch (Exception e) {
            System.out.println("[DesignerScreen.removed] " + e.getMessage());
        }
        super.removed();
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        //
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void renderFaceGuidelines(GuiGraphics g, int renderX, int renderY, float scale) {
        if (canvas == null) return;
        ItemStack mainStack = menu.getPatternContainer().getItem(DesignerMenu.MAIN_SLOT);
        if (mainStack.isEmpty() || !(mainStack.getItem() instanceof PatternItem patternItem)) return;
        if (guideMode.equals("none")) return;

        PatternType type = patternItem.getPatternType(mainStack);
        PatternType.FaceSegment[] segments = guideMode.equals("regular") ? type.getFaceSegments() : type.getSlimSegments();

        for (PatternType.FaceSegment r : segments) {
            int sx = renderX + (int)(r.canvasX() * scale);
            int sy = renderY + (int)(r.canvasY() * scale);
            int sw = (int)(r.w() * scale);
            int sh = (int)(r.h() * scale);

            g.fill(sx, sy, sx + sw, sy + 1, FACE_LINE_COLOR);
            g.fill(sx, sy + sh, sx + sw, sy + sh + 1, FACE_LINE_COLOR);
            g.fill(sx, sy, sx + 1, sy + sh, FACE_LINE_COLOR);
            g.fill(sx + sw, sy, sx + sw + 1, sy + sh, FACE_LINE_COLOR);

            if (sw >= 16 && sh >= 8) {
                g.drawString(font, r.label(), sx + 2, sy + 2, FACE_LABEL_COLOR, false);
            }
        }
    }
}