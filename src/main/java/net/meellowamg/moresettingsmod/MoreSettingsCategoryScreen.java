package net.meellowamg.moresettingsmod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;

import java.util.function.Consumer;

public class MoreSettingsCategoryScreen extends Screen {

    private final Screen parent;
    private final String category;
    private final MoreSettingsConfig config;

    private int scrollOffset = 0;
    private static final int SCROLL_AMOUNT = 20;
    private static final int SPACING = 26;
    private static final int W = 280;
    private static final int H = 20;
    private int contentHeight = 0;

    public MoreSettingsCategoryScreen(Screen parent, String category) {
        super(Component.literal(category + " Settings"));
        this.parent   = parent;
        this.category = category;
        this.config   = MoreSettingsConfig.get();
    }

    @Override
    protected void init() {
        this.clearWidgets();
        int cx = this.width / 2;
        int y  = 35 - scrollOffset;

        y = switch (category) {
            case MoreSettingsScreen.CAT_SOUND         -> buildSound(cx, y);
            case MoreSettingsScreen.CAT_VIDEO         -> buildVideo(cx, y);
            case MoreSettingsScreen.CAT_CHAT          -> buildChat(cx, y);
            case MoreSettingsScreen.CAT_GAMEPLAY      -> buildGameplay(cx, y);
            case MoreSettingsScreen.CAT_ACCESSIBILITY -> buildAccessibility(cx, y);
            default -> y;
        };

        contentHeight = y + scrollOffset + 40;

        this.addRenderableWidget(Button.builder(
                Component.literal("Back"),
                btn -> {
                    MoreSettingsConfig.save();
                    applyAll();
                    this.minecraft.gui.setScreen(parent);
                }
        ).bounds(cx - 75, this.height - 28, 150, 20).build());
    }

    private int buildSound(int cx, int y) {
        y = slider(cx, y, "Master Volume",      config.masterVolume,  0f, 1f, v -> { config.masterVolume  = v; applySound(); });
        y = slider(cx, y, "Music",              config.musicVolume,   0f, 1f, v -> { config.musicVolume   = v; applySound(); });
        y = slider(cx, y, "Records",            config.recordVolume,  0f, 1f, v -> { config.recordVolume  = v; applySound(); });
        y = slider(cx, y, "Weather",            config.weatherVolume, 0f, 1f, v -> { config.weatherVolume = v; applySound(); });
        y = slider(cx, y, "Blocks",             config.blockVolume,   0f, 1f, v -> { config.blockVolume   = v; applySound(); });
        y = slider(cx, y, "Hostile Mobs",       config.hostileVolume, 0f, 1f, v -> { config.hostileVolume = v; applySound(); });
        y = slider(cx, y, "Neutral Mobs",       config.neutralVolume, 0f, 1f, v -> { config.neutralVolume = v; applySound(); });
        y = slider(cx, y, "Players",            config.playerVolume,  0f, 1f, v -> { config.playerVolume  = v; applySound(); });
        y = slider(cx, y, "Ambient",            config.ambientVolume, 0f, 1f, v -> { config.ambientVolume = v; applySound(); });
        y = slider(cx, y, "Voice / Speech",     config.voiceVolume,   0f, 1f, v -> { config.voiceVolume   = v; applySound(); });
        return y;
    }

    private int buildVideo(int cx, int y) {
        y = slider(cx, y, "Brightness",         config.brightness,              0f,  1.5f, v -> { config.brightness         = v; applyVideo(); });
        y = slider(cx, y, "FOV",                (config.fov - 30) / 80f,        0f,  1f,   v -> { config.fov                = (int)(30 + v * 80); applyVideo(); });
        y = slider(cx, y, "Render Distance",    (config.renderDistance - 2) / 30f, 0f, 1f, v -> { config.renderDistance     = (int)(2 + v * 30); applyVideo(); });
        y = slider(cx, y, "Simulation Distance",(config.simulationDistance-5)/27f, 0f, 1f, v -> { config.simulationDistance = (int)(5 + v * 27); applyVideo(); });
        y = slider(cx, y, "Max Framerate",      (config.maxFramerate - 10) / 250f, 0f, 1f, v -> { config.maxFramerate       = (int)(10 + v * 250); applyVideo(); });
        y = toggle(cx, y, "Fullscreen",         config.fullscreen, v -> { config.fullscreen = v; applyVideo(); });
        y = toggle(cx, y, "VSync",              config.vsync,      v -> { config.vsync      = v; applyVideo(); });
        y = toggle(cx, y, "Clouds",             config.clouds,     v -> { config.clouds     = v; applyVideo(); });
        return y;
    }

