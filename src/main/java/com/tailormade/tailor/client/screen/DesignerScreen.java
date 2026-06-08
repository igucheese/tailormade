package com.tailormade.tailor.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.tailormade.tailor.client.gui.ColorPalette;
import com.tailormade.tailor.client.gui.ColorPickerWidget;
import com.tailormade.tailor.client.gui.HueBarWidget;
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

public class DesignerScreen extends AbstractContainerScreen<DesignerMenu> {
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

    private static final int GUI_W = 384;
    private static final int GUI_H = 216;
    private static final int GUI_OFFSET_X = 64;
    private static final int GUI_OFFSET_Y = 148;

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
    private static int BRUSH_SIZE = 1;
    private static String TOOL_MODE = "brush";

    private static final int RGB_Y_OFFSET = 4;  // エディタ下端からの距離
    private static final int RGB_BOX_W = 28;
    private static final int RGB_BOX_H = 10;

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
    private boolean isOnEditBox = false;
    private String beforeEyedropperTool = null;

    private float previewYaw   = 235.0f; // 正面が見える初期値
    private float previewPitch = 0.0f;
    private double lastDragX;
    private boolean draggingPreview = false;
    private double dragStartX = -1;
    private double dragStartY = -1;

    private ItemStack lastPatternStack = ItemStack.EMPTY;
    private TailorTextureCompositor previewCompositor;

    private static final int FACE_LINE_COLOR  = 0x3300DDFF;
    private static final int FACE_LABEL_COLOR = 0x7700DDFF;

    public DesignerScreen(DesignerMenu menu, Inventory playerInv, Component title) {
        super(menu, playerInv, title);
        this.imageWidth  = GUI_W;
        this.imageHeight = GUI_H;
    }

    @Override
    protected void init() {
        super.init();

        palette = new ColorPalette(leftPos + PAL_X, topPos + PAL_Y);
        refreshCanvas();

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

        hueBar = new HueBarWidget(leftPos + PAL_X, topPos + PAL_Y + 8 * 9 + 4);
        hueBar.setH(32);
        hueBar.init();
        colorPicker = new ColorPickerWidget(leftPos + PAL_X, topPos + PAL_Y + 8 * 9 + 38);
        colorPicker.init();
        colorPicker.setBaseColor(hueBar.getSelectedBaseColor());

        this.nameInput = new EditBox(this.font, leftPos + PV_X + PV_W - 70, topPos  + PV_Y + PV_H - 6, 72, 20, Component.translatable("gui.tailormade.tailor.pattern_name.placeholder"));
        this.nameInput.setMaxLength(15);
        this.nameInput.setHint(Component.translatable("gui.tailormade.tailor.pattern_name.placeholder"));
        this.addRenderableWidget(this.nameInput);

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

    private void refreshCanvas() {
        ItemStack mainStack = menu.getPatternContainer().getItem(DesignerMenu.MAIN_SLOT);
        if (mainStack.isEmpty() || !(mainStack.getItem() instanceof PatternItem patternItem)) {
            if (canvas != null) { canvas.close(); canvas = null; }
            return;
        }

        PatternType type  = patternItem.getPatternType(mainStack);
        int[] size        = type.getTextureSize();

        if (canvas != null && canvas.getWidth() == size[0] && canvas.getHeight() == size[1]) return;
        if (canvas != null) canvas.close();
        canvas = new PixelCanvas(size[0], size[1]);

        int[] existing = patternItem.getPixelData(mainStack);
        if (existing != null) canvas.loadPixels(existing);
        canvas.init();
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
    }

    private void renderToolabr(GuiGraphics g)
    {
        int toolBarX = this.leftPos + TOOLBAR_X;
        int toolBarY = this.topPos + TOOLBAR_Y;
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
            int hx = renderX + (int)(hoverPx[0] * scale);
            int hy = renderY + (int)(hoverPx[1] * scale);
            g.fill(hx, hy, hx + (int)scale, hy + (int)scale, 0x55FFFFFF);
        }

        renderFaceGuidelines(g, renderX, renderY, scale);
        renderSegmentBorders(g, renderX, renderY, scale);
        g.disableScissor();
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
        g.drawString(font, "G", leftPos + 8 + RGB_BOX_W  + 6, ly + 1, 0xFFFFFF, false);
        g.drawString(font, "B", leftPos + 8 + (RGB_BOX_W * 2) + 12, ly + 1, 0xFFFFFF, false);
    }

