package net.meellowamg.moresettingsmod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class MoreSettingsScreen extends Screen {

    private final Screen parent;
    private final MoreSettingsConfig config;

    private EditBox searchBox;
    private String searchQuery = "";

    private int scrollOffset = 0;
    private static final int SCROLL_AMOUNT = 15;
    private static final int ROW_H = 22;
    private static final int WIDGET_W = 300;
    private static final int WIDGET_H = 18;
    private static final int TOP_MARGIN = 32;
    private static final int BOT_MARGIN = 28;

    // Master list built once
    private final List<SettingEntry> allEntries  = new ArrayList<>();
    private final List<SettingEntry> shown       = new ArrayList<>();

    // Parallel widget state — rebuilt only when search changes
    private final List<SliderState> sliderStates = new ArrayList<>();

    public MoreSettingsScreen(Screen parent) {
        super(Component.literal("Full Settings"));
        this.parent = parent;
        this.config = MoreSettingsConfig.get();
    }

    @Override
    protected void init() {
        this.clearWidgets();

        if (allEntries.isEmpty()) buildAllEntries();

        // Search box
        searchBox = new EditBox(this.font, this.width / 2 - 150, 6, 300, 18,
                Component.literal("Search..."));
        searchBox.setMaxLength(64);
        searchBox.setHint(Component.literal("Search settings..."));
        searchBox.setValue(searchQuery);
        searchBox.setResponder(val -> {
            searchQuery = val.toLowerCase().trim();
            rebuildShown();
            scrollOffset = 0;
        });
        this.addRenderableWidget(searchBox);

        // Back button
        this.addRenderableWidget(Button.builder(
                Component.literal("Back"),
                btn -> {
                    MoreSettingsConfig.save();
                    applyAll();
                    this.minecraft.gui.setScreen(parent);
                }
        ).bounds(this.width / 2 - 50, this.height - 24, 100, 20).build());

        rebuildShown();
        buildSliderStates();
    }

    private void rebuildShown() {
        shown.clear();
        if (searchQuery.isEmpty()) {
            shown.addAll(allEntries);
        } else {
            for (SettingEntry e : allEntries) {
                if (!e.isHeader && e.label.toLowerCase().contains(searchQuery)) {
                    shown.add(e);
                }
            }
        }
        buildSliderStates();
    }

    private void buildSliderStates() {
        sliderStates.clear();
        for (SettingEntry e : shown) {
            if (e.isSlider) {
                double norm = (e.getValue.get() - e.min) / (e.max - e.min);
                norm = Math.max(0, Math.min(1, norm));
                sliderStates.add(new SliderState(e, norm));
            } else {
                sliderStates.add(null);
            }
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
        // Full dark background
        g.fill(0, 0, this.width, this.height, 0xFF0D0D0D);

        // Top bar
        g.fill(0, 0, this.width, TOP_MARGIN, 0xFF161616);
        g.fill(0, TOP_MARGIN - 1, this.width, TOP_MARGIN, 0xFF444444);
        g.text(this.font, "Full Settings", this.width / 2, 7 + (TOP_MARGIN - 18) / 2 - this.font.lineHeight / 2, 0xFFFFFF, true);

        // Bottom bar
        g.fill(0, this.height - BOT_MARGIN, this.width, this.height, 0xFF161616);
        g.fill(0, this.height - BOT_MARGIN, this.width, this.height - BOT_MARGIN + 1, 0xFF444444);

        int cx = this.width / 2;
        int visH = this.height - TOP_MARGIN - BOT_MARGIN;

        // Clip and draw rows
        for (int i = 0; i < shown.size(); i++) {
            SettingEntry e = shown.get(i);
            int y = TOP_MARGIN + i * ROW_H - scrollOffset;
            if (y + ROW_H < TOP_MARGIN || y > TOP_MARGIN + visH) continue;

            if (e.isHeader) {
                // Header row
                g.fill(0, y, this.width, y + ROW_H, 0xFF1C1C1C);
                g.fill(0, y, 3, y + ROW_H, 0xFFFFAA00);
                g.text(this.font, e.label, cx, y + (ROW_H - this.font.lineHeight) / 2, 0xFFAA00, true);
            } else if (e.isSlider) {
                // Draw slider manually
                SliderState ss = sliderStates.get(i);
                if (ss != null) {
                    int wx = cx - WIDGET_W / 2;
                    int wy = y + (ROW_H - WIDGET_H) / 2;
                    // Track
                    g.fill(wx, wy, wx + WIDGET_W, wy + WIDGET_H, 0xFF333333);
                    g.fill(wx + 1, wy + 1, wx + WIDGET_W - 1, wy + WIDGET_H - 1, 0xFF222222);
                    // Fill
                    int fillW = (int)(ss.value * (WIDGET_W - 8));
                    g.fill(wx + 4, wy + 4, wx + 4 + fillW, wy + WIDGET_H - 4, 0xFF5555AA);
                    // Handle
                    int hx = wx + (int)(ss.value * (WIDGET_W - 8));
                    g.fill(hx, wy, hx + 8, wy + WIDGET_H, 0xFFAAAAAA);
                    // Label
                    float val = (float)(e.min + ss.value * (e.max - e.min));
                    String text = e.label + ": " + e.formatValue(val);
                    g.text(this.font, text, cx, wy + (WIDGET_H - this.font.lineHeight) / 2, 0xFFFFFF, true);
                }
            } else if (!e.isToggle) {
                // Plain info row (shouldn't exist but just in case)
                g.text(this.font, e.label, cx, y + (ROW_H - this.font.lineHeight) / 2, 0xCCCCCC, true);
            }
            // Toggle buttons are rendered by super
        }

        // Scroll indicator
        int totalH = shown.size() * ROW_H;
        if (totalH > visH) {
            int trackH = visH;
            int thumbH = Math.max(20, (int)((float) visH / totalH * trackH));
            int thumbY = TOP_MARGIN + (int)((float) scrollOffset / (totalH - visH) * (trackH - thumbH));
            g.fill(this.width - 4, TOP_MARGIN, this.width, TOP_MARGIN + visH, 0xFF222222);
            g.fill(this.width - 4, thumbY, this.width, thumbY + thumbH, 0xFF888888);
        }

        super.extractRenderState(g, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Handle slider clicks
        int cx = this.width / 2;
        int visH = this.height - TOP_MARGIN - BOT_MARGIN;

        for (int i = 0; i < shown.size(); i++) {
            SettingEntry e = shown.get(i);
            if (!e.isSlider) continue;
            int y = TOP_MARGIN + i * ROW_H - scrollOffset;
            if (y + ROW_H < TOP_MARGIN || y > TOP_MARGIN + visH) continue;
            int wx = cx - WIDGET_W / 2;
            int wy = y + (ROW_H - WIDGET_H) / 2;
            if (mouseX >= wx && mouseX <= wx + WIDGET_W && mouseY >= wy && mouseY <= wy + WIDGET_H) {
                SliderState ss = sliderStates.get(i);
                if (ss != null) {
                    double newVal = Math.max(0, Math.min(1, (mouseX - wx) / WIDGET_W));
                    ss.value = newVal;
                    float realVal = (float)(e.min + newVal * (e.max - e.min));
                    e.setValue.accept(realVal);
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dx, double dy) {
        int cx = this.width / 2;
        int visH = this.height - TOP_MARGIN - BOT_MARGIN;

        for (int i = 0; i < shown.size(); i++) {
            SettingEntry e = shown.get(i);
            if (!e.isSlider) continue;
            int y = TOP_MARGIN + i * ROW_H - scrollOffset;
            if (y + ROW_H < TOP_MARGIN || y > TOP_MARGIN + visH) continue;
            int wx = cx - WIDGET_W / 2;
            int wy = y + (ROW_H - WIDGET_H) / 2;
            if (mouseY >= wy && mouseY <= wy + WIDGET_H) {
                SliderState ss = sliderStates.get(i);
                if (ss != null) {
                    double newVal = Math.max(0, Math.min(1, (mouseX - wx) / WIDGET_W));
                    ss.value = newVal;
                    float realVal = (float)(e.min + newVal * (e.max - e.min));
                    e.setValue.accept(realVal);
                    return true;
                }
            }
        }
        return super.mouseDragged(mouseX, mouseY, button, dx, dy);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int totalH = shown.size() * ROW_H;
        int visH = this.height - TOP_MARGIN - BOT_MARGIN;
        int maxScroll = Math.max(0, totalH - visH);
        scrollOffset = (int) Math.max(0, Math.min(maxScroll, scrollOffset - scrollY * SCROLL_AMOUNT));
        return true;
    }

    private void buildAllEntries() {
        allEntries.clear();

        // ==================== SOUND ====================
        allEntries.add(SettingEntry.header("SOUND"));
        allEntries.add(SettingEntry.slider("Master Volume",
                0f, 1f, true, () -> config.masterVolume, v -> config.masterVolume = v));
        allEntries.add(SettingEntry.slider("Music",
                0f, 1f, true, () -> config.musicVolume, v -> config.musicVolume = v));
        allEntries.add(SettingEntry.slider("Weather (Rain, Thunder)",
                0f, 1f, true, () -> config.weatherVolume, v -> config.weatherVolume = v));
        allEntries.add(SettingEntry.slider("Hostile Mobs (Zombies, Skeletons, etc.)",
                0f, 1f, true, () -> config.hostileVolume, v -> config.hostileVolume = v));
        allEntries.add(SettingEntry.slider("Neutral Mobs (Pigs, Cows, etc.)",
                0f, 1f, true, () -> config.neutralVolume, v -> config.neutralVolume = v));
        allEntries.add(SettingEntry.slider("Ambient / Environment",
                0f, 1f, true, () -> config.ambientVolume, v -> config.ambientVolume = v));
        allEntries.add(SettingEntry.slider("Voice / Speech",
                0f, 1f, true, () -> config.voiceVolume, v -> config.voiceVolume = v));

        // ==================== VIDEO ====================
        allEntries.add(SettingEntry.header("VIDEO"));
        allEntries.add(SettingEntry.slider("Brightness",
                0f, 1.5f, true, () -> config.brightness, v -> config.brightness = v));
        allEntries.add(SettingEntry.slider("Field of View (FOV)",
                30f, 110f, false, () -> config.fov, v -> config.fov = Math.round(v)));
        allEntries.add(SettingEntry.slider("Render Distance (chunks)",
                2f, 32f, false, () -> config.renderDistance, v -> config.renderDistance = Math.round(v)));
        allEntries.add(SettingEntry.slider("Simulation Distance (chunks)",
                5f, 32f, false, () -> config.simulationDistance, v -> config.simulationDistance = Math.round(v)));
        allEntries.add(SettingEntry.slider("Max Framerate (fps)",
                10f, 260f, false, () -> config.maxFramerate, v -> config.maxFramerate = Math.round(v)));
        allEntries.add(SettingEntry.slider("GUI Scale (0 = auto)",
                0f, 4f, false, () -> config.guiScale, v -> config.guiScale = Math.round(v)));
        allEntries.add(SettingEntry.slider("Mipmap Levels",
                0f, 4f, false, () -> config.mipmapLevels, v -> config.mipmapLevels = Math.round(v)));
        allEntries.add(SettingEntry.toggle("Fullscreen",
                () -> config.fullscreen ? 1f : 0f, v -> config.fullscreen = v >= 0.5f));
        allEntries.add(SettingEntry.toggle("VSync",
                () -> config.vsync ? 1f : 0f, v -> config.vsync = v >= 0.5f));
        allEntries.add(SettingEntry.toggle("Clouds",
                () -> config.clouds ? 1f : 0f, v -> config.clouds = v >= 0.5f));
        allEntries.add(SettingEntry.toggle("View Bobbing",
                () -> config.bobView ? 1f : 0f, v -> config.bobView = v >= 0.5f));
        allEntries.add(SettingEntry.toggle("Entity Shadows",
                () -> config.entityShadows ? 1f : 0f, v -> config.entityShadows = v >= 0.5f));
        allEntries.add(SettingEntry.toggle("Smooth Lighting",
                () -> config.smoothLighting ? 1f : 0f, v -> config.smoothLighting = v >= 0.5f));

        // ==================== MOUSE ====================
        allEntries.add(SettingEntry.header("MOUSE"));
        allEntries.add(SettingEntry.slider("Mouse Sensitivity",
                0f, 1f, true, () -> config.mouseSensitivity, v -> config.mouseSensitivity = v));
        allEntries.add(SettingEntry.slider("Mouse Wheel Sensitivity",
                0.01f, 10f, false, () -> config.mouseWheelSensitivity, v -> config.mouseWheelSensitivity = v));
        allEntries.add(SettingEntry.toggle("Invert Mouse Y-Axis",
                () -> config.invertYAxis ? 1f : 0f, v -> config.invertYAxis = v >= 0.5f));
        allEntries.add(SettingEntry.toggle("Discrete Scrolling",
                () -> config.discreteScrolling ? 1f : 0f, v -> config.discreteScrolling = v >= 0.5f));
        allEntries.add(SettingEntry.toggle("Raw Mouse Input",
                () -> config.rawMouseInput ? 1f : 0f, v -> config.rawMouseInput = v >= 0.5f));
        allEntries.add(SettingEntry.toggle("Touchscreen Mode",
                () -> config.touchscreen ? 1f : 0f, v -> config.touchscreen = v >= 0.5f));

        // ==================== CHAT ====================
        allEntries.add(SettingEntry.header("CHAT"));
        allEntries.add(SettingEntry.slider("Chat Opacity",
                0f, 1f, true, () -> config.chatOpacity, v -> config.chatOpacity = v));
        allEntries.add(SettingEntry.slider("Chat Scale",
                0.4f, 1f, true, () -> config.chatScale, v -> config.chatScale = v));
        allEntries.add(SettingEntry.slider("Chat Width",
                0f, 1f, true, () -> config.chatWidth, v -> config.chatWidth = v));
        allEntries.add(SettingEntry.slider("Chat Line Spacing",
                0f, 1f, true, () -> config.chatLineSpacing, v -> config.chatLineSpacing = v));
        allEntries.add(SettingEntry.slider("Chat Delay (seconds)",
                0f, 6f, false, () -> config.chatDelay, v -> config.chatDelay = v));
        allEntries.add(SettingEntry.toggle("Chat Visible",
                () -> config.chatVisibility ? 1f : 0f, v -> config.chatVisibility = v >= 0.5f));
        allEntries.add(SettingEntry.toggle("Chat Colors",
                () -> config.chatColors ? 1f : 0f, v -> config.chatColors = v >= 0.5f));
        allEntries.add(SettingEntry.toggle("Chat Links",
                () -> config.chatLinks ? 1f : 0f, v -> config.chatLinks = v >= 0.5f));
        allEntries.add(SettingEntry.toggle("Prompt on Chat Links",
                () -> config.chatLinksPrompt ? 1f : 0f, v -> config.chatLinksPrompt = v >= 0.5f));

        // ==================== GAMEPLAY ====================
        allEntries.add(SettingEntry.header("GAMEPLAY"));
        allEntries.add(SettingEntry.toggle("Auto Jump",
                () -> config.autoJump ? 1f : 0f, v -> config.autoJump = v >= 0.5f));
        allEntries.add(SettingEntry.toggle("Toggle Sprint",
                () -> config.toggleSprint ? 1f : 0f, v -> config.toggleSprint = v >= 0.5f));
        allEntries.add(SettingEntry.toggle("Toggle Crouch",
                () -> config.toggleCrouch ? 1f : 0f, v -> config.toggleCrouch = v >= 0.5f));
        allEntries.add(SettingEntry.toggle("Reduced Debug Info (F3)",
                () -> config.reducedDebugInfo ? 1f : 0f, v -> config.reducedDebugInfo = v >= 0.5f));

        // ==================== ACCESSIBILITY ====================
        allEntries.add(SettingEntry.header("ACCESSIBILITY"));
        allEntries.add(SettingEntry.toggle("Subtitles",
                () -> config.subtitles ? 1f : 0f, v -> config.subtitles = v >= 0.5f));
        allEntries.add(SettingEntry.toggle("Text Background",
                () -> config.textBackground ? 1f : 0f, v -> config.textBackground = v >= 0.5f));
        allEntries.add(SettingEntry.slider("Text Background Opacity",
                0f, 1f, true, () -> config.textBackgroundOp, v -> config.textBackgroundOp = v));
        allEntries.add(SettingEntry.toggle("High Contrast",
                () -> config.highContrast ? 1f : 0f, v -> config.highContrast = v >= 0.5f));
        allEntries.add(SettingEntry.toggle("Hide Lightning Flashes",
                () -> config.hideLightningFlash ? 1f : 0f, v -> config.hideLightningFlash = v >= 0.5f));
        allEntries.add(SettingEntry.toggle("Dark Mojang Screen",
                () -> config.darkMojangScreen ? 1f : 0f, v -> config.darkMojangScreen = v >= 0.5f));
        allEntries.add(SettingEntry.toggle("Monochrome Logo",
                () -> config.monochromeLogo ? 1f : 0f, v -> config.monochromeLogo = v >= 0.5f));
        allEntries.add(SettingEntry.slider("Damage Tilt Intensity",
                0f, 1f, true, () -> config.damageTilt, v -> config.damageTilt = v));
        allEntries.add(SettingEntry.slider("Glint Speed",
                0f, 1f, true, () -> config.glintSpeed, v -> config.glintSpeed = v));
        allEntries.add(SettingEntry.slider("Glint Strength",
                0f, 1f, true, () -> config.glintStrength, v -> config.glintStrength = v));
        allEntries.add(SettingEntry.slider("Panorama Scroll Speed",
                0f, 1f, true, () -> config.panoramaSpeed, v -> config.panoramaSpeed = v));

        // ==================== SKIN LAYERS ====================
        allEntries.add(SettingEntry.header("SKIN LAYERS"));
        allEntries.add(SettingEntry.toggle("Show Cape",
                () -> config.showCape ? 1f : 0f, v -> config.showCape = v >= 0.5f));
        allEntries.add(SettingEntry.toggle("Show Jacket",
                () -> config.showJacket ? 1f : 0f, v -> config.showJacket = v >= 0.5f));
        allEntries.add(SettingEntry.toggle("Show Left Sleeve",
                () -> config.showLeftSleeve ? 1f : 0f, v -> config.showLeftSleeve = v >= 0.5f));
        allEntries.add(SettingEntry.toggle("Show Right Sleeve",
                () -> config.showRightSleeve ? 1f : 0f, v -> config.showRightSleeve = v >= 0.5f));
        allEntries.add(SettingEntry.toggle("Show Left Pants",
                () -> config.showLeftPants ? 1f : 0f, v -> config.showLeftPants = v >= 0.5f));
        allEntries.add(SettingEntry.toggle("Show Right Pants",
                () -> config.showRightPants ? 1f : 0f, v -> config.showRightPants = v >= 0.5f));
        allEntries.add(SettingEntry.toggle("Show Hat",
                () -> config.showHat ? 1f : 0f, v -> config.showHat = v >= 0.5f));
    }

    private void applyAll() {
        Minecraft mc = Minecraft.getInstance();

        mc.options.getSoundSourceOptionInstance(SoundSource.MASTER).set((double) config.masterVolume);
        mc.options.getSoundSourceOptionInstance(SoundSource.MUSIC).set((double) config.musicVolume);
        mc.options.getSoundSourceOptionInstance(SoundSource.WEATHER).set((double) config.weatherVolume);
        mc.options.getSoundSourceOptionInstance(SoundSource.HOSTILE).set((double) config.hostileVolume);
        mc.options.getSoundSourceOptionInstance(SoundSource.NEUTRAL).set((double) config.neutralVolume);
        mc.options.getSoundSourceOptionInstance(SoundSource.AMBIENT).set((double) config.ambientVolume);
        mc.options.getSoundSourceOptionInstance(SoundSource.VOICE).set((double) config.voiceVolume);

        mc.options.gamma().set((double) config.brightness);
        mc.options.fov().set(config.fov);
        mc.options.renderDistance().set(config.renderDistance);
        mc.options.simulationDistance().set(config.simulationDistance);
        mc.options.framerateLimit().set(config.maxFramerate);
        mc.options.enableVsync().set(config.vsync);
        mc.options.fullscreen().set(config.fullscreen);
        mc.options.bobView().set(config.bobView);
        mc.options.entityShadows().set(config.entityShadows);

        mc.options.chatOpacity().set((double) config.chatOpacity);
        mc.options.chatScale().set((double) config.chatScale);
        mc.options.chatWidth().set((double) config.chatWidth);
        mc.options.chatVisibility().set(config.chatVisibility
                ? net.minecraft.world.entity.player.ChatVisiblity.FULL
                : net.minecraft.world.entity.player.ChatVisiblity.HIDDEN);
        mc.options.chatColors().set(config.chatColors);
        mc.options.chatLinks().set(config.chatLinks);

        mc.options.autoJump().set(config.autoJump);
        mc.options.toggleSprint().set(config.toggleSprint);
        mc.options.toggleCrouch().set(config.toggleCrouch);
        mc.options.reducedDebugInfo().set(config.reducedDebugInfo);

        mc.options.showSubtitles().set(config.subtitles);
        mc.options.backgroundForChatOnly().set(!config.textBackground);

        mc.options.mouseSensitivity().set((double) config.mouseSensitivity);
        mc.options.invertYMouse().set(config.invertYAxis);
        mc.options.discreteMouseScroll().set(config.discreteScrolling);
        mc.options.touchscreen().set(config.touchscreen);
        mc.options.rawMouseInput().set(config.rawMouseInput);

        mc.options.save();
    }

    @Override
    public void onClose() {
        MoreSettingsConfig.save();
        applyAll();
        this.minecraft.gui.setScreen(parent);
    }

    // ========== Inner classes ==========

    static class SliderState {
        final SettingEntry entry;
        double value;
        SliderState(SettingEntry e, double v) { this.entry = e; this.value = v; }
    }

    static class SettingEntry {
        String label;
        float min, max;
        boolean isSlider, isHeader, isToggle, isPercent;
        Supplier<Float> getValue;
        Consumer<Float> setValue;

        static SettingEntry header(String label) {
            SettingEntry e = new SettingEntry();
            e.label = label; e.isHeader = true; return e;
        }

        static SettingEntry slider(String label, float min, float max, boolean percent,
                                   Supplier<Float> get, Consumer<Float> set) {
            SettingEntry e = new SettingEntry();
            e.label = label; e.min = min; e.max = max;
            e.isSlider = true; e.isPercent = percent;
            e.getValue = get; e.setValue = set;
            return e;
        }

        static SettingEntry toggle(String label, Supplier<Float> get, Consumer<Float> set) {
            SettingEntry e = new SettingEntry();
            e.label = label; e.isToggle = true;
            e.getValue = get; e.setValue = set;
            return e;
        }

        String formatValue(float v) {
            if (isPercent) return Math.round(v * 100) + "%";
            if (max - min <= 10 && (v != Math.floor(v)))
                return String.format("%.2f", v);
            return String.valueOf(Math.round(v));
        }
    }
}