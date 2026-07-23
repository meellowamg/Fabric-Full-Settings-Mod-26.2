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

    // ===== SOUND =====
    public float masterVolume   = 1.0f;
    public float musicVolume    = 1.0f;
    public float weatherVolume  = 1.0f;
    public float hostileVolume  = 1.0f;
    public float neutralVolume  = 1.0f;
    public float ambientVolume  = 1.0f;
    public float voiceVolume    = 1.0f;

    // ===== VIDEO =====
    public float   brightness         = 0.5f;
    public int     fov                = 70;
    public int     renderDistance     = 12;
    public int     simulationDistance = 12;
    public int     maxFramerate       = 120;
    public boolean fullscreen         = false;
    public boolean vsync              = true;
    public boolean clouds             = true;
    public int     guiScale           = 0; // 0 = auto
    public boolean bobView            = true;
    public boolean highContrastBlocks = false;
    public boolean entityShadows      = true;
    public boolean particles          = true; // true=all
    public boolean smoothLighting     = true;
    public int     mipmapLevels       = 4;

    // ===== CHAT =====
    public float   chatOpacity      = 1.0f;
    public float   chatScale        = 1.0f;
    public float   chatWidth        = 1.0f;
    public float   chatLineSpacing  = 0.0f;
    public float   chatDelay        = 0.0f;
    public boolean chatVisibility   = true;
    public boolean chatColors       = true;
    public boolean chatLinks        = true;
    public boolean chatLinksPrompt  = true;

    // ===== GAMEPLAY =====
    public boolean autoJump          = true;
    public boolean toggleSprint      = false;
    public boolean toggleCrouch      = false;
    public boolean reducedDebugInfo  = false;
    public boolean showSubtitlesInGame = false;
    public boolean sneakToggle       = false;
    public boolean operatorTabList   = false;

    // ===== ACCESSIBILITY =====
    public boolean subtitles         = false;
    public boolean textBackground    = true;
    public float   textBackgroundOp  = 0.5f;
    public boolean highContrast      = false;
    public boolean darkMojangScreen  = false;
    public boolean hideLightningFlash = false;
    public float   damageTilt        = 1.0f;
    public float   panoramaSpeed     = 1.0f;
    public boolean monochromeLogo    = false;
    public float   glintSpeed        = 0.5f;
    public float   glintStrength     = 0.75f;
    public boolean narratorEnabled   = false;

    // ===== MOUSE =====
    public float   mouseSensitivity  = 0.5f;
    public boolean invertYAxis       = false;
    public boolean discreteScrolling = false;
    public boolean touchscreen       = false;
    public float   mouseWheelSensitivity = 1.0f;
    public boolean rawMouseInput     = true;

    // ===== SKINS =====
    public boolean showCape          = true;
    public boolean showJacket        = true;
    public boolean showLeftSleeve    = true;
    public boolean showRightSleeve   = true;
    public boolean showLeftPants     = true;
    public boolean showRightPants    = true;
    public boolean showHat           = true;

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