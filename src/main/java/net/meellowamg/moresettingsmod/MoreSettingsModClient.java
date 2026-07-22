package net.meellowamg.moresettingsmod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class MoreSettingsModClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        MoreSettingsMod.LOGGER.info("More Settings Mod Client Loaded!");
        MoreSettingsConfig.load();

        ScreenEvents.AFTER_INIT.register(new ScreenEvents.AfterInit() {
            @Override
            public void afterInit(Minecraft client, Screen screen, int scaledWidth, int scaledHeight) {
                if (!(screen instanceof PauseScreen)) return;

                int buttonY = scaledHeight / 4 + 160;

                Button fullSettingsButton = Button.builder(
                        Component.literal("Full Settings"),
                        btn -> client.gui.setScreen(new MoreSettingsScreen(screen))
                ).bounds(scaledWidth / 2 - 102, buttonY, 204, 20).build();

                // Use reflection to call addRenderableWidget
                try {
                    var method = Screen.class.getDeclaredMethod("addRenderableWidget",
                            net.minecraft.client.gui.components.events.GuiEventListener.class);
                    method.setAccessible(true);
                    method.invoke(screen, fullSettingsButton);
                } catch (Exception e) {
                    MoreSettingsMod.LOGGER.error("Failed to add Full Settings button", e);
                }
            }
        });
    }
}