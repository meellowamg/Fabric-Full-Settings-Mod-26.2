package net.meellowamg.moresettingsmod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.network.chat.Component;

import java.lang.reflect.Method;

public class MoreSettingsModClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        MoreSettingsMod.LOGGER.info("More Settings Mod Client Loaded!");
        MoreSettingsConfig.load();

        ScreenEvents.AFTER_INIT.register((Minecraft client, Screen screen, int scaledWidth, int scaledHeight) -> {
            if (!(screen instanceof OptionsScreen)) return;

            // Place button below the Done button
            int buttonY = scaledHeight - 27;

            Button btn = Button.builder(
                    Component.literal("Full Settings"),
                    b -> client.gui.setScreen(new MoreSettingsScreen(screen))
            ).bounds(scaledWidth / 2 - 102, buttonY, 204, 20).build();

            try {
                Method method = Screen.class.getDeclaredMethod("addRenderableWidget",
                        net.minecraft.client.gui.components.events.GuiEventListener.class);
                method.setAccessible(true);
                method.invoke(screen, btn);
            } catch (Exception e) {
                MoreSettingsMod.LOGGER.error("Failed to add Full Settings button: " + e.getMessage());
            }
        });
    }
}