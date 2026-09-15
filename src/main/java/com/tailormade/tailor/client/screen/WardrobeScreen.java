package com.tailormade.tailor.client.screen;

import com.tailormade.tailor.client.gui.ColorPalette;
import com.tailormade.tailor.client.gui.ColorPickerWidget;
import com.tailormade.tailor.client.gui.HueBarWidget;
import com.tailormade.tailor.client.gui.PowderRoomEditableRegions;
import com.tailormade.tailor.client.renderer.SkinLayerRenderLayer;
import com.tailormade.tailor.client.renderer.TailorTextureCompositor;
import com.tailormade.tailor.client.renderer.UnderwearTextureCompositor;
import com.tailormade.tailor.data.*;
import com.tailormade.tailor.network.payloads.SaveUnderwearPayload;
import com.tailormade.tailor.utils.MannequinStylePreviewHelper;
import com.tailormade.tailor.utils.PixelCanvas;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.*;

import static com.tailormade.tailor.Tailormade.MODID;

public class WardrobeScreen extends Screen {
    private static final ResourceLocation GUI_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/wardrobe_gui.png");

    private static final int GUI_W = 176;
    private static final int GUI_H = 128;
    private static final int GUI_OFFSET_X = 40;
    private static final int GUI_OFFSET_Y = 64;

    private static final int PAL_X = 16;
    private static final int PAL_Y = 10;

    private static final int PV_X = 96;
    private static final int PV_Y = 10;
    private static final int PV_W = 64;
    private static final int PV_H = 96;

    private static final int TYPE_X = 48;
    private static final int TYPE_Y = 22;
    private static final int TYPE_SIZE = 18;

    private int leftPos;
    private int topPos;
    private int imageWidth;
    private int imageHeight;

    private UnderwearType selectedType = UnderwearType.MALE_BOXER;
    private int selectedColor = 0xFF000000;
    private ColorPalette palette;
    private ColorPickerWidget colorPicker;
    private HueBarWidget hueBar;

    private float previewYaw = 235.0f;
    private float previewPitch = 0.0f;
    private double lastDragX;
    private boolean draggingPreview = false;
    private double dragStartX = -1;
    private double dragStartY = -1;
    private Button saveButton;

    private PixelCanvas canvas;
    private UnderwearTextureCompositor underwearCompositor;
    private TailorTextureCompositor previewCompositor;
    private static final ResourceLocation defaultUnderwearLocation = ResourceLocation.fromNamespaceAndPath(MODID, "textures/underwear/male_boxer.png");
    private ResourceLocation composedTexture = null;

    public WardrobeScreen() {
        super(Component.translatable("gui.tailormade.wardrobe"));
        this.imageWidth = GUI_W;
        this.imageHeight = GUI_H;
    }

