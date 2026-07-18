package net.meellowamg.moresettingsmod;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public class MoreSettingsConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance()
            .getConfigDir().resolve("moresettingsmod.json");

    // Sound
    public float masterVolume  = 1.0f;
    public float musicVolume   = 1.0f;
    public float weatherVolume = 1.0f;
    public float hostileVolume = 1.0f;
    public float neutralVolume = 1.0f;
    public float ambientVolume = 1.0f;
    public float voiceVolume   = 1.0f;

    // Video
    public float   brightness         = 0.5f;
    public int     fov                = 70;
    public int     renderDistance     = 12;
    public int     simulationDistance = 12;
    public int     maxFramerate       = 120;
    public boolean fullscreen         = false;
    public boolean vsync              = true;
    public boolean clouds             = true;

    // Chat
    public float   chatOpacity    = 1.0f;
    public float   chatScale      = 1.0f;
    public float   chatWidth      = 1.0f;
    public boolean chatVisibility = true;
    public boolean chatColors     = true;
    public boolean chatLinks      = true;

    // Gameplay
    public boolean autoJump         = true;
    public boolean toggleSprint     = false;
    public boolean toggleCrouch     = false;
    public boolean reducedDebugInfo = false;

    // Accessibility
    public boolean subtitles      = false;
    public boolean textBackground = true;
    public float   textBackgroundOp = 0.5f;

    private static MoreSettingsConfig instance;

    public static MoreSettingsConfig get() {
        if (instance == null) load();
        return instance;
    }

    public static void load() {
        if (Files.exists(CONFIG_PATH)) {
            try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
                instance = GSON.fromJson(reader, MoreSettingsConfig.class);
                if (instance == null) instance = new MoreSettingsConfig();
            } catch (IOException e) {
                MoreSettingsMod.LOGGER.error("Failed to load config", e);
                instance = new MoreSettingsConfig();
            }
        } else {
            instance = new MoreSettingsConfig();
            save();
        }
    }

    public static void save() {
        try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
            GSON.toJson(instance, writer);
        } catch (IOException e) {
            MoreSettingsMod.LOGGER.error("Failed to save config", e);
        }
    }
}