    private int buildChat(int cx, int y) {
        y = slider(cx, y, "Chat Opacity", config.chatOpacity, 0f, 1f, v -> { config.chatOpacity = v; applyChat(); });
        y = slider(cx, y, "Chat Scale",   config.chatScale,   0.4f, 1f, v -> { config.chatScale   = v; applyChat(); });
        y = slider(cx, y, "Chat Width",   config.chatWidth,   0f, 1f, v -> { config.chatWidth   = v; applyChat(); });
        y = toggle(cx, y, "Chat Visible", config.chatVisibility, v -> { config.chatVisibility = v; applyChat(); });
        y = toggle(cx, y, "Chat Colors",  config.chatColors,    v -> { config.chatColors     = v; applyChat(); });
        y = toggle(cx, y, "Chat Links",   config.chatLinks,     v -> { config.chatLinks      = v; applyChat(); });
        return y;
    }

    private int buildGameplay(int cx, int y) {
        y = toggle(cx, y, "Auto Jump",          config.autoJump,         v -> { config.autoJump         = v; applyGameplay(); });
        y = toggle(cx, y, "Toggle Sprint",      config.toggleSprint,     v -> { config.toggleSprint     = v; applyGameplay(); });
        y = toggle(cx, y, "Toggle Crouch",      config.toggleCrouch,     v -> { config.toggleCrouch     = v; applyGameplay(); });
        y = toggle(cx, y, "Reduced Debug Info", config.reducedDebugInfo, v -> { config.reducedDebugInfo = v; applyGameplay(); });
        y = toggle(cx, y, "Held Item Tooltips", config.heldItemTooltips, v -> { config.heldItemTooltips = v; applyGameplay(); });
        y = toggle(cx, y, "Advanced Tooltips",  config.advancedTooltips, v -> { config.advancedTooltips = v; applyGameplay(); });
        return y;
    }

    private int buildAccessibility(int cx, int y) {
        y = toggle(cx, y, "Subtitles",            config.subtitles,      v -> { config.subtitles      = v; applyAccessibility(); });
        y = toggle(cx, y, "Text Background",      config.textBackground, v -> { config.textBackground = v; applyAccessibility(); });
        y = slider(cx, y, "Text Background Opacity", config.textBackgroundOp, 0f, 1f, v -> { config.textBackgroundOp = v; applyAccessibility(); });
        return y;
    }

    private int slider(int cx, int y, String label, float current,
                       float min, float max, Consumer<Float> onChange) {
        double norm = Math.max(0, Math.min(1, (current - min) / (max - min)));
        this.addRenderableWidget(new AbstractSliderButton(cx - W / 2, y, W, H,
                Component.literal(label + ": " + fmt(current, min, max)),
                norm) {
            @Override protected void updateMessage() {
                float v = (float)(min + this.value * (max - min));
                setMessage(Component.literal(label + ": " + fmt(v, min, max)));
            }
            @Override protected void applyValue() {
                float v = (float)(min + this.value * (max - min));
                onChange.accept(v);
            }
        });
        return y + SPACING;
    }

    private int toggle(int cx, int y, String label, boolean current, Consumer<Boolean> onChange) {
        Button[] btn = new Button[1];
        btn[0] = Button.builder(
                Component.literal(label + ": " + (current ? "ON" : "OFF")),
                b -> {
                    boolean newVal = b.getMessage().getString().endsWith("OFF");
                    b.setMessage(Component.literal(label + ": " + (newVal ? "ON" : "OFF")));
                    onChange.accept(newVal);
                }
        ).bounds(cx - W / 2, y, W, H).build();
        this.addRenderableWidget(btn[0]);
        return y + SPACING;
    }