    private void renderUnsavedWarning(GuiGraphics g) {
        int wx = leftPos + imageWidth  / 2 - 64;
        int wy = topPos  + imageHeight / 2 - 22;

        g.fill(wx - 4, wy - 4, wx + 132, wy + 48, 0xDD000000);
        g.drawString(font, Component.translatable("gui.tailormade.designer.warning.title").getString(), wx, wy, 0xFF5555, false);
        g.drawString(font, Component.translatable("gui.tailormade.designer.warning.description").getString(), wx, wy + 12, 0xFFFFFF, false);

        g.fill(wx,      wy + 26, wx + 58,  wy + 38, 0xFF4444);
        g.drawString(font, Component.translatable("gui.tailormade.modal.close").getString(),  wx + 4,  wy + 28, 0xFFFFFF, false);

        g.fill(wx + 64, wy + 26, wx + 128, wy + 38, 0x444444);
        g.drawString(font, Component.translatable("gui.tailormade.modal.cancel").getString(), wx + 68, wy + 28, 0xFFFFFF, false);
    }

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
        if (hueBar.mousePressed(mx, my)) {
            colorPicker.setBaseColor(hueBar.getSelectedBaseColor());
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

        float oldScale = currentScale();
        float minZoom = 1.0f;  // fitScale 相当が縮小限界
        float newZoom = Math.max(minZoom, zoomScale + (dy > 0 ? 0.25f : -0.25f));

        // ズーム限界（16x16 が表示できる程度）
        float maxZoom = Math.min(ED_W, ED_H) / 16.0f / fitScale();
        newZoom = Math.min(newZoom, maxZoom);

        float newScale = fitScale() * newZoom;
        float scaleDelta = newScale / oldScale;

        // マウス位置を中心に拡縮
        int[] rxy = currentRenderXY();
        panOffsetX = (float)(mx - (mx - rxy[0]) * scaleDelta - (leftPos + ED_X + (ED_W - canvas.getWidth() * newScale) / 2));
        panOffsetY = (float)(my - (my - rxy[1]) * scaleDelta - (topPos  + ED_Y + (ED_H - canvas.getHeight() * newScale) / 2));

        zoomScale = newZoom;

        // 縮小限界ではオフセットをリセット
        if (zoomScale <= 1.0f) { panOffsetX = 0; panOffsetY = 0; }

        return true;
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
            if (canvas == null) return true;
            if (shift) {
                canvas.redo();
            } else {
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
        if (button == 1) { rightDragStartX = -1; rightDragStartY = -1; }
        clickedArea = null;
        hueBar.mouseReleased();
        colorPicker.mouseReleased();
        return super.mouseReleased(mx, my, button);
    }

    private boolean handleWarningClick(double mx, double my) {
        int wx = leftPos + imageWidth  / 2 - 64;
        int wy = topPos  + imageHeight / 2 - 22;
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

    private void applyBrush(double mx, double my) {
        if (canvas == null) return;
        float scale  = currentScale();
        int[] rxy    = currentRenderXY();
        int renderX  = rxy[0];
        int renderY  = rxy[1];

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
                canvas.fill(palette.getSelectedColor());
            } else {
                canvas.setPixel(px[0], px[1], palette.getSelectedColor(), BRUSH_SIZE);
            }
        }
        hasUnsavedChanges = true;
        wasPaintingStroke  = true;
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
        float scale  = currentScale();
        int renderW  = (int)(canvas.getWidth()  * scale);
        int renderH  = (int)(canvas.getHeight() * scale);
        int baseX    = leftPos + ED_X + (ED_W - renderW) / 2;
        int baseY    = topPos  + ED_Y + (ED_H - renderH) / 2;
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
        return inBox(mx, my, leftPos + TOOLBAR_X, topPos + TOOLBAR_Y, 16, 186);
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
        if (hueBar != null) hueBar.close();
        if (colorPicker != null) colorPicker.close();
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

        PatternType type = patternItem.getPatternType(mainStack);

        for (PatternType.FaceSegment r : type.getFaceSegments()) {
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