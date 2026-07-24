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

    // ===== SOUND CATEGORIES =====
    public float masterVolume   = 1.0f;
    public float musicVolume    = 1.0f;
    public float weatherVolume  = 1.0f;
    public float hostileVolume  = 1.0f;
    public float neutralVolume  = 1.0f;
    public float ambientVolume  = 1.0f;
    public float voiceVolume    = 1.0f;

    // ===== MUSIC =====
    public float musicCreative  = 1.0f;
    public float musicEnd       = 1.0f;
    public float musicNether    = 1.0f;
    public float musicOverworld = 1.0f;
    public float musicUnderwater = 1.0f;
    public float musicMenu      = 1.0f;

    // ===== WEATHER =====
    public float soundRain      = 1.0f;
    public float soundThunder   = 1.0f;

    // ===== HOSTILE MOBS =====
    public float soundZombieAmbient  = 1.0f;
    public float soundZombieHurt     = 1.0f;
    public float soundZombieDeath    = 1.0f;
    public float soundZombieStep     = 1.0f;
    public float soundSkeletonAmbient = 1.0f;
    public float soundSkeletonHurt   = 1.0f;
    public float soundSkeletonDeath  = 1.0f;
    public float soundSkeletonStep   = 1.0f;
    public float soundCreeperPrimed  = 1.0f;
    public float soundCreeperHurt    = 1.0f;
    public float soundCreeperDeath   = 1.0f;
    public float soundSpiderAmbient  = 1.0f;
    public float soundSpiderHurt     = 1.0f;
    public float soundSpiderDeath    = 1.0f;
    public float soundSpiderStep     = 1.0f;
    public float soundEndermanAmbient = 1.0f;
    public float soundEndermanScream = 1.0f;
    public float soundEndermanHurt   = 1.0f;
    public float soundEndermanDeath  = 1.0f;
    public float soundEndermanTeleport = 1.0f;
    public float soundBlazeAmbient   = 1.0f;
    public float soundBlazeShoot     = 1.0f;
    public float soundBlazeHurt      = 1.0f;
    public float soundBlazeDeath     = 1.0f;
    public float soundGhastAmbient   = 1.0f;
    public float soundGhastScream    = 1.0f;
    public float soundGhastShoot     = 1.0f;
    public float soundGhastWarn      = 1.0f;
    public float soundGhastHurt      = 1.0f;
    public float soundGhastDeath     = 1.0f;
    public float soundSlimeAttack    = 1.0f;
    public float soundSlimeJump      = 1.0f;
    public float soundSlimeSquish    = 1.0f;
    public float soundSlimeDeath     = 1.0f;
    public float soundWitherSpawn    = 1.0f;
    public float soundWitherAmbient  = 1.0f;
    public float soundWitherShoot    = 1.0f;
    public float soundWitherHurt     = 1.0f;
    public float soundWitherDeath    = 1.0f;
    public float soundDragonAmbient  = 1.0f;
    public float soundDragonGrowl    = 1.0f;
    public float soundDragonHurt     = 1.0f;
    public float soundDragonDeath    = 1.0f;
    public float soundDragonFlap     = 1.0f;
    public float soundGuardianAmbient = 1.0f;
    public float soundGuardianAttack = 1.0f;
    public float soundGuardianHurt   = 1.0f;
    public float soundGuardianDeath  = 1.0f;
    public float soundShulkerAmbient = 1.0f;
    public float soundShulkerOpen    = 1.0f;
    public float soundShulkerClose   = 1.0f;
    public float soundShulkerShoot   = 1.0f;
    public float soundShulkerHurt    = 1.0f;
    public float soundShulkerDeath   = 1.0f;
    public float soundShulkerTeleport = 1.0f;
    public float soundWitchAmbient   = 1.0f;
    public float soundWitchHurt      = 1.0f;
    public float soundWitchDeath     = 1.0f;
    public float soundHuskAmbient    = 1.0f;
    public float soundHuskHurt       = 1.0f;
    public float soundHuskDeath      = 1.0f;
    public float soundStrayAmbient   = 1.0f;
    public float soundStrayHurt      = 1.0f;
    public float soundStrayDeath     = 1.0f;
    public float soundPhantomAmbient = 1.0f;
    public float soundPhantomBite    = 1.0f;
    public float soundPhantomHurt    = 1.0f;
    public float soundPhantomDeath   = 1.0f;
    public float soundDrownedAmbient = 1.0f;
    public float soundDrownedHurt    = 1.0f;
    public float soundDrownedDeath   = 1.0f;
    public float soundPillagerAmbient = 1.0f;
    public float soundPillagerHurt   = 1.0f;
    public float soundPillagerDeath  = 1.0f;
    public float soundRavagerAmbient = 1.0f;
    public float soundRavagerAttack  = 1.0f;
    public float soundRavagerHurt    = 1.0f;
    public float soundRavagerDeath   = 1.0f;
    public float soundHoglinAmbient  = 1.0f;
    public float soundHoglinAttack   = 1.0f;
    public float soundHoglinHurt     = 1.0f;
    public float soundHoglinDeath    = 1.0f;
    public float soundPiglinAmbient  = 1.0f;
    public float soundPiglinAngry    = 1.0f;
    public float soundPiglinHurt     = 1.0f;
    public float soundPiglinDeath    = 1.0f;
    public float soundWardenAmbient  = 1.0f;
    public float soundWardenNearbyClose = 1.0f;
    public float soundWardenSonicBoom = 1.0f;
    public float soundWardenRoar     = 1.0f;
    public float soundWardenHurt     = 1.0f;
    public float soundWardenDeath    = 1.0f;
    public float soundWardenEmerge   = 1.0f;
    public float soundWardenDig      = 1.0f;
    public float soundBreezeIdle     = 1.0f;
    public float soundBreezeShoot    = 1.0f;
    public float soundBreezeHurt     = 1.0f;
    public float soundBreezeDeath    = 1.0f;
    public float soundSilverfishAmbient = 1.0f;
    public float soundSilverfishHurt = 1.0f;
    public float soundSilverfishDeath = 1.0f;
    public float soundMagmaCubeJump  = 1.0f;
    public float soundMagmaCubeSquish = 1.0f;
    public float soundMagmaCubeDeath = 1.0f;
    public float soundVexAmbient     = 1.0f;
    public float soundVexCharge      = 1.0f;
    public float soundVexHurt        = 1.0f;
    public float soundVexDeath       = 1.0f;
    public float soundEvokerAmbient  = 1.0f;
    public float soundEvokerCast     = 1.0f;
    public float soundEvokerHurt     = 1.0f;
    public float soundEvokerDeath    = 1.0f;
    public float soundVindicatorAmbient = 1.0f;
    public float soundVindicatorHurt = 1.0f;
    public float soundVindicatorDeath = 1.0f;
    public float soundEndermiteAmbient = 1.0f;
    public float soundEndermiteHurt  = 1.0f;
    public float soundEndermiteDeath = 1.0f;
    public float soundIllusionerAmbient = 1.0f;
    public float soundIllusionerCast = 1.0f;
    public float soundIllusionerHurt = 1.0f;
    public float soundIllusionerDeath = 1.0f;
    public float soundBoggedAmbient  = 1.0f;
    public float soundBoggedHurt     = 1.0f;
    public float soundBoggedDeath    = 1.0f;
    public float soundCreakingAmbient = 1.0f;
    public float soundCreakingActivate = 1.0f;
    public float soundCreakingHurt   = 1.0f;
    public float soundCreakingDeath  = 1.0f;

    // ===== NEUTRAL MOBS =====
    public float soundPigAmbient     = 1.0f;
    public float soundPigHurt        = 1.0f;
    public float soundPigDeath       = 1.0f;
    public float soundPigStep        = 1.0f;
    public float soundCowAmbient     = 1.0f;
    public float soundCowHurt        = 1.0f;
    public float soundCowDeath       = 1.0f;
    public float soundCowStep        = 1.0f;
    public float soundCowMilk        = 1.0f;
    public float soundSheepAmbient   = 1.0f;
    public float soundSheepHurt      = 1.0f;
    public float soundSheepDeath     = 1.0f;
    public float soundSheepShear     = 1.0f;
    public float soundChickenAmbient = 1.0f;
    public float soundChickenHurt    = 1.0f;
    public float soundChickenDeath   = 1.0f;
    public float soundChickenEgg     = 1.0f;
    public float soundWolfAmbient    = 1.0f;
    public float soundWolfGrowl      = 1.0f;
    public float soundWolfHurt       = 1.0f;
    public float soundWolfDeath      = 1.0f;
    public float soundWolfPant       = 1.0f;
    public float soundCatAmbient     = 1.0f;
    public float soundCatHurt        = 1.0f;
    public float soundCatDeath       = 1.0f;
    public float soundCatPurr        = 1.0f;
    public float soundCatHiss        = 1.0f;
    public float soundHorseAmbient   = 1.0f;
    public float soundHorseHurt      = 1.0f;
    public float soundHorseDeath     = 1.0f;
    public float soundHorseGallop    = 1.0f;
    public float soundHorseJump      = 1.0f;
    public float soundBeeAmbient     = 1.0f;
    public float soundBeeHurt        = 1.0f;
    public float soundBeeDeath       = 1.0f;
    public float soundBeeSting       = 1.0f;
    public float soundFoxAmbient     = 1.0f;
    public float soundFoxHurt        = 1.0f;
    public float soundFoxDeath       = 1.0f;
    public float soundFoxScreech     = 1.0f;
    public float soundPandaAmbient   = 1.0f;
    public float soundPandaSneeze    = 1.0f;
    public float soundPandaHurt      = 1.0f;
    public float soundPandaDeath     = 1.0f;
    public float soundLlamaAmbient   = 1.0f;
    public float soundLlamaHurt      = 1.0f;
    public float soundLlamaDeath     = 1.0f;
    public float soundLlamaSpit      = 1.0f;
    public float soundDolphinAmbient = 1.0f;
    public float soundDolphinHurt    = 1.0f;
    public float soundDolphinDeath   = 1.0f;
    public float soundTurtleAmbient  = 1.0f;
    public float soundTurtleHurt     = 1.0f;
    public float soundTurtleDeath    = 1.0f;
    public float soundPolarBearAmbient = 1.0f;
    public float soundPolarBearHurt  = 1.0f;
    public float soundPolarBearDeath = 1.0f;
    public float soundPolarBearWarning = 1.0f;
    public float soundGoatAmbient    = 1.0f;
    public float soundGoatHurt       = 1.0f;
    public float soundGoatDeath      = 1.0f;
    public float soundGoatMilk       = 1.0f;
    public float soundGoatRamImpact  = 1.0f;
    public float soundAxolotlAmbient = 1.0f;
    public float soundAxolotlHurt    = 1.0f;
    public float soundAxolotlDeath   = 1.0f;
    public float soundAllayAmbient   = 1.0f;
    public float soundAllayHurt      = 1.0f;
    public float soundAllayDeath     = 1.0f;
    public float soundFrogAmbient    = 1.0f;
    public float soundFrogHurt       = 1.0f;
    public float soundFrogDeath      = 1.0f;
    public float soundFrogTongue     = 1.0f;
    public float soundSnifferAmbient = 1.0f;
    public float soundSnifferSniffing = 1.0f;
    public float soundSnifferHurt    = 1.0f;
    public float soundSnifferDeath   = 1.0f;
    public float soundCamelAmbient   = 1.0f;
    public float soundCamelHurt      = 1.0f;
    public float soundCamelDeath     = 1.0f;
    public float soundArmadilloAmbient = 1.0f;
    public float soundArmadilloHurt  = 1.0f;
    public float soundArmadilloDeath = 1.0f;
    public float soundArmadilloRoll  = 1.0f;

    // ===== AMBIENT =====
    public float soundCaveAmbient    = 1.0f;
    public float soundWater          = 1.0f;
    public float soundLava           = 1.0f;
    public float soundFire           = 1.0f;
    public float soundPortal         = 1.0f;
    public float soundUnderwaterAmbient = 1.0f;
    public float soundBeaconAmbient  = 1.0f;
    public float soundConduitAmbient = 1.0f;
    public float soundSculkSensor    = 1.0f;
    public float soundSculkShrieker  = 1.0f;

    // ===== PLAYER =====
    public float soundPlayerHurt     = 1.0f;
    public float soundPlayerDeath    = 1.0f;
    public float soundPlayerEat      = 1.0f;
    public float soundPlayerBurp     = 1.0f;
    public float soundPlayerSwim     = 1.0f;
    public float soundPlayerSplash   = 1.0f;
    public float soundPlayerLevelUp  = 1.0f;
    public float soundPlayerAttack   = 1.0f;
    public float soundPlayerTeleport = 1.0f;

    // ===== BLOCKS =====
    public float soundBlockBreak     = 1.0f;
    public float soundBlockPlace     = 1.0f;
    public float soundChest          = 1.0f;
    public float soundDoorWood       = 1.0f;
    public float soundDoorIron       = 1.0f;
    public float soundTrapdoor       = 1.0f;
    public float soundButton         = 1.0f;
    public float soundPiston         = 1.0f;
    public float soundTNT            = 1.0f;
    public float soundExplosion      = 1.0f;
    public float soundAnvil          = 1.0f;
    public float soundEnchantTable   = 1.0f;
    public float soundNoteBlock      = 1.0f;
    public float soundAmethyst       = 1.0f;
    public float soundBell           = 1.0f;
    public float soundCampfire       = 1.0f;
    public float soundGrindstone     = 1.0f;
    public float soundBrewingStand   = 1.0f;

    // ===== ITEMS / COMBAT =====
    public float soundArrowShoot     = 1.0f;
    public float soundArrowHit       = 1.0f;
    public float soundSweep          = 1.0f;
    public float soundShieldBlock    = 1.0f;
    public float soundShieldBreak    = 1.0f;
    public float soundItemPickup     = 1.0f;
    public float soundItemBreak      = 1.0f;
    public float soundCrossbow       = 1.0f;
    public float soundTrident        = 1.0f;
    public float soundTotem          = 1.0f;
    public float soundXPPickup       = 1.0f;
    public float soundFishing        = 1.0f;
    public float soundFirework       = 1.0f;
    public float soundElytra         = 1.0f;
    public float soundBucket         = 1.0f;
    public float soundEnderPearl     = 1.0f;
    public float soundPotion         = 1.0f;

    // ===== UI =====
    public float soundUIClick        = 1.0f;
    public float soundToast          = 1.0f;
    public float soundVillagerAmbient = 1.0f;
    public float soundVillagerTrade  = 1.0f;
    public float soundVillagerHurt   = 1.0f;
    public float soundVillagerDeath  = 1.0f;
    public float soundLightning      = 1.0f;

    // ===== VIDEO =====
    public float   brightness         = 0.5f;
    public int     fov                = 70;
    public int     renderDistance     = 12;
    public int     simulationDistance = 12;
    public int     maxFramerate       = 120;
    public boolean fullscreen         = false;
    public boolean vsync              = true;
    public boolean clouds             = true;
    public int     guiScale           = 0;
    public boolean bobView            = true;
    public boolean entityShadows      = true;
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

    // ===== ACCESSIBILITY =====
    public boolean subtitles         = false;
    public boolean textBackground    = true;
    public float   textBackgroundOp  = 0.5f;
    public boolean highContrast      = false;
    public boolean hideLightningFlash = false;
    public boolean darkMojangScreen  = false;
    public boolean monochromeLogo    = false;
    public float   damageTilt        = 1.0f;
    public float   glintSpeed        = 0.5f;
    public float   glintStrength     = 0.75f;
    public float   panoramaSpeed     = 1.0f;

    // ===== MOUSE =====
    public float   mouseSensitivity     = 0.5f;
    public boolean invertYAxis          = false;
    public boolean discreteScrolling    = false;
    public boolean touchscreen          = false;
    public float   mouseWheelSensitivity = 1.0f;
    public boolean rawMouseInput        = true;

    // ===== SKIN LAYERS =====
    public boolean showCape         = true;
    public boolean showJacket       = true;
    public boolean showLeftSleeve   = true;
    public boolean showRightSleeve  = true;
    public boolean showLeftPants    = true;
    public boolean showRightPants   = true;
    public boolean showHat          = true;

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