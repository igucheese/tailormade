package com.tailormade.tailor.client.screen;

import com.tailormade.tailor.client.gui.ColorPalette;
import com.tailormade.tailor.client.gui.ColorPickerWidget;
import com.tailormade.tailor.client.gui.HueBarWidget;
import com.tailormade.tailor.client.gui.PowderRoomEditableRegions;
import com.tailormade.tailor.client.menu.DesignerMenu;
import com.tailormade.tailor.client.renderer.SkinLayerRenderLayer;
import com.tailormade.tailor.client.renderer.TailorArmorRenderLayer;
import com.tailormade.tailor.client.renderer.TailorTextureCompositor;
import com.tailormade.tailor.client.renderer.UnderwearTextureCompositor;
import com.tailormade.tailor.client.screen.ui.CatalogScrollWidget;
import com.tailormade.tailor.client.screen.ui.ScrollableWidget;
import com.tailormade.tailor.data.*;
import com.tailormade.tailor.data.records.CatalogData;
import com.tailormade.tailor.data.records.CatalogEnlistData;
import com.tailormade.tailor.entities.items.PatternItem;
import com.tailormade.tailor.network.payloads.GetCatalogBookPayload;
import com.tailormade.tailor.network.payloads.RetrieveCatalogPayload;
import com.tailormade.tailor.network.payloads.SaveCatalogPayload;
import com.tailormade.tailor.network.payloads.SaveUnderwearPayload;
import com.tailormade.tailor.registries.ModDataComponents;
import com.tailormade.tailor.utils.CatalogService;
import com.tailormade.tailor.utils.MannequinStylePreviewHelper;
import com.tailormade.tailor.utils.PixelCanvas;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import org.checkerframework.checker.units.qual.A;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.*;

import static com.tailormade.tailor.Tailormade.MODID;

public class CatalogScreen extends Screen {
    private static final ResourceLocation GUI_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/catalog/catalog_gui.png");
    private static final ResourceLocation MANAGEMENT_GUI_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/catalog/catalog_manager_gui.png");
    private static final ResourceLocation MANAGEMENT_BUTTON =
            ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/catalog/catalog_manegement_btn.png");

    private static final ResourceLocation GUI_TAB_ALL =
            ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/catalog/catalog_gui_tab_all.png");
    private static final ResourceLocation GUI_TAB_HAT =
            ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/catalog/catalog_gui_tab_hat.png");
    private static final ResourceLocation GUI_TAB_SHIRT =
            ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/catalog/catalog_gui_tab_shirt.png");
    private static final ResourceLocation GUI_TAB_PANTS =
            ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/catalog/catalog_gui_tab_pants.png");
    private static final ResourceLocation GUI_TAB_SHOES =
            ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/catalog/catalog_gui_tab_shoes.png");

    private static final int GUI_W = 384;
    private static final int GUI_H = 256;
    private static final int GUI_OFFSET_X = 64;
    private static final int GUI_OFFSET_Y = 128;

    // 操作系ボタン
    private static final int BTN_MANAGE_X = 144;
    private static final int BTN_MANAGE_Y = 232;
    private static final int BTN_MANAGE_CANCEL_X = 16;
    private static final int BTN_MANAGE_SAVE_X = 144;
    private static final int BTN_MANAGE_EXTRACT_X = 272;
    private static final int BTN_W = 96;
    private static final int BTN_H = 16;

    // タブボタン
    private static final int TAB_X = 18;
    private static final int TAB_Y = 34;
    private static final int TAB_WHOLE_W = 204;
    private static final int TAB_W = 40;
    private static final int TAB_H = 14;
    private static final int TAB_ALL_W = 46;
    private static final int TAB_ALL_X = 18;
    private static final int TAB_HAT_X = 64;
    private static final int TAB_SHIRT_X = 104;
    private static final int TAB_PANTS_X = 144;
    private static final int TAB_SHOES_X = 184;

