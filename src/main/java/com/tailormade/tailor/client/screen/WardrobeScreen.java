package com.tailormade.tailor.client.screen;

import com.tailormade.tailor.client.gui.ColorPalette;
import com.tailormade.tailor.client.gui.ColorPickerWidget;
import com.tailormade.tailor.client.renderer.SkinLayerRenderLayer;
import com.tailormade.tailor.data.UnderwearSetting;
import com.tailormade.tailor.data.UnderwearType;
import com.tailormade.tailor.network.payloads.SaveUnderwarePayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.neoforged.neoforge.network.PacketDistributor;
import org.joml.Quaternionf;
import org.joml.Vector3f;

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

    private UnderwearType selectedType  = UnderwearType.MALE_BOXER;
    private DyeColor selectedColor = DyeColor.WHITE;
    private ColorPalette palette;
    private ColorPickerWidget colorPicker;

    private float previewYaw   = 235.0f; // 正面が見える初期値
    private float previewPitch = 0.0f;
    private double lastDragX;
    private boolean draggingPreview = false;
    private double dragStartX = -1;
    private double dragStartY = -1;
    private Button saveButton;

    public WardrobeScreen() {
        super(Component.translatable("gui.tailormade.wardrobe"));
        this.imageWidth  = GUI_W;
        this.imageHeight = GUI_H;
    }

    @Override
    protected void init() {
        leftPos = (width  - GUI_W) / 2;
        topPos  = (height - GUI_H) / 2;

        palette = new ColorPalette(leftPos + PAL_X, topPos + PAL_Y);
        palette.disableEraser();
        colorPicker = new ColorPickerWidget(leftPos + PAL_X, topPos + PAL_Y + 8 * 9 + 4);
        colorPicker.init();

        // 現在の設定をロード
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            UnderwearSetting current = SkinLayerRenderLayer.getUnderwearSetting(mc.player.getUUID());
            if (current != null) {
                selectedType  = current.type();
                selectedColor = current.color();
                System.out.println("[CHECK][CURRENT UNDERWARE TYPE] : " + selectedType + ", COLOR: " + selectedColor);
            }
        }

        // SAVE ボタン
        int saveX = leftPos + PV_X + PV_W - 63;
        int saveY = topPos  + PV_Y + PV_H + 3;
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
        colorPicker.render(g, mouseX, mouseY);
        renderPreview(g, mouseX, mouseY);
        saveButton.render(g, mouseX, mouseY, partialTick);
//        super.render(g, mouseX, mouseY, partialTick);
    }

    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - GUI_W) / 2;
        int y = (this.height - GUI_H) / 2;
        g.fill(0, 0, this.width, this.height, 0x80000000);
        g.blit(GUI_TEXTURE, x, y, GUI_OFFSET_X, GUI_OFFSET_Y, GUI_W, GUI_H, 256, 256);
    }

    // ---- 染料スウォッチ ----------------------------------------

    // ---- 3D プレビュー ----------------------------------------

    private void renderPreview(GuiGraphics g, int mouseX, int mouseY) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        // 下着のみプレビュー
        SkinLayerRenderLayer.setUnderwearPreview(
                new UnderwearSetting(selectedType, selectedColor));

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
                    45,
                    new Vector3f(0, 0, 0),
                    pose, null,
                    mc.player
            );
        } finally {
            mc.player.setXRot(savedXRot);
            mc.player.xRotO = savedXRotO;
            SkinLayerRenderLayer.clearPreview();
        }
    }

    // ---- マウスイベント ----------------------------------------

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (palette.mouseClicked(mx, my)) {
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
            return true;
        }
        if (inTypeBoxes(mx, my)) {
            mouseClickedOnTypeBoxes(mx, my);
            return true;
        }
        // プレビュードラッグ
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
        System.out.println("[CHECK][mouseClickedOnTypeBoxes]" + selectedType);
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
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
            return true;
        }
        return super.mouseDragged(mx, my, button, dx, dy);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        if (button == 0) { dragStartX = -1; dragStartY = -1; }
        colorPicker.mouseReleased();
        return super.mouseReleased(mx, my, button);
    }

    private boolean inPreviewArea(double mx, double my) {
        return mx >= leftPos + PV_X && mx < leftPos + PV_X + PV_W
                && my >= topPos  + PV_Y && my < topPos  + PV_Y + PV_H;
    }

    // ---- SAVE -------------------------------------------------

    private void onSave() {
        PacketDistributor.sendToServer(new SaveUnderwarePayload(new UnderwearSetting(selectedType, selectedColor)));
        onClose();
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
