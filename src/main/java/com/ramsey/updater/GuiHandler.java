package com.ramsey.updater;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.internal.BrandingControl;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = Main.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class GuiHandler {
    private static boolean wasDisplayed;

    @SubscribeEvent
    public static void onScreenOpen(ScreenEvent.Opening event) {
        if (!wasDisplayed && event.getNewScreen() instanceof TitleScreen) {
            updateBrands();
            init(event);
            wasDisplayed = true;
        }
    }

    @SuppressWarnings({"InstantiationOfUtilityClass", "unchecked"})
    private static void updateBrands() {
        BrandingControl brandingControl = new BrandingControl();

        try {
            Field brandingsField = BrandingControl.class.getDeclaredField("brandings");
            brandingsField.setAccessible(true);

            Method computeBrandingMethod = BrandingControl.class.getDeclaredMethod("computeBranding");
            computeBrandingMethod.setAccessible(true);
            computeBrandingMethod.invoke(null);

            List<String> brands = new ArrayList<>((List<String>) brandingsField.get(brandingControl));

            if (UpdateChecker.updateState == UpdateChecker.UpdateState.FailedToFetch) {
                brands.add("§cFailed to fetch update information");
            }

            brandingsField.set(brandingControl, brands);
        } catch (NoSuchFieldException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException exception) {
            throw new RuntimeException(exception);
        }
    }

    private static void init(ScreenEvent.Opening event) {
        if (UpdateChecker.updateState == UpdateChecker.UpdateState.UpdateAvailable) {
            event.setNewScreen(new ConfirmScreen(GuiHandler::startUpdate,
                Component.translatable("gui.updater.available.title"),
                Component.translatable("gui.updater.available.message"),
                Component.translatable("gui.updater.confirm"),
                Component.translatable("gui.updater.refuse")
            ));
        }
    }

    private static void startUpdate(boolean confirmed) {
        Minecraft minecraft = Minecraft.getInstance();

        if (!confirmed) {
            minecraft.stop();
        }

        UpdateScreen updateScreen = new UpdateScreen();

        minecraft.setScreen(updateScreen);
        UpdateHandler.startUpdate(updateScreen);
    }
}