    // プレビュー表示エリア
    private static final int PREVIEW_X = 240;
    private static final int PREVIEW_Y = 32;
    private static final int PREVIEW_W = 128;
    private static final int PREVIEW_H = 192;

    // フォーム
    private EditBox nameInput;
    private static final int INPUT_X = 176;
    private static final int INPUT_Y = 9;
    private static final int INPUT_W = 104;
    private static final int INPUT_H = 14;

    // 型紙リスト
    private ScrollableWidget<CatalogEnlistData> customerCatalog;
    private ScrollableWidget<CatalogEnlistData> managementCatalog;
    private List<CatalogEnlistData> customerCatalogList = new ArrayList<>();
    private List<CatalogEnlistData> managerCatalogList = new ArrayList<>();

    // 閲覧モード
    private boolean isEditable = false;
    private boolean IS_MANAGEMENT_MODE = false;

    // データ
    private UUID catalogId = null;
    private UUID ownerId = null;
    private UUID previewDesignId = null;
    private CatalogData catalogData = null;
    private List<DesignDataRecord> patterns = null;
    private List<DesignDataRecord> visiblePatterns = null;
    // 管理用データ
    private UUID playerId = null;
    private List<UUID> listedPatterns = new ArrayList<>();
    private List<DesignDataRecord> allPatterns = null;
    private List<DesignDataRecord> visibleManagementPatterns = null;
    private boolean isFromLectern = false;
    private BlockPos pos = null;

    // タブ
    private String currentTab = "all";
    private String managerTab = "all";

    private int leftPos;
    private int topPos;
    private int imageWidth;
    private int imageHeight;

    private UnderwearType selectedType = UnderwearType.FEMALE_BOXER;
    private int selectedColor = 0xFFC8A882;

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
    private ResourceLocation originalTexture;
    private static final ResourceLocation defaultUnderwearLocation = ResourceLocation.fromNamespaceAndPath(MODID, "textures/underwear/male_boxer.png");
    private ResourceLocation composedTexture = null;

    public CatalogScreen(UUID id, boolean isFromLectern, BlockPos pos) {
        super(Component.translatable("gui.tailormade.catalog"));
        this.imageWidth = GUI_W;
        this.imageHeight = GUI_H;

        this.catalogId = id;
        this.isFromLectern = isFromLectern;
        this.pos = pos;
    }

    private void setCatalogData() {
        this.catalogData = CatalogService.getCatalog(this.catalogId);
        if (this.catalogData != null) {
            this.ownerId = this.catalogData.playerId();
            this.patterns = CatalogService.getCatalogContents(this.catalogData.patterns());
            this.listedPatterns = new ArrayList<>(this.catalogData.patterns());
            handleCatalogContent();
        }
    }

    @Override
    protected void init() {
        leftPos = (width  - GUI_W) / 2;
        topPos = (height - GUI_H) / 2;

        this.setCatalogData();

        Minecraft mc = Minecraft.getInstance();
        UnderwearSetting current = null;
        if (mc.player != null) {
            this.playerId = mc.player.getUUID();
            current = UnderwearDataClientCache.get(this.playerId);
            this.isEditable = this.ownerId != null ? this.ownerId.equals(this.playerId) : true;
        }

        previewCompositor = TailorTextureCompositor.createForPreview();
        originalTexture = TailorArmorRenderLayer.getPreviewOverride();
        underwearCompositor = new UnderwearTextureCompositor();
        underwearCompositor.init(current != null ? current.type().getTexture() : defaultUnderwearLocation);
        applyUnderwearPreview();

        this.nameInput = new EditBox(this.font, leftPos + INPUT_X, topPos + INPUT_Y, INPUT_W, INPUT_H, Component.translatable("gui.tailormade.catalog.catalog_name.placeholder"));
        this.nameInput.setMaxLength(50);
        this.nameInput.setHint(Component.translatable("gui.tailormade.catalog.catalog_name.placeholder"));
        this.addRenderableWidget(this.nameInput);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBg(g, partialTick, mouseX, mouseY);
        renderButton(g);
        renderTexts(g);
        renderPreview(g, mouseX, mouseY);
        renderForms(g, mouseX, mouseY, partialTick);
        renderCatalog(g, mouseX, mouseY, partialTick);
    }

