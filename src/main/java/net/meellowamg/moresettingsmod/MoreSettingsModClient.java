package net.meellowamg.moresettingsmod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.network.chat.Component;

public class MoreSettingsModClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        MoreSettingsMod.LOGGER.info("More Settings Mod Client Loaded!");
        MoreSettingsConfig.load();

        // Use Fabric's screen event to add button to pause screen
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (!(screen instanceof PauseScreen)) return;

            // Find the lowest existing button Y position
            int lowestY = 0;
            for (var widget : Screens.getButtons(screen)) {
                int bottom = widget.getY() + widget.getHeight();
                if (bottom > lowestY) lowestY = bottom;
            }

            Screens.getButtons(screen).add(Button.builder(
                    Component.literal("Full Settings"),
                    btn -> client.gui.setScreen(new MoreSettingsScreen(screen))
            ).bounds(scaledWidth / 2 - 102, lowestY + 5, 204, 20).build());
        });
    }
}