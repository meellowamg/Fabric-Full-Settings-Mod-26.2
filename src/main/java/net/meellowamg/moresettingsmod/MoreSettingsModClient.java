package net.meellowamg.moresettingsmod;

import net.fabricmc.api.ClientModInitializer;

public class MoreSettingsModClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        MoreSettingsMod.LOGGER.info("More Settings Mod Client Loaded!");
        MoreSettingsConfig.load();
    }
}