    @Override
    protected void init() {
        leftPos = (width  - GUI_W) / 2;
        topPos = (height - GUI_H) / 2;

        palette = new ColorPalette(leftPos + PAL_X, topPos + PAL_Y);
        palette.disableEraser();
        hueBar = new HueBarWidget(leftPos + PAL_X, topPos + PAL_Y + 8 * 8);
        hueBar.setH(16);
        hueBar.init();
        colorPicker = new ColorPickerWidget(leftPos + PAL_X, topPos + PAL_Y + 8 * 8 + 16);
        colorPicker.setH(16);
        colorPicker.init();
        colorPicker.setBaseColor(hueBar.getSelectedBaseColor());

        Minecraft mc = Minecraft.getInstance();
        UnderwearSetting current = null;
        if (mc.player != null) {
            current = UnderwearDataClientCache.get(mc.player.getUUID());
            if (current != null) {
                selectedType = current.type();
                selectedColor = current.color();
                palette.setSelectedColor(current.color());
            }
        }

        PixelData existingData = SkinDataClientCache.get(mc.player.getUUID());
        if (existingData == null) {
            canvas = new PixelCanvas(64, 64);
            canvas.init();
            canvas.fill(0xFFC8A882);
            PowderRoomEditableRegions.lockNonEditablePixels(canvas);
            canvas.setIsSkin(true);
        }

        previewCompositor = TailorTextureCompositor.createForPreview();
        underwearCompositor = new UnderwearTextureCompositor();
        underwearCompositor.init(current != null ? current.type().getTexture() : defaultUnderwearLocation);
        applyUnderwearPreview();

        int saveX = leftPos + PV_X + PV_W - 63;
        int saveY = topPos + PV_Y + PV_H + 3;
        saveButton = Button.builder(Component.translatable("gui.tailormade.designer.save"), btn -> onSave())
                .pos(saveX, saveY)
                .size(65, 24)
                .build();
        addRenderableWidget(saveButton);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBg(g, partialTick, mouseX, mouseY);
        palette.render(g, mouseX, mouseY);
        hueBar.render(g, mouseX, mouseY);
        colorPicker.render(g, mouseX, mouseY);
        renderPreview(g, mouseX, mouseY);
        saveButton.render(g, mouseX, mouseY, partialTick);
    }

    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - GUI_W) / 2;
        int y = (this.height - GUI_H) / 2;
        g.fill(0, 0, this.width, this.height, 0x80000000);
        g.blit(GUI_TEXTURE, x, y, GUI_OFFSET_X, GUI_OFFSET_Y, GUI_W, GUI_H, 256, 256);
    }

    private void renderPreview(GuiGraphics g, int mouseX, int mouseY) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        if (canvas != null) {
            Map<PatternType, int[]> pixelMap = buildPreviewPixelMap();
            ResourceLocation skinTex = previewCompositor.composeForPreview(pixelMap);
            SkinLayerRenderLayer.setSkinPreview(skinTex);
        }

        if (composedTexture != null) {
            SkinLayerRenderLayer.setUnderwearPreview(new UnderwearSetting(selectedType, selectedColor), composedTexture);
        } else {
            SkinLayerRenderLayer.setUnderwearPreview(new UnderwearSetting(selectedType, selectedColor));
        }
        MannequinStylePreviewHelper.setHideArmor(true);
        SkinLayerRenderLayer.setIsForcePreview(true);

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
                    45,
                    new Vector3f(0, 0, 0),
                    pose, null,
                    mc.player
            );
        } finally {
            mc.player.setXRot(savedXRot);
            mc.player.xRotO = savedXRotO;
            SkinLayerRenderLayer.clearPreview();
            MannequinStylePreviewHelper.setHideArmor(false);
            SkinLayerRenderLayer.setIsForcePreview(false);
        }
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (palette.mouseClicked(mx, my)) {
            if (!palette.isEraserMode()) {
                colorPicker.setBaseColor(palette.getSelectedColor());
            }
            applyUnderwearPreview();
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
            applyUnderwearPreview();
            return true;
        }
        if (inTypeBoxes(mx, my)) {
            mouseClickedOnTypeBoxes(mx, my);
            applyUnderwearPreview();
            return true;
        }
        if (button == 0 && inPreviewArea(mx, my)) {
            dragStartX = mx; dragStartY = my; return true;
        }
        return super.mouseClicked(mx, my, button);
    }

    private void mouseClickedOnTypeBoxes(double mx, double my) {
        if (mx >= (this.leftPos + TYPE_X) && mx <= (this.leftPos + TYPE_X + TYPE_SIZE) && my >= (this.topPos + TYPE_Y) && my <= (this.topPos + TYPE_Y + TYPE_SIZE)) {
            selectedType = UnderwearType.MALE_BOXER;
        } else if (mx >= (this.leftPos + TYPE_X) && mx <= (this.leftPos + TYPE_X + TYPE_SIZE) && my >= (this.topPos + TYPE_Y+ (TYPE_SIZE * 1)) && my <= (this.topPos + TYPE_Y + (TYPE_SIZE * 2))) {
            selectedType = UnderwearType.MALE_BIKINI;
        } else if (mx >= (this.leftPos + TYPE_X) && mx <= (this.leftPos + TYPE_X + TYPE_SIZE) && my >= (this.topPos + TYPE_Y+ (TYPE_SIZE * 2)) && my <= (this.topPos + TYPE_Y + (TYPE_SIZE * 3))) {
            selectedType = UnderwearType.FEMALE_BOXER;
        } else if (mx >= (this.leftPos + TYPE_X) && mx <= (this.leftPos + TYPE_X + TYPE_SIZE) && my >= (this.topPos + TYPE_Y+ (TYPE_SIZE * 3)) && my <= (this.topPos + TYPE_Y + (TYPE_SIZE * 4))) {
            selectedType = UnderwearType.FEMALE_BIKINI;
        }
        underwearCompositor.init(selectedType.getTexture());
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
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
        if (hueBar.mouseDragged(mx, my)) {
            colorPicker.setBaseColor(hueBar.getSelectedBaseColor());
            int base = hueBar.getSelectedBaseColor();
            palette.setRgb((base >> 16) & 0xFF, (base >> 8) & 0xFF, base & 0xFF);
            return true;
        }
        if (colorPicker.mouseDragged(mx, my)) {
            int picked = colorPicker.getSelectedColor();
            palette.setRgb(
                    (picked >> 16) & 0xFF,
                    (picked >>  8) & 0xFF,
                    picked & 0xFF
            );
            return true;
        }
        return super.mouseDragged(mx, my, button, dx, dy);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        if (button == 0) { dragStartX = -1; dragStartY = -1; }
        hueBar.mouseReleased();
        colorPicker.mouseReleased();
        return super.mouseReleased(mx, my, button);
    }

    private boolean inPreviewArea(double mx, double my) {
        return mx >= leftPos + PV_X && mx < leftPos + PV_X + PV_W && my >= topPos + PV_Y && my < topPos + PV_Y + PV_H;
    }

    private Map<PatternType, int[]> buildPreviewPixelMap() {
        Map<PatternType, int[]> map = new EnumMap<>(PatternType.class);

        int[] pixels = canvas.getPixels();
        for (PatternType type : PatternType.values()) {
            map.put(type, cropPixels(pixels, type));
        }

        return map;
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

    private void applyUnderwearPreview() {
        if (underwearCompositor == null) return;
        int argb = palette.getSelectedColor();
        selectedColor = palette.getSelectedColor();
        this.composedTexture = underwearCompositor.setIsPreview(true).compose(argb, null);
        underwearCompositor.setIsPreview(false);
        if (composedTexture == null) return;

        SkinLayerRenderLayer.setUnderwearPreview(new UnderwearSetting(selectedType, selectedColor), composedTexture);
    }

    private void onSave() {
        PacketDistributor.sendToServer(new SaveUnderwearPayload(new UnderwearSetting(selectedType, selectedColor)));
        onClose();
    }

    @Override
    public void removed() {
        if (underwearCompositor != null) underwearCompositor.close();
        if (hueBar != null) hueBar.close();
        if (colorPicker != null) colorPicker.close();
        super.removed();
    }

    @Override
    public boolean isPauseScreen() { return false; }


    private boolean inTypeBoxes(double mx, double my) {
        return inBox(mx, my, leftPos + TYPE_X, topPos + TYPE_Y, TYPE_SIZE, TYPE_SIZE * 4);
    }
    private boolean inBox(double mx, double my, int bx, int by, int w, int h) {
        return mx >= bx && mx < bx + w && my >= by && my < by + h;
    }
}