    private void renderButton(GuiGraphics g) {
        if (this.isEditable && !this.IS_MANAGEMENT_MODE) {
            g.blit(MANAGEMENT_BUTTON, leftPos + BTN_MANAGE_X, topPos + BTN_MANAGE_Y, 0, 0, BTN_W, BTN_H, BTN_W, BTN_H);
        }
    }

    private void renderTexts(GuiGraphics g) {
        if (this.isEditable && this.IS_MANAGEMENT_MODE) {
            int selected = this.listedPatterns.size();
            Component selectedText = Component.translatable("gui.tailormade.catalog.select.size", selected);
            int sizeLabelWidth = this.font.width(selectedText);
            g.drawString(this.font, selectedText, leftPos + 220 - sizeLabelWidth, topPos + 208, 0xffe6d1ac);
        }
    }

    private void renderForms(GuiGraphics g, int mx, int my, float pt) {
        if (this.isEditable && this.IS_MANAGEMENT_MODE) {
            this.nameInput.render(g, mx, my, pt);
            this.nameInput.visible = true;
        } else {
            this.nameInput.visible = false;

            String name = this.catalogData != null ? this.catalogData.name() : "カタログ";
            int width = this.font.width(name);
            int x = leftPos + (192 - (width / 2));
            g.drawString(this.font, name, x, topPos + 12, 0xffdfc9a3);
        }
    }

    private void renderCatalog(GuiGraphics g, int mx, int my, float pt) {
        if (this.IS_MANAGEMENT_MODE) {
            if (this.managementCatalog != null) this.managementCatalog.render(g, mx, my, pt);
        } else {
            if (this.customerCatalog != null) this.customerCatalog.render(g, mx, my, pt);
        }
    }

