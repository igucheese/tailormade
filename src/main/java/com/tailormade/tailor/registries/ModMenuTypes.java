package com.tailormade.tailor.registries;

import com.tailormade.tailor.client.menu.BleachMenu;
import com.tailormade.tailor.client.menu.DesignerMenu;
import com.tailormade.tailor.client.menu.ManagerMenu;
import com.tailormade.tailor.client.menu.TailorMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, "mezcraft");

    public static final DeferredHolder<MenuType<?>, MenuType<DesignerMenu>> DESIGNER_MENU =
            MENUS.register("designer_menu", () ->
                    IMenuTypeExtension.create(DesignerMenu::new)
            );

    public static final DeferredHolder<MenuType<?>, MenuType<TailorMenu>> TAILOR_MENU =
            MENUS.register("tailor_menu", () ->
                    IMenuTypeExtension.create(TailorMenu::new)
            );

    public static final DeferredHolder<MenuType<?>, MenuType<BleachMenu>> BLEACH_MENU =
            MENUS.register("bleach_menu", () ->
                    IMenuTypeExtension.create(BleachMenu::new)
            );

    public static final DeferredHolder<MenuType<?>, MenuType<ManagerMenu>> MANAGER_MENU =
            MENUS.register("manager_menu", () ->
                    IMenuTypeExtension.create(ManagerMenu::new)
            );
}
