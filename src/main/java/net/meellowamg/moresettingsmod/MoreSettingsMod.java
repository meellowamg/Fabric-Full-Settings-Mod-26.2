package net.meellowamg.moresettingsmod;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MoreSettingsMod implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("moresettingsmod");

    @Override
    public void onInitialize() {
        LOGGER.info("More Settings Mod Loaded!");
    }
}