package net.meellowamg.moresettingsmod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
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

    // Search
    private EditBox searchBox;
    private String searchQuery = "";

    // Scroll
    private int scrollOffset = 0;
    private static final int SCROLL_AMOUNT = 20;
    private static final int ITEM_HEIGHT = 24;
    private static final int ITEM_W = 300;
    private static final int ITEM_H = 20;

    // All setting entries
    private final List<SettingEntry> allEntries = new ArrayList<>();
    private final List<SettingEntry> filteredEntries = new ArrayList<>();

    public MoreSettingsScreen(Screen parent) {
        super(Component.literal("Full Settings"));
        this.parent = parent;
        this.config = MoreSettingsConfig.get();
    }

    @Override
    protected void init() {
        this.clearWidgets();

        // Search box
        searchBox = new EditBox(this.font, this.width / 2 - 150, 8, 300, 18,
                Component.literal("Search settings..."));
        searchBox.setMaxLength(64);
        searchBox.setHint(Component.literal("Search settings..."));
        searchBox.setResponder(val -> {
            searchQuery = val.toLowerCase();
            rebuildFiltered();
            scrollOffset = 0;
            this.init();
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
        ).bounds(this.width / 2 - 50, this.height - 26, 100, 20).build());

        // Build entries if not built yet
        if (allEntries.isEmpty()) buildEntries();
        rebuildFiltered();

        // Build visible widgets
        int startY = 34;
        int cx = this.width / 2;
        int visibleHeight = this.height - 34 - 30;

        for (int i = 0; i < filteredEntries.size(); i++) {
            int y = startY + i * ITEM_HEIGHT - scrollOffset;
            if (y + ITEM_HEIGHT < startY || y > startY + visibleHeight) continue;

            SettingEntry entry = filteredEntries.get(i);
            if (entry.isSlider) {
                addSliderWidget(cx, y, entry);
            } else {
                addToggleWidget(cx, y, entry);
            }
        }
    }

    private void addSliderWidget(int cx, int y, SettingEntry entry) {
        double norm = Math.max(0, Math.min(1,
                (entry.getValue.get() - entry.min) / (entry.max - entry.min)));

        this.addRenderableWidget(new AbstractSliderButton(
                cx - ITEM_W / 2, y, ITEM_W, ITEM_H,
                Component.literal(entry.label + ": " + entry.formatValue(entry.getValue.get())),
                norm) {
            @Override
            protected void updateMessage() {
                double v = entry.min + this.value * (entry.max - entry.min);
                setMessage(Component.literal(entry.label + ": " + entry.formatValue((float) v)));
            }
            @Override
            protected void applyValue() {
                double v = entry.min + this.value * (entry.max - entry.min);
                entry.setValue.accept((float) v);
            }
        });
    }

    private void addToggleWidget(int cx, int y, SettingEntry entry) {
        boolean cur = entry.getValue.get() >= 0.5f;
        Button[] btn = new Button[1];
        btn[0] = Button.builder(
                Component.literal(entry.label + ": " + (cur ? "ON" : "OFF")),
                b -> {
                    boolean newVal = b.getMessage().getString().endsWith("OFF");
                    b.setMessage(Component.literal(entry.label + ": " + (newVal ? "ON" : "OFF")));
                    entry.setValue.accept(newVal ? 1f : 0f);
                }
        ).bounds(cx - ITEM_W / 2, y, ITEM_W, ITEM_H).build();
        this.addRenderableWidget(btn[0]);
    }

    private void buildEntries() {
        allEntries.clear();

        // ===== SOUND =====
        allEntries.add(SettingEntry.header("— Sound —"));
        allEntries.add(SettingEntry.slider("Master Volume",       0f, 1f, true,  () -> config.masterVolume,  v -> config.masterVolume  = v));
        allEntries.add(SettingEntry.slider("Music Volume",        0f, 1f, true,  () -> config.musicVolume,   v -> config.musicVolume   = v));
        allEntries.add(SettingEntry.slider("Weather Volume",      0f, 1f, true,  () -> config.weatherVolume, v -> config.weatherVolume = v));
        allEntries.add(SettingEntry.slider("Hostile Mob Volume",  0f, 1f, true,  () -> config.hostileVolume, v -> config.hostileVolume = v));
        allEntries.add(SettingEntry.slider("Neutral Mob Volume",  0f, 1f, true,  () -> config.neutralVolume, v -> config.neutralVolume = v));
        allEntries.add(SettingEntry.slider("Ambient Volume",      0f, 1f, true,  () -> config.ambientVolume, v -> config.ambientVolume = v));
        allEntries.add(SettingEntry.slider("Voice / Speech",      0f, 1f, true,  () -> config.voiceVolume,   v -> config.voiceVolume   = v));

        // ===== VIDEO =====
        allEntries.add(SettingEntry.header("— Video —"));
        allEntries.add(SettingEntry.slider("Brightness",          0f,  1.5f, true,  () -> config.brightness,         v -> config.brightness         = v));
        allEntries.add(SettingEntry.slider("FOV",                 30f, 110f, false, () -> (float) config.fov,         v -> config.fov               = Math.round(v)));
        allEntries.add(SettingEntry.slider("Render Distance",     2f,  32f,  false, () -> (float) config.renderDistance,    v -> config.renderDistance     = Math.round(v)));
        allEntries.add(SettingEntry.slider("Simulation Distance", 5f,  32f,  false, () -> (float) config.simulationDistance, v -> config.simulationDistance = Math.round(v)));
        allEntries.add(SettingEntry.slider("Max Framerate",       10f, 260f, false, () -> (float) config.maxFramerate, v -> config.maxFramerate      = Math.round(v)));
        allEntries.add(SettingEntry.toggle("Fullscreen",   () -> config.fullscreen  ? 1f : 0f, v -> config.fullscreen  = v >= 0.5f));
        allEntries.add(SettingEntry.toggle("VSync",        () -> config.vsync       ? 1f : 0f, v -> config.vsync       = v >= 0.5f));
        allEntries.add(SettingEntry.toggle("Clouds",       () -> config.clouds      ? 1f : 0f, v -> config.clouds      = v >= 0.5f));

        // ===== CHAT =====
        allEntries.add(SettingEntry.header("— Chat —"));
        allEntries.add(SettingEntry.slider("Chat Opacity",  0f, 1f, true,  () -> config.chatOpacity, v -> config.chatOpacity = v));
        allEntries.add(SettingEntry.slider("Chat Scale",    0.4f, 1f, true, () -> config.chatScale,   v -> config.chatScale   = v));
        allEntries.add(SettingEntry.slider("Chat Width",    0f, 1f, true,  () -> config.chatWidth,   v -> config.chatWidth   = v));
        allEntries.add(SettingEntry.toggle("Chat Visible",  () -> config.chatVisibility ? 1f : 0f, v -> config.chatVisibility = v >= 0.5f));
        allEntries.add(SettingEntry.toggle("Chat Colors",   () -> config.chatColors     ? 1f : 0f, v -> config.chatColors     = v >= 0.5f));
        allEntries.add(SettingEntry.toggle("Chat Links",    () -> config.chatLinks      ? 1f : 0f, v -> config.chatLinks      = v >= 0.5f));

        // ===== GAMEPLAY =====
        allEntries.add(SettingEntry.header("— Gameplay —"));
        allEntries.add(SettingEntry.toggle("Auto Jump",          () -> config.autoJump         ? 1f : 0f, v -> config.autoJump         = v >= 0.5f));
        allEntries.add(SettingEntry.toggle("Toggle Sprint",      () -> config.toggleSprint     ? 1f : 0f, v -> config.toggleSprint     = v >= 0.5f));
        allEntries.add(SettingEntry.toggle("Toggle Crouch",      () -> config.toggleCrouch     ? 1f : 0f, v -> config.toggleCrouch     = v >= 0.5f));
        allEntries.add(SettingEntry.toggle("Reduced Debug Info", () -> config.reducedDebugInfo ? 1f : 0f, v -> config.reducedDebugInfo = v >= 0.5f));

        // ===== ACCESSIBILITY =====
        allEntries.add(SettingEntry.header("— Accessibility —"));
        allEntries.add(SettingEntry.toggle("Subtitles",           () -> config.subtitles      ? 1f : 0f, v -> config.subtitles      = v >= 0.5f));
        allEntries.add(SettingEntry.toggle("Text Background",     () -> config.textBackground ? 1f : 0f, v -> config.textBackground = v >= 0.5f));
        allEntries.add(SettingEntry.slider("Text Background Opacity", 0f, 1f, true, () -> config.textBackgroundOp, v -> config.textBackgroundOp = v));
    }

    private void rebuildFiltered() {
        filteredEntries.clear();
        if (searchQuery.isEmpty()) {
            filteredEntries.addAll(allEntries);
        } else {
            for (SettingEntry e : allEntries) {
                if (e.isHeader) continue; // hide headers during search
                if (e.label.toLowerCase().contains(searchQuery)) {
                    filteredEntries.add(e);
                }
            }
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int totalHeight = filteredEntries.size() * ITEM_HEIGHT;
        int visibleHeight = this.height - 34 - 30;
        int maxScroll = Math.max(0, totalHeight - visibleHeight);
        scrollOffset = (int) Math.max(0, Math.min(maxScroll, scrollOffset - scrollY * SCROLL_AMOUNT));
        this.init();
        return true;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        // Background
        graphics.fill(0, 0, this.width, this.height, 0xCC101010);
        graphics.fill(0, 0, this.width, 30, 0xFF111111);
        graphics.text(this.font, "Full Settings", this.width / 2, 30 - this.font.lineHeight - 2, 0xFFFFFF, true);

        // Section headers drawn as text, not widgets
        int startY = 34;
        int cx = this.width / 2;
        int visibleHeight = this.height - 34 - 30;

        for (int i = 0; i < filteredEntries.size(); i++) {
            int y = startY + i * ITEM_HEIGHT - scrollOffset;
            if (y + ITEM_HEIGHT < startY || y > startY + visibleHeight) continue;
            SettingEntry entry = filteredEntries.get(i);
            if (entry.isHeader) {
                graphics.fill(0, y, this.width, y + ITEM_HEIGHT, 0xFF1A1A1A);
                graphics.text(this.font, entry.label, cx, y + 6, 0xFFAA00, true);
            }
        }

        super.extractRenderState(graphics, mouseX, mouseY, delta);
    }

    private void applyAll() {
        Minecraft mc = Minecraft.getInstance();

        // Sound
        mc.options.getSoundSourceOptionInstance(SoundSource.MASTER).set((double) config.masterVolume);
        mc.options.getSoundSourceOptionInstance(SoundSource.MUSIC).set((double) config.musicVolume);
        mc.options.getSoundSourceOptionInstance(SoundSource.WEATHER).set((double) config.weatherVolume);
        mc.options.getSoundSourceOptionInstance(SoundSource.HOSTILE).set((double) config.hostileVolume);
        mc.options.getSoundSourceOptionInstance(SoundSource.NEUTRAL).set((double) config.neutralVolume);
        mc.options.getSoundSourceOptionInstance(SoundSource.AMBIENT).set((double) config.ambientVolume);
        mc.options.getSoundSourceOptionInstance(SoundSource.VOICE).set((double) config.voiceVolume);

        // Video
        mc.options.gamma().set((double) config.brightness);
        mc.options.fov().set(config.fov);
        mc.options.renderDistance().set(config.renderDistance);
        mc.options.simulationDistance().set(config.simulationDistance);
        mc.options.framerateLimit().set(config.maxFramerate);
        mc.options.enableVsync().set(config.vsync);
        mc.options.fullscreen().set(config.fullscreen);

        // Chat
        mc.options.chatOpacity().set((double) config.chatOpacity);
        mc.options.chatScale().set((double) config.chatScale);
        mc.options.chatWidth().set((double) config.chatWidth);
        mc.options.chatVisibility().set(config.chatVisibility
                ? net.minecraft.world.entity.player.ChatVisiblity.FULL
                : net.minecraft.world.entity.player.ChatVisiblity.HIDDEN);
        mc.options.chatColors().set(config.chatColors);
        mc.options.chatLinks().set(config.chatLinks);

        // Gameplay
        mc.options.autoJump().set(config.autoJump);
        mc.options.toggleSprint().set(config.toggleSprint);
        mc.options.toggleCrouch().set(config.toggleCrouch);
        mc.options.reducedDebugInfo().set(config.reducedDebugInfo);

        // Accessibility
        mc.options.showSubtitles().set(config.subtitles);
        mc.options.backgroundForChatOnly().set(!config.textBackground);

        mc.options.save();
    }

    @Override
    public void onClose() {
        MoreSettingsConfig.save();
        applyAll();
        this.minecraft.gui.setScreen(parent);
    }

    // ---- SettingEntry ----
    static class SettingEntry {
        String label;
        float min, max;
        boolean isSlider, isHeader, isPercent;
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
            e.label = label; e.isSlider = false;
            e.getValue = get; e.setValue = set;
            return e;
        }

        String formatValue(float v) {
            if (isPercent) return Math.round(v * 100) + "%";
            return String.valueOf(Math.round(v));
        }
    }
}