    private String fmt(float v, float min, float max) {
        if (min == 0f && max == 1f) return Math.round(v * 100) + "%";
        if (min == 0f && max == 1.5f) return Math.round(v * 100) + "%";
        return String.valueOf(Math.round(v));
    }

    private void applySound() {
        Minecraft mc = Minecraft.getInstance();
        mc.options.getSoundSourceOptionInstance(SoundSource.MASTER).set((double) config.masterVolume);
        mc.options.getSoundSourceOptionInstance(SoundSource.MUSIC).set((double) config.musicVolume);
        mc.options.getSoundSourceOptionInstance(SoundSource.RECORD).set((double) config.recordVolume);
        mc.options.getSoundSourceOptionInstance(SoundSource.WEATHER).set((double) config.weatherVolume);
        mc.options.getSoundSourceOptionInstance(SoundSource.BLOCK).set((double) config.blockVolume);
        mc.options.getSoundSourceOptionInstance(SoundSource.HOSTILE).set((double) config.hostileVolume);
        mc.options.getSoundSourceOptionInstance(SoundSource.NEUTRAL).set((double) config.neutralVolume);
        mc.options.getSoundSourceOptionInstance(SoundSource.PLAYER).set((double) config.playerVolume);
        mc.options.getSoundSourceOptionInstance(SoundSource.AMBIENT).set((double) config.ambientVolume);
        mc.options.getSoundSourceOptionInstance(SoundSource.VOICE).set((double) config.voiceVolume);
        mc.soundManager.reload();
    }

    private void applyVideo() {
        Minecraft mc = Minecraft.getInstance();
        mc.options.gamma().set((double) config.brightness);
        mc.options.fov().set(config.fov);
        mc.options.renderDistance().set(config.renderDistance);
        mc.options.simulationDistance().set(config.simulationDistance);
        mc.options.framerateLimit().set(config.maxFramerate);
        mc.options.enableVsync().set(config.vsync);
        mc.options.fullscreen().set(config.fullscreen);
        mc.options.save();
    }

    private void applyChat() {
        Minecraft mc = Minecraft.getInstance();
        mc.options.chatOpacity().set((double) config.chatOpacity);
        mc.options.chatScale().set((double) config.chatScale);
        mc.options.chatWidth().set((double) config.chatWidth);
        mc.options.chatVisibility().set(config.chatVisibility
                ? net.minecraft.world.entity.player.ChatVisiblity.FULL
                : net.minecraft.world.entity.player.ChatVisiblity.HIDDEN);
        mc.options.chatColors().set(config.chatColors);
        mc.options.chatLinks().set(config.chatLinks);
        mc.options.save();
    }

    private void applyGameplay() {
        Minecraft mc = Minecraft.getInstance();
        mc.options.autoJump().set(config.autoJump);
        mc.options.toggleSprint().set(config.toggleSprint);
        mc.options.toggleCrouch().set(config.toggleCrouch);
        mc.options.reducedDebugInfo().set(config.reducedDebugInfo);
        mc.options.heldItemTooltips().set(config.heldItemTooltips);
        mc.options.advancedItemTooltips().set(config.advancedTooltips);
        mc.options.save();
    }

    private void applyAccessibility() {
        Minecraft mc = Minecraft.getInstance();
        mc.options.showSubtitles().set(config.subtitles);
        mc.options.backgroundForChatOnly().set(!config.textBackground);
        mc.options.save();
    }

    private void applyAll() {
        applySound();
        applyVideo();
        applyChat();
        applyGameplay();
        applyAccessibility();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int maxScroll = Math.max(0, contentHeight - this.height);
        scrollOffset  = (int) Math.max(0, Math.min(maxScroll, scrollOffset - scrollY * SCROLL_AMOUNT));
        this.init();
        return true;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        graphics.fill(0, 0, this.width, this.height, 0xCC000000);
        graphics.fill(0, 0, this.width, 24, 0xFF111111);
        graphics.text(this.font, category + " Settings", this.width / 2, 7, 0xFFFFFF, true);
        super.extractRenderState(graphics, mouseX, mouseY, delta);
    }

    @Override
    public void onClose() {
        MoreSettingsConfig.save();
        applyAll();
        this.minecraft.gui.setScreen(parent);
    }
}