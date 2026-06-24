package com.tailormade.tailor.client;

import com.tailormade.tailor.client.screen.BleachScreen;
import com.tailormade.tailor.client.screen.DesignerScreen;
import com.tailormade.tailor.client.screen.TailorScreen;
import com.tailormade.tailor.registries.ModMenuTypes;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

import static com.tailormade.tailor.Tailormade.MODID;

@EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
public class ClientSetup {
    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenuTypes.DESIGNER_MENU.get(), DesignerScreen::new);
        event.register(ModMenuTypes.TAILOR_MENU.get(), TailorScreen::new);
        event.register(ModMenuTypes.BLEACH_MENU.get(), BleachScreen::new);
    }
}