    private void composeCatalog() {
        this.customerCatalog = new ScrollableWidget<>(leftPos + 24, topPos + 56, 192, 160, "えらんでね", 30, 5, customerCatalogList, selected -> {
            previewDesignId = selected.design().uuid();
        }, CatalogScrollWidget.getRenderer(false));
        this.customerCatalog.setOutlineVisible(false);
        this.customerCatalog.setBackgroundColor(0x00dfc9a3);
        this.customerCatalog.setHoverColor(0x55ffffff);

        this.managementCatalog = new ScrollableWidget<>(leftPos + 24, topPos + 56, 192, 160, "えらんでね", 30, 5, managerCatalogList, selected -> {
            // 表示リスト操作
            int index = this.managerCatalogList.indexOf(selected);
            boolean isAdding = !selected.isListed();
            if (index != -1) {
                this.managerCatalogList.set(index, new CatalogEnlistData(selected.design(), !selected.isListed()));
            }

            // 全体の管理リスト操作
            if (isAdding) {
                listedPatterns.add(selected.design().uuid());
            } else {
                listedPatterns.remove(selected.design().uuid());
            }

            // プレビュー対象
            previewDesignId = selected.design().uuid();
        }, CatalogScrollWidget.getRenderer(true));
        this.managementCatalog.setOutlineVisible(false);
        this.managementCatalog.setBackgroundColor(0x00dfc9a3);
        this.managementCatalog.setHoverColor(0x55ffffff);
    }

    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - GUI_W) / 2;
        int y = (this.height - GUI_H) / 2;
        g.fill(0, 0, this.width, this.height, 0x80000000);
        g.blit(GUI_TEXTURE, x, y, GUI_OFFSET_X, GUI_OFFSET_Y, GUI_W, GUI_H, 512, 512);
        if (this.IS_MANAGEMENT_MODE) {
            g.blit(MANAGEMENT_GUI_TEXTURE, x, y, GUI_OFFSET_X, GUI_OFFSET_Y, GUI_W, GUI_H, 512, 512);
        }
        if (!this.IS_MANAGEMENT_MODE) {
            if (this.currentTab.equals("all")) {
                g.blit(GUI_TAB_ALL, x, y, GUI_OFFSET_X, GUI_OFFSET_Y, GUI_W, GUI_H, 512, 512);
            } else if (this.currentTab.equals("head")) {
                g.blit(GUI_TAB_HAT, x, y, GUI_OFFSET_X, GUI_OFFSET_Y, GUI_W, GUI_H, 512, 512);
            } else if (this.currentTab.equals("chest")) {
                g.blit(GUI_TAB_SHIRT, x, y, GUI_OFFSET_X, GUI_OFFSET_Y, GUI_W, GUI_H, 512, 512);
            } else if (this.currentTab.equals("legs")) {
                g.blit(GUI_TAB_PANTS, x, y, GUI_OFFSET_X, GUI_OFFSET_Y, GUI_W, GUI_H, 512, 512);
            } else if (this.currentTab.equals("feet")) {
                g.blit(GUI_TAB_SHOES, x, y, GUI_OFFSET_X, GUI_OFFSET_Y, GUI_W, GUI_H, 512, 512);
            }
        } else {
            if (this.managerTab.equals("all")) {
                g.blit(GUI_TAB_ALL, x, y, GUI_OFFSET_X, GUI_OFFSET_Y, GUI_W, GUI_H, 512, 512);
            } else if (this.managerTab.equals("head")) {
                g.blit(GUI_TAB_HAT, x, y, GUI_OFFSET_X, GUI_OFFSET_Y, GUI_W, GUI_H, 512, 512);
            } else if (this.managerTab.equals("chest")) {
                g.blit(GUI_TAB_SHIRT, x, y, GUI_OFFSET_X, GUI_OFFSET_Y, GUI_W, GUI_H, 512, 512);
            } else if (this.managerTab.equals("legs")) {
                g.blit(GUI_TAB_PANTS, x, y, GUI_OFFSET_X, GUI_OFFSET_Y, GUI_W, GUI_H, 512, 512);
            } else if (this.managerTab.equals("feet")) {
                g.blit(GUI_TAB_SHOES, x, y, GUI_OFFSET_X, GUI_OFFSET_Y, GUI_W, GUI_H, 512, 512);
            }
        }
    }

    private void renderPreview(GuiGraphics g, int mouseX, int mouseY) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        Map<PatternType, int[]> pixelMap = buildPreviewPixelMap();
        if (pixelMap != null) {
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
            int centerX = leftPos + PREVIEW_X + PREVIEW_W / 2;
            int centerY = topPos + PREVIEW_Y + PREVIEW_H / 2 + 50;

            InventoryScreen.renderEntityInInventory(
                    g,
                    centerX,
                    centerY + 20,
                    75,
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

    private Map<PatternType, int[]> buildPreviewPixelMap() {
        Map<PatternType, int[]> map = new EnumMap<>(PatternType.class);
        PixelData pixelData = null;
        if (previewDesignId != null) {
            DesignDataRecord designData = DesignDataClientCache.get(previewDesignId);
            if (designData != null) {
                pixelData = designData.pixelData();
                PatternType type = PatternType.CHEST;
                switch (designData.type()) {
                    case "head" -> type = PatternType.HEAD;
                    case "chest" -> type = PatternType.CHEST;
                    case "legs" -> type = PatternType.LEGS;
                    case "feet" -> type = PatternType.FEET;
                }
                map.putIfAbsent(type, pixelData.pixels());
            }
        }
        if (pixelData == null) return null;
        return map;
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (button == 0 && inTabBoxes(mx, my)) {
            handleTabMovement(mx, my);
            return true;
        }
        if (button == 0 && inManagementBtnBox(mx, my) && !this.IS_MANAGEMENT_MODE && this.isEditable) {
            changeManagementMode(true);
            return true;
        }
        if (button == 0 && this.IS_MANAGEMENT_MODE) {
            if (inManagementExitBtnBox(mx, my)) {
                changeManagementMode(false);
                return true;
            } else if (inManagementSaveBtnBox(mx, my)) {
                this.onSave();
                return true;
            } else if (inManagementExtractBtnBox(mx, my)) {
                this.onBookIssue();
                return true;
            }
        }
        if (button ==0 && inMainBox(mx, my)) {
            if (this.IS_MANAGEMENT_MODE && this.managementCatalog != null) {
                managementCatalog.mouseClicked(mx, my, 1);
                return true;
            } else if (this.customerCatalog != null) {
                customerCatalog.mouseClicked(mx, my, 1);
                return true;
            }
        }
        if (button == 0 && inPreviewArea(mx, my)) {
            dragStartX = mx; dragStartY = my; return true;
        }
        return super.mouseClicked(mx, my, button);
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
        return super.mouseDragged(mx, my, button, dx, dy);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        if (button == 0) { dragStartX = -1; dragStartY = -1; }
        return super.mouseReleased(mx, my, button);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double dx, double dy) {
        int actualLeft = (this.width - 224) / 2;
        int actualTop = (this.height - 224) / 2;

        int mX = (int) (mx - actualLeft);
        int mY = (int) (my - actualTop);
        if (inMainBox(mx, my)) {
            if (this.IS_MANAGEMENT_MODE && this.managementCatalog != null) {
                return managementCatalog.mouseScrolled(mx, my, dx, dy);
            } else if (this.customerCatalog != null) {
                return customerCatalog.mouseScrolled(mx, my, dx, dy);
            }
        }
        return super.mouseScrolled(mx, my, dx, dy);
    }

    private void changeManagementMode(boolean isActive) {
        this.IS_MANAGEMENT_MODE = isActive;
        if (isActive) {
            handleManagementContent();
        } else {
            this.setCatalogData();
            handleCatalogContent();
        }
    }

    private void handleTabMovement(double mx, double my) {
        if (inTabAllBox(mx, my)) {
            this.setTab("all");
        } else if (inTabHatBox(mx, my)) {
            this.setTab("head");
        } else if (inTabShirtBox(mx, my)) {
            this.setTab("chest");
        } else if (inTabPantsBox(mx, my)) {
            this.setTab("legs");
        } else if (inTabShoesBox(mx, my)) {
            this.setTab("feet");
        }
    }

    private void setTab(String tab) {
        if (this.IS_MANAGEMENT_MODE) {
            this.managerTab = tab;
            handleManagementContent();
        } else {
            this.currentTab = tab;
            this.setCatalogData();
            handleCatalogContent();
        }
    }

    private void handleManagementContent() {
        if (this.playerId == null) return;
        this.allPatterns = DesignDataClientCache.getMine(this.playerId);

        if (this.managerTab.equals("all")) {
            visibleManagementPatterns = allPatterns;
        } else {
            visibleManagementPatterns = allPatterns.stream().filter(p -> p.type().equals(this.managerTab)).toList();
        }
        this.managerCatalogList = CatalogService.getCombinedManagementList(visibleManagementPatterns, listedPatterns);

        if (this.catalogData != null) {
            this.nameInput.setValue(this.catalogData.name());
        }
        composeCatalog();
    }
    private void handleCatalogContent() {
        if (this.patterns == null) {
            visiblePatterns = new ArrayList<>();
            return;
        }

        if (this.currentTab.equals("all")) {
            visiblePatterns = patterns;
        } else {
            visiblePatterns = patterns.stream().filter(p -> p.type().equals(this.currentTab)).toList();
        }
        this.customerCatalogList = CatalogService.getCombinedCustomerList(visiblePatterns);
        composeCatalog();
    }

    private boolean inPreviewArea(double mx, double my) {
        return mx >= leftPos + PREVIEW_X && mx < leftPos + PREVIEW_X + PREVIEW_W && my >= topPos + PREVIEW_Y && my < topPos + PREVIEW_Y + PREVIEW_H;
    }

    private void applyUnderwearPreview() {
        if (underwearCompositor == null) return;
        this.composedTexture = underwearCompositor.setIsPreview(true).compose(selectedColor, null);
        underwearCompositor.setIsPreview(false);
        if (composedTexture == null) return;

        SkinLayerRenderLayer.setUnderwearPreview(new UnderwearSetting(selectedType, selectedColor), composedTexture);
    }

    private void onSave() {
        String catalogName = this.nameInput.getValue();
        PacketDistributor.sendToServer(new SaveCatalogPayload(this.catalogId, catalogName, this.listedPatterns));
        onClose();
    }

    private void onBookIssue() {
        if (this.isFromLectern) {
            PacketDistributor.sendToServer(new RetrieveCatalogPayload(this.pos));
            onClose();
        } else {
            PacketDistributor.sendToServer(new GetCatalogBookPayload(this.catalogId));
        }
    }

    @Override
    public void removed() {
        if (underwearCompositor != null) underwearCompositor.close();
        TailorArmorRenderLayer.clearPreviewOverride();
        super.removed();
    }

    @Override
    public boolean isPauseScreen() { return false; }


    private boolean inManagementBtnBox(double mx, double my) {
        return inBox(mx, my, leftPos + BTN_MANAGE_X, topPos + BTN_MANAGE_Y, BTN_W, BTN_H);
    }
    private boolean inManagementExitBtnBox(double mx, double my) {
        return inBox(mx, my, leftPos + BTN_MANAGE_CANCEL_X, topPos + BTN_MANAGE_Y, BTN_W, BTN_H);
    }
    private boolean inManagementSaveBtnBox(double mx, double my) {
        return inBox(mx, my, leftPos + BTN_MANAGE_SAVE_X, topPos + BTN_MANAGE_Y, BTN_W, BTN_H);
    }
    private boolean inManagementExtractBtnBox(double mx, double my) {
        return inBox(mx, my, leftPos + BTN_MANAGE_EXTRACT_X, topPos + BTN_MANAGE_Y, BTN_W, BTN_H);
    }
    private boolean inTabBoxes(double mx, double my) {
        return inBox(mx, my, leftPos + TAB_X, topPos + TAB_Y, TAB_WHOLE_W, TAB_H);
    }
    private boolean inTabAllBox(double mx, double my) {
        return inBox(mx, my, leftPos + TAB_ALL_X, topPos + TAB_Y, TAB_ALL_W, TAB_H);
    }
    private boolean inTabHatBox(double mx, double my) {
        return inBox(mx, my, leftPos + TAB_HAT_X, topPos + TAB_Y, TAB_W, TAB_H);
    }
    private boolean inTabShirtBox(double mx, double my) {
        return inBox(mx, my, leftPos + TAB_SHIRT_X, topPos + TAB_Y, TAB_W, TAB_H);
    }
    private boolean inTabPantsBox(double mx, double my) {
        return inBox(mx, my, leftPos + TAB_PANTS_X, topPos + TAB_Y, TAB_W, TAB_H);
    }
    private boolean inTabShoesBox(double mx, double my) {
        return inBox(mx, my, leftPos + TAB_SHOES_X, topPos + TAB_Y, TAB_W, TAB_H);
    }
    private boolean inMainBox(double mx, double my) {
        return inBox(mx, my, leftPos + 16, topPos + 48, 208, 176);
    }
    private boolean inBox(double mx, double my, int bx, int by, int w, int h) {
        return mx >= bx && mx < bx + w && my >= by && my < by + h;
    }
}
