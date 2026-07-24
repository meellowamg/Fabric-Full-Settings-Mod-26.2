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
    private static final int ROW_H = 24;
    private static final int WIDGET_W = 320;
    private static final int WIDGET_H = 18;
    private static final int TOP_MARGIN = 30;
    private static final int BOT_MARGIN = 28;

    private final List<SettingEntry> allEntries   = new ArrayList<>();
    private final List<SettingEntry> shown        = new ArrayList<>();
    private final List<SliderState>  sliderStates = new ArrayList<>();

    private int draggingSliderIndex = -1;

    public MoreSettingsScreen(Screen parent) {
        super(Component.literal("Full Settings"));
        this.parent = parent;
        this.config = MoreSettingsConfig.get();
    }

    @Override
    protected void init() {
        this.clearWidgets();

        if (allEntries.isEmpty()) buildAllEntries();

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

        this.addRenderableWidget(Button.builder(
                Component.literal("Back"),
                btn -> {
                    MoreSettingsConfig.save();
                    applyAll();
                    this.minecraft.gui.setScreen(parent);
                }
        ).bounds(this.width / 2 - 50, this.height - 24, 100, 20).build());

        rebuildShown();
        addToggleWidgets();
    }

    private void addToggleWidgets() {
        int cx = this.width / 2;
        int visH = this.height - TOP_MARGIN - BOT_MARGIN;
        for (int i = 0; i < shown.size(); i++) {
            SettingEntry e = shown.get(i);
            if (!e.isToggle) continue;
            int y = TOP_MARGIN + i * ROW_H - scrollOffset;
            if (y + ROW_H < TOP_MARGIN || y > TOP_MARGIN + visH) continue;
            int wy = y + (ROW_H - WIDGET_H) / 2;
            boolean cur = e.getValue.get() >= 0.5f;
            final SettingEntry fe = e;
            Button[] bArr = new Button[1];
            bArr[0] = Button.builder(
                    Component.literal(e.label + ": " + (cur ? "ON" : "OFF")),
                    b -> {
                        boolean nv = b.getMessage().getString().endsWith("OFF");
                        b.setMessage(Component.literal(fe.label + ": " + (nv ? "ON" : "OFF")));
                        fe.setValue.accept(nv ? 1f : 0f);
                    }
            ).bounds(cx - WIDGET_W / 2, wy, WIDGET_W, WIDGET_H).build();
            this.addRenderableWidget(bArr[0]);
        }
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
        sliderStates.clear();
        for (SettingEntry e : shown) {
            if (e.isSlider) {
                double norm = Math.max(0, Math.min(1,
                        (e.getValue.get() - e.min) / (e.max - e.min)));
                sliderStates.add(new SliderState(e, norm));
            } else {
                sliderStates.add(null);
            }
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
        g.fill(0, 0, this.width, this.height, 0xFF0D0D0D);
        g.fill(0, 0, this.width, TOP_MARGIN, 0xFF161616);
        g.fill(0, TOP_MARGIN - 1, this.width, TOP_MARGIN, 0xFF444444);
        g.text(this.font, "Full Settings", this.width / 2,
                (TOP_MARGIN - this.font.lineHeight) / 2, 0xFFFFFF, true);
        g.fill(0, this.height - BOT_MARGIN, this.width, this.height, 0xFF161616);
        g.fill(0, this.height - BOT_MARGIN, this.width, this.height - BOT_MARGIN + 1, 0xFF444444);

        int cx = this.width / 2;
        int visH = this.height - TOP_MARGIN - BOT_MARGIN;

        for (int i = 0; i < shown.size(); i++) {
            SettingEntry e = shown.get(i);
            int y = TOP_MARGIN + i * ROW_H - scrollOffset;
            if (y + ROW_H < TOP_MARGIN || y > TOP_MARGIN + visH) continue;

            if (e.isHeader) {
                g.fill(0, y, this.width, y + ROW_H, 0xFF1C1C1C);
                g.fill(0, y, 3, y + ROW_H, 0xFFFFAA00);
                g.text(this.font, e.label, cx,
                        y + (ROW_H - this.font.lineHeight) / 2, 0xFFAA00, true);
            } else if (e.isSlider) {
                SliderState ss = sliderStates.get(i);
                if (ss != null) {
                    int wx = cx - WIDGET_W / 2;
                    int wy = y + (ROW_H - WIDGET_H) / 2;
                    g.fill(wx, wy, wx + WIDGET_W, wy + WIDGET_H, 0xFF555555);
                    g.fill(wx + 1, wy + 1, wx + WIDGET_W - 1, wy + WIDGET_H - 1, 0xFF222222);
                    int fillW = (int)(ss.value * (WIDGET_W - 8));
                    if (fillW > 0)
                        g.fill(wx + 4, wy + 3, wx + 4 + fillW, wy + WIDGET_H - 3, 0xFF5577CC);
                    int hx = wx + (int)(ss.value * (WIDGET_W - 8));
                    g.fill(hx, wy, hx + 8, wy + WIDGET_H, 0xFFBBBBBB);
                    float val = (float)(e.min + ss.value * (e.max - e.min));
                    g.text(this.font, e.label + ": " + e.formatValue(val),
                            cx, wy + (WIDGET_H - this.font.lineHeight) / 2, 0xFFFFFF, true);
                }
            }
        }

        // Scrollbar
        int totalH = shown.size() * ROW_H;
        if (totalH > visH) {
            int thumbH = Math.max(20, (int)((float) visH / totalH * visH));
            int maxScroll = totalH - visH;
            int thumbY = TOP_MARGIN + (maxScroll > 0
                    ? (int)((float) scrollOffset / maxScroll * (visH - thumbH)) : 0);
            g.fill(this.width - 4, TOP_MARGIN, this.width, TOP_MARGIN + visH, 0xFF222222);
            g.fill(this.width - 4, thumbY, this.width, thumbY + thumbH, 0xFF888888);
        }

        super.extractRenderState(g, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double sx, double sy) {
        if (mouseY < TOP_MARGIN) return super.mouseScrolled(mouseX, mouseY, sx, sy);
        int totalH = shown.size() * ROW_H;
        int visH = this.height - TOP_MARGIN - BOT_MARGIN;
        int maxScroll = Math.max(0, totalH - visH);
        scrollOffset = (int) Math.max(0, Math.min(maxScroll, scrollOffset - sy * SCROLL_AMOUNT));
        this.clearWidgets();
        this.addRenderableWidget(searchBox);
        this.addRenderableWidget(Button.builder(
                Component.literal("Back"),
                btn -> {
                    MoreSettingsConfig.save();
                    applyAll();
                    this.minecraft.gui.setScreen(parent);
                }
        ).bounds(this.width / 2 - 50, this.height - 24, 100, 20).build());
        addToggleWidgets();
        return true;
    }

    private int getSliderAt(double mx, double my) {
        int cx = this.width / 2;
        int visH = this.height - TOP_MARGIN - BOT_MARGIN;
        for (int i = 0; i < shown.size(); i++) {
            if (!shown.get(i).isSlider) continue;
            int y = TOP_MARGIN + i * ROW_H - scrollOffset;
            if (y + ROW_H < TOP_MARGIN || y > TOP_MARGIN + visH) continue;
            int wx = cx - WIDGET_W / 2;
            int wy = y + (ROW_H - WIDGET_H) / 2;
            if (mx >= wx && mx <= wx + WIDGET_W && my >= wy && my <= wy + WIDGET_H) return i;
        }
        return -1;
    }

    private void moveSlider(int idx, double mx) {
        int cx = this.width / 2;
        int wx = cx - WIDGET_W / 2;
        SliderState ss = sliderStates.get(idx);
        SettingEntry e = shown.get(idx);
        if (ss == null) return;
        ss.value = Math.max(0, Math.min(1, (mx - wx) / WIDGET_W));
        e.setValue.accept((float)(e.min + ss.value * (e.max - e.min)));
    }

    // Override without @Override to avoid signature mismatch in 26.2
    public boolean mouseClicked(double mx, double my, int btn) {
        int idx = getSliderAt(mx, my);
        if (idx >= 0) { draggingSliderIndex = idx; moveSlider(idx, mx); return true; }
        draggingSliderIndex = -1;
        // forward to widgets manually
        for (var w : this.children()) {
            if (w.isMouseOver(mx, my)) {
                this.setFocused(w);
            }
        }
        return super.mouseScrolled(mx, my, 0, 0) || false;
    }

    public boolean mouseReleased(double mx, double my, int btn) {
        draggingSliderIndex = -1;
        return false;
    }

    public boolean mouseDragged(double mx, double my, int btn, double dx, double dy) {
        if (draggingSliderIndex >= 0) { moveSlider(draggingSliderIndex, mx); return true; }
        return false;
    }

    private void buildAllEntries() {
        allEntries.clear();

        // ==================== SOUND ====================
        allEntries.add(SettingEntry.header("SOUND — Master"));
        allEntries.add(SettingEntry.slider("Master Volume (all sounds)",
                0f, 1f, true, () -> config.masterVolume, v -> config.masterVolume = v));

        allEntries.add(SettingEntry.header("SOUND — Music"));
        allEntries.add(SettingEntry.slider("Music (background music in game & menus)",
                0f, 1f, true, () -> config.musicVolume, v -> config.musicVolume = v));
        allEntries.add(SettingEntry.slider("Music — Creative Mode",
                0f, 1f, true, () -> config.musicCreative, v -> config.musicCreative = v));
        allEntries.add(SettingEntry.slider("Music — End Dimension",
                0f, 1f, true, () -> config.musicEnd, v -> config.musicEnd = v));
        allEntries.add(SettingEntry.slider("Music — Nether / Dragon Fight",
                0f, 1f, true, () -> config.musicNether, v -> config.musicNether = v));
        allEntries.add(SettingEntry.slider("Music — Overworld",
                0f, 1f, true, () -> config.musicOverworld, v -> config.musicOverworld = v));
        allEntries.add(SettingEntry.slider("Music — Underwater",
                0f, 1f, true, () -> config.musicUnderwater, v -> config.musicUnderwater = v));
        allEntries.add(SettingEntry.slider("Music — Menu / Title Screen",
                0f, 1f, true, () -> config.musicMenu, v -> config.musicMenu = v));

        allEntries.add(SettingEntry.header("SOUND — Weather"));
        allEntries.add(SettingEntry.slider("Weather (rain, thunder, storm)",
                0f, 1f, true, () -> config.weatherVolume, v -> config.weatherVolume = v));
        allEntries.add(SettingEntry.slider("Rain (ambient rain sound)",
                0f, 1f, true, () -> config.soundRain, v -> config.soundRain = v));
        allEntries.add(SettingEntry.slider("Thunder (lightning strike boom)",
                0f, 1f, true, () -> config.soundThunder, v -> config.soundThunder = v));

        allEntries.add(SettingEntry.header("SOUND — Hostile Mobs"));
        allEntries.add(SettingEntry.slider("Hostile Mobs (category volume)",
                0f, 1f, true, () -> config.hostileVolume, v -> config.hostileVolume = v));
        allEntries.add(SettingEntry.slider("Zombie — Ambient / Groan",
                0f, 1f, true, () -> config.soundZombieAmbient, v -> config.soundZombieAmbient = v));
        allEntries.add(SettingEntry.slider("Zombie — Hurt",
                0f, 1f, true, () -> config.soundZombieHurt, v -> config.soundZombieHurt = v));
        allEntries.add(SettingEntry.slider("Zombie — Death",
                0f, 1f, true, () -> config.soundZombieDeath, v -> config.soundZombieDeath = v));
        allEntries.add(SettingEntry.slider("Zombie — Step",
                0f, 1f, true, () -> config.soundZombieStep, v -> config.soundZombieStep = v));
        allEntries.add(SettingEntry.slider("Skeleton — Ambient",
                0f, 1f, true, () -> config.soundSkeletonAmbient, v -> config.soundSkeletonAmbient = v));
        allEntries.add(SettingEntry.slider("Skeleton — Hurt",
                0f, 1f, true, () -> config.soundSkeletonHurt, v -> config.soundSkeletonHurt = v));
        allEntries.add(SettingEntry.slider("Skeleton — Death",
                0f, 1f, true, () -> config.soundSkeletonDeath, v -> config.soundSkeletonDeath = v));
        allEntries.add(SettingEntry.slider("Skeleton — Step",
                0f, 1f, true, () -> config.soundSkeletonStep, v -> config.soundSkeletonStep = v));
        allEntries.add(SettingEntry.slider("Creeper — Fuse (hissing)",
                0f, 1f, true, () -> config.soundCreeperPrimed, v -> config.soundCreeperPrimed = v));
        allEntries.add(SettingEntry.slider("Creeper — Hurt",
                0f, 1f, true, () -> config.soundCreeperHurt, v -> config.soundCreeperHurt = v));
        allEntries.add(SettingEntry.slider("Creeper — Death",
                0f, 1f, true, () -> config.soundCreeperDeath, v -> config.soundCreeperDeath = v));
        allEntries.add(SettingEntry.slider("Spider — Ambient",
                0f, 1f, true, () -> config.soundSpiderAmbient, v -> config.soundSpiderAmbient = v));
        allEntries.add(SettingEntry.slider("Spider — Hurt",
                0f, 1f, true, () -> config.soundSpiderHurt, v -> config.soundSpiderHurt = v));
        allEntries.add(SettingEntry.slider("Spider — Death",
                0f, 1f, true, () -> config.soundSpiderDeath, v -> config.soundSpiderDeath = v));
        allEntries.add(SettingEntry.slider("Spider — Step",
                0f, 1f, true, () -> config.soundSpiderStep, v -> config.soundSpiderStep = v));
        allEntries.add(SettingEntry.slider("Enderman — Ambient",
                0f, 1f, true, () -> config.soundEndermanAmbient, v -> config.soundEndermanAmbient = v));
        allEntries.add(SettingEntry.slider("Enderman — Scream",
                0f, 1f, true, () -> config.soundEndermanScream, v -> config.soundEndermanScream = v));
        allEntries.add(SettingEntry.slider("Enderman — Hurt",
                0f, 1f, true, () -> config.soundEndermanHurt, v -> config.soundEndermanHurt = v));
        allEntries.add(SettingEntry.slider("Enderman — Death",
                0f, 1f, true, () -> config.soundEndermanDeath, v -> config.soundEndermanDeath = v));
        allEntries.add(SettingEntry.slider("Enderman — Teleport",
                0f, 1f, true, () -> config.soundEndermanTeleport, v -> config.soundEndermanTeleport = v));
        allEntries.add(SettingEntry.slider("Blaze — Ambient (breathing)",
                0f, 1f, true, () -> config.soundBlazeAmbient, v -> config.soundBlazeAmbient = v));
        allEntries.add(SettingEntry.slider("Blaze — Shoot",
                0f, 1f, true, () -> config.soundBlazeShoot, v -> config.soundBlazeShoot = v));
        allEntries.add(SettingEntry.slider("Blaze — Hurt",
                0f, 1f, true, () -> config.soundBlazeHurt, v -> config.soundBlazeHurt = v));
        allEntries.add(SettingEntry.slider("Blaze — Death",
                0f, 1f, true, () -> config.soundBlazeDeath, v -> config.soundBlazeDeath = v));
        allEntries.add(SettingEntry.slider("Ghast — Ambient (crying)",
                0f, 1f, true, () -> config.soundGhastAmbient, v -> config.soundGhastAmbient = v));
        allEntries.add(SettingEntry.slider("Ghast — Scream",
                0f, 1f, true, () -> config.soundGhastScream, v -> config.soundGhastScream = v));
        allEntries.add(SettingEntry.slider("Ghast — Shoot",
                0f, 1f, true, () -> config.soundGhastShoot, v -> config.soundGhastShoot = v));
        allEntries.add(SettingEntry.slider("Ghast — Warn",
                0f, 1f, true, () -> config.soundGhastWarn, v -> config.soundGhastWarn = v));
        allEntries.add(SettingEntry.slider("Ghast — Hurt",
                0f, 1f, true, () -> config.soundGhastHurt, v -> config.soundGhastHurt = v));
        allEntries.add(SettingEntry.slider("Ghast — Death",
                0f, 1f, true, () -> config.soundGhastDeath, v -> config.soundGhastDeath = v));
        allEntries.add(SettingEntry.slider("Slime — Attack",
                0f, 1f, true, () -> config.soundSlimeAttack, v -> config.soundSlimeAttack = v));
        allEntries.add(SettingEntry.slider("Slime — Jump",
                0f, 1f, true, () -> config.soundSlimeJump, v -> config.soundSlimeJump = v));
        allEntries.add(SettingEntry.slider("Slime — Squish",
                0f, 1f, true, () -> config.soundSlimeSquish, v -> config.soundSlimeSquish = v));
        allEntries.add(SettingEntry.slider("Slime — Death",
                0f, 1f, true, () -> config.soundSlimeDeath, v -> config.soundSlimeDeath = v));
        allEntries.add(SettingEntry.slider("Wither — Spawn",
                0f, 1f, true, () -> config.soundWitherSpawn, v -> config.soundWitherSpawn = v));
        allEntries.add(SettingEntry.slider("Wither — Ambient",
                0f, 1f, true, () -> config.soundWitherAmbient, v -> config.soundWitherAmbient = v));
        allEntries.add(SettingEntry.slider("Wither — Shoot",
                0f, 1f, true, () -> config.soundWitherShoot, v -> config.soundWitherShoot = v));
        allEntries.add(SettingEntry.slider("Wither — Hurt",
                0f, 1f, true, () -> config.soundWitherHurt, v -> config.soundWitherHurt = v));
        allEntries.add(SettingEntry.slider("Wither — Death",
                0f, 1f, true, () -> config.soundWitherDeath, v -> config.soundWitherDeath = v));
        allEntries.add(SettingEntry.slider("Ender Dragon — Ambient",
                0f, 1f, true, () -> config.soundDragonAmbient, v -> config.soundDragonAmbient = v));
        allEntries.add(SettingEntry.slider("Ender Dragon — Growl",
                0f, 1f, true, () -> config.soundDragonGrowl, v -> config.soundDragonGrowl = v));
        allEntries.add(SettingEntry.slider("Ender Dragon — Hurt",
                0f, 1f, true, () -> config.soundDragonHurt, v -> config.soundDragonHurt = v));
        allEntries.add(SettingEntry.slider("Ender Dragon — Death",
                0f, 1f, true, () -> config.soundDragonDeath, v -> config.soundDragonDeath = v));
        allEntries.add(SettingEntry.slider("Ender Dragon — Flap",
                0f, 1f, true, () -> config.soundDragonFlap, v -> config.soundDragonFlap = v));
        allEntries.add(SettingEntry.slider("Guardian — Ambient",
                0f, 1f, true, () -> config.soundGuardianAmbient, v -> config.soundGuardianAmbient = v));
        allEntries.add(SettingEntry.slider("Guardian — Attack",
                0f, 1f, true, () -> config.soundGuardianAttack, v -> config.soundGuardianAttack = v));
        allEntries.add(SettingEntry.slider("Guardian — Hurt",
                0f, 1f, true, () -> config.soundGuardianHurt, v -> config.soundGuardianHurt = v));
        allEntries.add(SettingEntry.slider("Guardian — Death",
                0f, 1f, true, () -> config.soundGuardianDeath, v -> config.soundGuardianDeath = v));
        allEntries.add(SettingEntry.slider("Shulker — Ambient",
                0f, 1f, true, () -> config.soundShulkerAmbient, v -> config.soundShulkerAmbient = v));
        allEntries.add(SettingEntry.slider("Shulker — Open",
                0f, 1f, true, () -> config.soundShulkerOpen, v -> config.soundShulkerOpen = v));
        allEntries.add(SettingEntry.slider("Shulker — Close",
                0f, 1f, true, () -> config.soundShulkerClose, v -> config.soundShulkerClose = v));
        allEntries.add(SettingEntry.slider("Shulker — Shoot",
                0f, 1f, true, () -> config.soundShulkerShoot, v -> config.soundShulkerShoot = v));
        allEntries.add(SettingEntry.slider("Shulker — Hurt",
                0f, 1f, true, () -> config.soundShulkerHurt, v -> config.soundShulkerHurt = v));
        allEntries.add(SettingEntry.slider("Shulker — Death",
                0f, 1f, true, () -> config.soundShulkerDeath, v -> config.soundShulkerDeath = v));
        allEntries.add(SettingEntry.slider("Shulker — Teleport",
                0f, 1f, true, () -> config.soundShulkerTeleport, v -> config.soundShulkerTeleport = v));
        allEntries.add(SettingEntry.slider("Witch — Ambient",
                0f, 1f, true, () -> config.soundWitchAmbient, v -> config.soundWitchAmbient = v));
        allEntries.add(SettingEntry.slider("Witch — Hurt",
                0f, 1f, true, () -> config.soundWitchHurt, v -> config.soundWitchHurt = v));
        allEntries.add(SettingEntry.slider("Witch — Death",
                0f, 1f, true, () -> config.soundWitchDeath, v -> config.soundWitchDeath = v));
        allEntries.add(SettingEntry.slider("Husk — Ambient",
                0f, 1f, true, () -> config.soundHuskAmbient, v -> config.soundHuskAmbient = v));
        allEntries.add(SettingEntry.slider("Husk — Hurt",
                0f, 1f, true, () -> config.soundHuskHurt, v -> config.soundHuskHurt = v));
        allEntries.add(SettingEntry.slider("Husk — Death",
                0f, 1f, true, () -> config.soundHuskDeath, v -> config.soundHuskDeath = v));
        allEntries.add(SettingEntry.slider("Stray — Ambient",
                0f, 1f, true, () -> config.soundStrayAmbient, v -> config.soundStrayAmbient = v));
        allEntries.add(SettingEntry.slider("Stray — Hurt",
                0f, 1f, true, () -> config.soundStrayHurt, v -> config.soundStrayHurt = v));
        allEntries.add(SettingEntry.slider("Stray — Death",
                0f, 1f, true, () -> config.soundStrayDeath, v -> config.soundStrayDeath = v));
        allEntries.add(SettingEntry.slider("Phantom — Ambient",
                0f, 1f, true, () -> config.soundPhantomAmbient, v -> config.soundPhantomAmbient = v));
        allEntries.add(SettingEntry.slider("Phantom — Bite",
                0f, 1f, true, () -> config.soundPhantomBite, v -> config.soundPhantomBite = v));
        allEntries.add(SettingEntry.slider("Phantom — Hurt",
                0f, 1f, true, () -> config.soundPhantomHurt, v -> config.soundPhantomHurt = v));
        allEntries.add(SettingEntry.slider("Phantom — Death",
                0f, 1f, true, () -> config.soundPhantomDeath, v -> config.soundPhantomDeath = v));
        allEntries.add(SettingEntry.slider("Drowned — Ambient",
                0f, 1f, true, () -> config.soundDrownedAmbient, v -> config.soundDrownedAmbient = v));
        allEntries.add(SettingEntry.slider("Drowned — Hurt",
                0f, 1f, true, () -> config.soundDrownedHurt, v -> config.soundDrownedHurt = v));
        allEntries.add(SettingEntry.slider("Drowned — Death",
                0f, 1f, true, () -> config.soundDrownedDeath, v -> config.soundDrownedDeath = v));
        allEntries.add(SettingEntry.slider("Pillager — Ambient",
                0f, 1f, true, () -> config.soundPillagerAmbient, v -> config.soundPillagerAmbient = v));
        allEntries.add(SettingEntry.slider("Pillager — Hurt",
                0f, 1f, true, () -> config.soundPillagerHurt, v -> config.soundPillagerHurt = v));
        allEntries.add(SettingEntry.slider("Pillager — Death",
                0f, 1f, true, () -> config.soundPillagerDeath, v -> config.soundPillagerDeath = v));
        allEntries.add(SettingEntry.slider("Ravager — Ambient",
                0f, 1f, true, () -> config.soundRavagerAmbient, v -> config.soundRavagerAmbient = v));
        allEntries.add(SettingEntry.slider("Ravager — Attack",
                0f, 1f, true, () -> config.soundRavagerAttack, v -> config.soundRavagerAttack = v));
        allEntries.add(SettingEntry.slider("Ravager — Hurt",
                0f, 1f, true, () -> config.soundRavagerHurt, v -> config.soundRavagerHurt = v));
        allEntries.add(SettingEntry.slider("Ravager — Death",
                0f, 1f, true, () -> config.soundRavagerDeath, v -> config.soundRavagerDeath = v));
        allEntries.add(SettingEntry.slider("Hoglin — Ambient",
                0f, 1f, true, () -> config.soundHoglinAmbient, v -> config.soundHoglinAmbient = v));
        allEntries.add(SettingEntry.slider("Hoglin — Attack",
                0f, 1f, true, () -> config.soundHoglinAttack, v -> config.soundHoglinAttack = v));
        allEntries.add(SettingEntry.slider("Hoglin — Hurt",
                0f, 1f, true, () -> config.soundHoglinHurt, v -> config.soundHoglinHurt = v));
        allEntries.add(SettingEntry.slider("Hoglin — Death",
                0f, 1f, true, () -> config.soundHoglinDeath, v -> config.soundHoglinDeath = v));
        allEntries.add(SettingEntry.slider("Piglin — Ambient",
                0f, 1f, true, () -> config.soundPiglinAmbient, v -> config.soundPiglinAmbient = v));
        allEntries.add(SettingEntry.slider("Piglin — Angry",
                0f, 1f, true, () -> config.soundPiglinAngry, v -> config.soundPiglinAngry = v));
        allEntries.add(SettingEntry.slider("Piglin — Hurt",
                0f, 1f, true, () -> config.soundPiglinHurt, v -> config.soundPiglinHurt = v));
        allEntries.add(SettingEntry.slider("Piglin — Death",
                0f, 1f, true, () -> config.soundPiglinDeath, v -> config.soundPiglinDeath = v));
        allEntries.add(SettingEntry.slider("Warden — Ambient",
                0f, 1f, true, () -> config.soundWardenAmbient, v -> config.soundWardenAmbient = v));
        allEntries.add(SettingEntry.slider("Warden — Nearby Close (heartbeat fast)",
                0f, 1f, true, () -> config.soundWardenNearbyClose, v -> config.soundWardenNearbyClose = v));
        allEntries.add(SettingEntry.slider("Warden — Sonic Boom",
                0f, 1f, true, () -> config.soundWardenSonicBoom, v -> config.soundWardenSonicBoom = v));
        allEntries.add(SettingEntry.slider("Warden — Roar",
                0f, 1f, true, () -> config.soundWardenRoar, v -> config.soundWardenRoar = v));
        allEntries.add(SettingEntry.slider("Warden — Hurt",
                0f, 1f, true, () -> config.soundWardenHurt, v -> config.soundWardenHurt = v));
        allEntries.add(SettingEntry.slider("Warden — Death",
                0f, 1f, true, () -> config.soundWardenDeath, v -> config.soundWardenDeath = v));
        allEntries.add(SettingEntry.slider("Warden — Emerge (digging up)",
                0f, 1f, true, () -> config.soundWardenEmerge, v -> config.soundWardenEmerge = v));
        allEntries.add(SettingEntry.slider("Warden — Dig (sinking down)",
                0f, 1f, true, () -> config.soundWardenDig, v -> config.soundWardenDig = v));
        allEntries.add(SettingEntry.slider("Breeze — Idle Ground",
                0f, 1f, true, () -> config.soundBreezeIdle, v -> config.soundBreezeIdle = v));
        allEntries.add(SettingEntry.slider("Breeze — Shoot (wind charge)",
                0f, 1f, true, () -> config.soundBreezeShoot, v -> config.soundBreezeShoot = v));
        allEntries.add(SettingEntry.slider("Breeze — Hurt",
                0f, 1f, true, () -> config.soundBreezeHurt, v -> config.soundBreezeHurt = v));
        allEntries.add(SettingEntry.slider("Breeze — Death",
                0f, 1f, true, () -> config.soundBreezeDeath, v -> config.soundBreezeDeath = v));
        allEntries.add(SettingEntry.slider("Silverfish — Ambient",
                0f, 1f, true, () -> config.soundSilverfishAmbient, v -> config.soundSilverfishAmbient = v));
        allEntries.add(SettingEntry.slider("Silverfish — Hurt",
                0f, 1f, true, () -> config.soundSilverfishHurt, v -> config.soundSilverfishHurt = v));
        allEntries.add(SettingEntry.slider("Silverfish — Death",
                0f, 1f, true, () -> config.soundSilverfishDeath, v -> config.soundSilverfishDeath = v));
        allEntries.add(SettingEntry.slider("Magma Cube — Jump",
                0f, 1f, true, () -> config.soundMagmaCubeJump, v -> config.soundMagmaCubeJump = v));
        allEntries.add(SettingEntry.slider("Magma Cube — Squish",
                0f, 1f, true, () -> config.soundMagmaCubeSquish, v -> config.soundMagmaCubeSquish = v));
        allEntries.add(SettingEntry.slider("Magma Cube — Death",
                0f, 1f, true, () -> config.soundMagmaCubeDeath, v -> config.soundMagmaCubeDeath = v));
        allEntries.add(SettingEntry.slider("Vex — Ambient",
                0f, 1f, true, () -> config.soundVexAmbient, v -> config.soundVexAmbient = v));
        allEntries.add(SettingEntry.slider("Vex — Charge",
                0f, 1f, true, () -> config.soundVexCharge, v -> config.soundVexCharge = v));
        allEntries.add(SettingEntry.slider("Vex — Hurt",
                0f, 1f, true, () -> config.soundVexHurt, v -> config.soundVexHurt = v));
        allEntries.add(SettingEntry.slider("Vex — Death",
                0f, 1f, true, () -> config.soundVexDeath, v -> config.soundVexDeath = v));
        allEntries.add(SettingEntry.slider("Evoker — Ambient",
                0f, 1f, true, () -> config.soundEvokerAmbient, v -> config.soundEvokerAmbient = v));
        allEntries.add(SettingEntry.slider("Evoker — Cast Spell",
                0f, 1f, true, () -> config.soundEvokerCast, v -> config.soundEvokerCast = v));
        allEntries.add(SettingEntry.slider("Evoker — Hurt",
                0f, 1f, true, () -> config.soundEvokerHurt, v -> config.soundEvokerHurt = v));
        allEntries.add(SettingEntry.slider("Evoker — Death",
                0f, 1f, true, () -> config.soundEvokerDeath, v -> config.soundEvokerDeath = v));
        allEntries.add(SettingEntry.slider("Vindicator — Ambient",
                0f, 1f, true, () -> config.soundVindicatorAmbient, v -> config.soundVindicatorAmbient = v));
        allEntries.add(SettingEntry.slider("Vindicator — Hurt",
                0f, 1f, true, () -> config.soundVindicatorHurt, v -> config.soundVindicatorHurt = v));
        allEntries.add(SettingEntry.slider("Vindicator — Death",
                0f, 1f, true, () -> config.soundVindicatorDeath, v -> config.soundVindicatorDeath = v));
        allEntries.add(SettingEntry.slider("Endermite — Ambient",
                0f, 1f, true, () -> config.soundEndermiteAmbient, v -> config.soundEndermiteAmbient = v));
        allEntries.add(SettingEntry.slider("Endermite — Hurt",
                0f, 1f, true, () -> config.soundEndermiteHurt, v -> config.soundEndermiteHurt = v));
        allEntries.add(SettingEntry.slider("Endermite — Death",
                0f, 1f, true, () -> config.soundEndermiteDeath, v -> config.soundEndermiteDeath = v));
        allEntries.add(SettingEntry.slider("Illusioner — Ambient",
                0f, 1f, true, () -> config.soundIllusionerAmbient, v -> config.soundIllusionerAmbient = v));
        allEntries.add(SettingEntry.slider("Illusioner — Cast Spell",
                0f, 1f, true, () -> config.soundIllusionerCast, v -> config.soundIllusionerCast = v));
        allEntries.add(SettingEntry.slider("Illusioner — Hurt",
                0f, 1f, true, () -> config.soundIllusionerHurt, v -> config.soundIllusionerHurt = v));
        allEntries.add(SettingEntry.slider("Illusioner — Death",
                0f, 1f, true, () -> config.soundIllusionerDeath, v -> config.soundIllusionerDeath = v));
        allEntries.add(SettingEntry.slider("Bogged — Ambient",
                0f, 1f, true, () -> config.soundBoggedAmbient, v -> config.soundBoggedAmbient = v));
        allEntries.add(SettingEntry.slider("Bogged — Hurt",
                0f, 1f, true, () -> config.soundBoggedHurt, v -> config.soundBoggedHurt = v));
        allEntries.add(SettingEntry.slider("Bogged — Death",
                0f, 1f, true, () -> config.soundBoggedDeath, v -> config.soundBoggedDeath = v));
        allEntries.add(SettingEntry.slider("Creaking — Ambient",
                0f, 1f, true, () -> config.soundCreakingAmbient, v -> config.soundCreakingAmbient = v));
        allEntries.add(SettingEntry.slider("Creaking — Activate",
                0f, 1f, true, () -> config.soundCreakingActivate, v -> config.soundCreakingActivate = v));
        allEntries.add(SettingEntry.slider("Creaking — Hurt",
                0f, 1f, true, () -> config.soundCreakingHurt, v -> config.soundCreakingHurt = v));
        allEntries.add(SettingEntry.slider("Creaking — Death",
                0f, 1f, true, () -> config.soundCreakingDeath, v -> config.soundCreakingDeath = v));

        allEntries.add(SettingEntry.header("SOUND — Neutral Mobs"));
        allEntries.add(SettingEntry.slider("Neutral Mobs (category volume)",
                0f, 1f, true, () -> config.neutralVolume, v -> config.neutralVolume = v));
        allEntries.add(SettingEntry.slider("Pig — Ambient",
                0f, 1f, true, () -> config.soundPigAmbient, v -> config.soundPigAmbient = v));
        allEntries.add(SettingEntry.slider("Pig — Hurt",
                0f, 1f, true, () -> config.soundPigHurt, v -> config.soundPigHurt = v));
        allEntries.add(SettingEntry.slider("Pig — Death",
                0f, 1f, true, () -> config.soundPigDeath, v -> config.soundPigDeath = v));
        allEntries.add(SettingEntry.slider("Pig — Step",
                0f, 1f, true, () -> config.soundPigStep, v -> config.soundPigStep = v));
        allEntries.add(SettingEntry.slider("Cow — Ambient",
                0f, 1f, true, () -> config.soundCowAmbient, v -> config.soundCowAmbient = v));
        allEntries.add(SettingEntry.slider("Cow — Hurt",
                0f, 1f, true, () -> config.soundCowHurt, v -> config.soundCowHurt = v));
        allEntries.add(SettingEntry.slider("Cow — Death",
                0f, 1f, true, () -> config.soundCowDeath, v -> config.soundCowDeath = v));
        allEntries.add(SettingEntry.slider("Cow — Step",
                0f, 1f, true, () -> config.soundCowStep, v -> config.soundCowStep = v));
        allEntries.add(SettingEntry.slider("Cow — Milk",
                0f, 1f, true, () -> config.soundCowMilk, v -> config.soundCowMilk = v));
        allEntries.add(SettingEntry.slider("Sheep — Ambient",
                0f, 1f, true, () -> config.soundSheepAmbient, v -> config.soundSheepAmbient = v));
        allEntries.add(SettingEntry.slider("Sheep — Hurt",
                0f, 1f, true, () -> config.soundSheepHurt, v -> config.soundSheepHurt = v));
        allEntries.add(SettingEntry.slider("Sheep — Death",
                0f, 1f, true, () -> config.soundSheepDeath, v -> config.soundSheepDeath = v));
        allEntries.add(SettingEntry.slider("Sheep — Shear",
                0f, 1f, true, () -> config.soundSheepShear, v -> config.soundSheepShear = v));
        allEntries.add(SettingEntry.slider("Chicken — Ambient",
                0f, 1f, true, () -> config.soundChickenAmbient, v -> config.soundChickenAmbient = v));
        allEntries.add(SettingEntry.slider("Chicken — Hurt",
                0f, 1f, true, () -> config.soundChickenHurt, v -> config.soundChickenHurt = v));
        allEntries.add(SettingEntry.slider("Chicken — Death",
                0f, 1f, true, () -> config.soundChickenDeath, v -> config.soundChickenDeath = v));
        allEntries.add(SettingEntry.slider("Chicken — Egg Lay",
                0f, 1f, true, () -> config.soundChickenEgg, v -> config.soundChickenEgg = v));
        allEntries.add(SettingEntry.slider("Wolf — Ambient",
                0f, 1f, true, () -> config.soundWolfAmbient, v -> config.soundWolfAmbient = v));
        allEntries.add(SettingEntry.slider("Wolf — Growl",
                0f, 1f, true, () -> config.soundWolfGrowl, v -> config.soundWolfGrowl = v));
        allEntries.add(SettingEntry.slider("Wolf — Hurt",
                0f, 1f, true, () -> config.soundWolfHurt, v -> config.soundWolfHurt = v));
        allEntries.add(SettingEntry.slider("Wolf — Death",
                0f, 1f, true, () -> config.soundWolfDeath, v -> config.soundWolfDeath = v));
        allEntries.add(SettingEntry.slider("Wolf — Pant (happy)",
                0f, 1f, true, () -> config.soundWolfPant, v -> config.soundWolfPant = v));
        allEntries.add(SettingEntry.slider("Cat — Ambient",
                0f, 1f, true, () -> config.soundCatAmbient, v -> config.soundCatAmbient = v));
        allEntries.add(SettingEntry.slider("Cat — Hurt",
                0f, 1f, true, () -> config.soundCatHurt, v -> config.soundCatHurt = v));
        allEntries.add(SettingEntry.slider("Cat — Death",
                0f, 1f, true, () -> config.soundCatDeath, v -> config.soundCatDeath = v));
        allEntries.add(SettingEntry.slider("Cat — Purr",
                0f, 1f, true, () -> config.soundCatPurr, v -> config.soundCatPurr = v));
        allEntries.add(SettingEntry.slider("Cat — Hiss",
                0f, 1f, true, () -> config.soundCatHiss, v -> config.soundCatHiss = v));
        allEntries.add(SettingEntry.slider("Horse — Ambient",
                0f, 1f, true, () -> config.soundHorseAmbient, v -> config.soundHorseAmbient = v));
        allEntries.add(SettingEntry.slider("Horse — Hurt",
                0f, 1f, true, () -> config.soundHorseHurt, v -> config.soundHorseHurt = v));
        allEntries.add(SettingEntry.slider("Horse — Death",
                0f, 1f, true, () -> config.soundHorseDeath, v -> config.soundHorseDeath = v));
        allEntries.add(SettingEntry.slider("Horse — Gallop",
                0f, 1f, true, () -> config.soundHorseGallop, v -> config.soundHorseGallop = v));
        allEntries.add(SettingEntry.slider("Horse — Jump",
                0f, 1f, true, () -> config.soundHorseJump, v -> config.soundHorseJump = v));
        allEntries.add(SettingEntry.slider("Bee — Ambient",
                0f, 1f, true, () -> config.soundBeeAmbient, v -> config.soundBeeAmbient = v));
        allEntries.add(SettingEntry.slider("Bee — Hurt",
                0f, 1f, true, () -> config.soundBeeHurt, v -> config.soundBeeHurt = v));
        allEntries.add(SettingEntry.slider("Bee — Death",
                0f, 1f, true, () -> config.soundBeeDeath, v -> config.soundBeeDeath = v));
        allEntries.add(SettingEntry.slider("Bee — Sting",
                0f, 1f, true, () -> config.soundBeeSting, v -> config.soundBeeSting = v));
        allEntries.add(SettingEntry.slider("Fox — Ambient",
                0f, 1f, true, () -> config.soundFoxAmbient, v -> config.soundFoxAmbient = v));
        allEntries.add(SettingEntry.slider("Fox — Hurt",
                0f, 1f, true, () -> config.soundFoxHurt, v -> config.soundFoxHurt = v));
        allEntries.add(SettingEntry.slider("Fox — Death",
                0f, 1f, true, () -> config.soundFoxDeath, v -> config.soundFoxDeath = v));
        allEntries.add(SettingEntry.slider("Fox — Screech",
                0f, 1f, true, () -> config.soundFoxScreech, v -> config.soundFoxScreech = v));
        allEntries.add(SettingEntry.slider("Panda — Ambient",
                0f, 1f, true, () -> config.soundPandaAmbient, v -> config.soundPandaAmbient = v));
        allEntries.add(SettingEntry.slider("Panda — Sneeze",
                0f, 1f, true, () -> config.soundPandaSneeze, v -> config.soundPandaSneeze = v));
        allEntries.add(SettingEntry.slider("Panda — Hurt",
                0f, 1f, true, () -> config.soundPandaHurt, v -> config.soundPandaHurt = v));
        allEntries.add(SettingEntry.slider("Panda — Death",
                0f, 1f, true, () -> config.soundPandaDeath, v -> config.soundPandaDeath = v));
        allEntries.add(SettingEntry.slider("Llama — Ambient",
                0f, 1f, true, () -> config.soundLlamaAmbient, v -> config.soundLlamaAmbient = v));
        allEntries.add(SettingEntry.slider("Llama — Hurt",
                0f, 1f, true, () -> config.soundLlamaHurt, v -> config.soundLlamaHurt = v));
        allEntries.add(SettingEntry.slider("Llama — Death",
                0f, 1f, true, () -> config.soundLlamaDeath, v -> config.soundLlamaDeath = v));
        allEntries.add(SettingEntry.slider("Llama — Spit",
                0f, 1f, true, () -> config.soundLlamaSpit, v -> config.soundLlamaSpit = v));
        allEntries.add(SettingEntry.slider("Dolphin — Ambient",
                0f, 1f, true, () -> config.soundDolphinAmbient, v -> config.soundDolphinAmbient = v));
        allEntries.add(SettingEntry.slider("Dolphin — Hurt",
                0f, 1f, true, () -> config.soundDolphinHurt, v -> config.soundDolphinHurt = v));
        allEntries.add(SettingEntry.slider("Dolphin — Death",
                0f, 1f, true, () -> config.soundDolphinDeath, v -> config.soundDolphinDeath = v));
        allEntries.add(SettingEntry.slider("Turtle — Ambient",
                0f, 1f, true, () -> config.soundTurtleAmbient, v -> config.soundTurtleAmbient = v));
        allEntries.add(SettingEntry.slider("Turtle — Hurt",
                0f, 1f, true, () -> config.soundTurtleHurt, v -> config.soundTurtleHurt = v));
        allEntries.add(SettingEntry.slider("Turtle — Death",
                0f, 1f, true, () -> config.soundTurtleDeath, v -> config.soundTurtleDeath = v));
        allEntries.add(SettingEntry.slider("Polar Bear — Ambient",
                0f, 1f, true, () -> config.soundPolarBearAmbient, v -> config.soundPolarBearAmbient = v));
        allEntries.add(SettingEntry.slider("Polar Bear — Hurt",
                0f, 1f, true, () -> config.soundPolarBearHurt, v -> config.soundPolarBearHurt = v));
        allEntries.add(SettingEntry.slider("Polar Bear — Death",
                0f, 1f, true, () -> config.soundPolarBearDeath, v -> config.soundPolarBearDeath = v));
        allEntries.add(SettingEntry.slider("Polar Bear — Warning (growl)",
                0f, 1f, true, () -> config.soundPolarBearWarning, v -> config.soundPolarBearWarning = v));
        allEntries.add(SettingEntry.slider("Goat — Ambient",
                0f, 1f, true, () -> config.soundGoatAmbient, v -> config.soundGoatAmbient = v));
        allEntries.add(SettingEntry.slider("Goat — Hurt",
                0f, 1f, true, () -> config.soundGoatHurt, v -> config.soundGoatHurt = v));
        allEntries.add(SettingEntry.slider("Goat — Death",
                0f, 1f, true, () -> config.soundGoatDeath, v -> config.soundGoatDeath = v));
        allEntries.add(SettingEntry.slider("Goat — Milk",
                0f, 1f, true, () -> config.soundGoatMilk, v -> config.soundGoatMilk = v));
        allEntries.add(SettingEntry.slider("Goat — Ram Impact",
                0f, 1f, true, () -> config.soundGoatRamImpact, v -> config.soundGoatRamImpact = v));
        allEntries.add(SettingEntry.slider("Axolotl — Ambient",
                0f, 1f, true, () -> config.soundAxolotlAmbient, v -> config.soundAxolotlAmbient = v));
        allEntries.add(SettingEntry.slider("Axolotl — Hurt",
                0f, 1f, true, () -> config.soundAxolotlHurt, v -> config.soundAxolotlHurt = v));
        allEntries.add(SettingEntry.slider("Axolotl — Death",
                0f, 1f, true, () -> config.soundAxolotlDeath, v -> config.soundAxolotlDeath = v));
        allEntries.add(SettingEntry.slider("Allay — Ambient",
                0f, 1f, true, () -> config.soundAllayAmbient, v -> config.soundAllayAmbient = v));
        allEntries.add(SettingEntry.slider("Allay — Hurt",
                0f, 1f, true, () -> config.soundAllayHurt, v -> config.soundAllayHurt = v));
        allEntries.add(SettingEntry.slider("Allay — Death",
                0f, 1f, true, () -> config.soundAllayDeath, v -> config.soundAllayDeath = v));
        allEntries.add(SettingEntry.slider("Frog — Ambient",
                0f, 1f, true, () -> config.soundFrogAmbient, v -> config.soundFrogAmbient = v));
        allEntries.add(SettingEntry.slider("Frog — Hurt",
                0f, 1f, true, () -> config.soundFrogHurt, v -> config.soundFrogHurt = v));
        allEntries.add(SettingEntry.slider("Frog — Death",
                0f, 1f, true, () -> config.soundFrogDeath, v -> config.soundFrogDeath = v));
        allEntries.add(SettingEntry.slider("Frog — Tongue",
                0f, 1f, true, () -> config.soundFrogTongue, v -> config.soundFrogTongue = v));
        allEntries.add(SettingEntry.slider("Sniffer — Ambient",
                0f, 1f, true, () -> config.soundSnifferAmbient, v -> config.soundSnifferAmbient = v));
        allEntries.add(SettingEntry.slider("Sniffer — Sniffing",
                0f, 1f, true, () -> config.soundSnifferSniffing, v -> config.soundSnifferSniffing = v));
        allEntries.add(SettingEntry.slider("Sniffer — Hurt",
                0f, 1f, true, () -> config.soundSnifferHurt, v -> config.soundSnifferHurt = v));
        allEntries.add(SettingEntry.slider("Sniffer — Death",
                0f, 1f, true, () -> config.soundSnifferDeath, v -> config.soundSnifferDeath = v));
        allEntries.add(SettingEntry.slider("Camel — Ambient",
                0f, 1f, true, () -> config.soundCamelAmbient, v -> config.soundCamelAmbient = v));
        allEntries.add(SettingEntry.slider("Camel — Hurt",
                0f, 1f, true, () -> config.soundCamelHurt, v -> config.soundCamelHurt = v));
        allEntries.add(SettingEntry.slider("Camel — Death",
                0f, 1f, true, () -> config.soundCamelDeath, v -> config.soundCamelDeath = v));
        allEntries.add(SettingEntry.slider("Armadillo — Ambient",
                0f, 1f, true, () -> config.soundArmadilloAmbient, v -> config.soundArmadilloAmbient = v));
        allEntries.add(SettingEntry.slider("Armadillo — Hurt",
                0f, 1f, true, () -> config.soundArmadilloHurt, v -> config.soundArmadilloHurt = v));
        allEntries.add(SettingEntry.slider("Armadillo — Death",
                0f, 1f, true, () -> config.soundArmadilloDeath, v -> config.soundArmadilloDeath = v));
        allEntries.add(SettingEntry.slider("Armadillo — Roll",
                0f, 1f, true, () -> config.soundArmadilloRoll, v -> config.soundArmadilloRoll = v));

        allEntries.add(SettingEntry.header("SOUND — Ambient / Environment"));
        allEntries.add(SettingEntry.slider("Ambient / Environment (category)",
                0f, 1f, true, () -> config.ambientVolume, v -> config.ambientVolume = v));
        allEntries.add(SettingEntry.slider("Cave Ambience (underground sounds)",
                0f, 1f, true, () -> config.soundCaveAmbient, v -> config.soundCaveAmbient = v));
        allEntries.add(SettingEntry.slider("Water (swimming, splashing)",
                0f, 1f, true, () -> config.soundWater, v -> config.soundWater = v));
        allEntries.add(SettingEntry.slider("Lava (bubbling, flowing lava)",
                0f, 1f, true, () -> config.soundLava, v -> config.soundLava = v));
        allEntries.add(SettingEntry.slider("Fire (burning flames)",
                0f, 1f, true, () -> config.soundFire, v -> config.soundFire = v));
        allEntries.add(SettingEntry.slider("Portal (nether portal hum)",
                0f, 1f, true, () -> config.soundPortal, v -> config.soundPortal = v));
        allEntries.add(SettingEntry.slider("Underwater Ambience",
                0f, 1f, true, () -> config.soundUnderwaterAmbient, v -> config.soundUnderwaterAmbient = v));
        allEntries.add(SettingEntry.slider("Beacon Ambient (hum)",
                0f, 1f, true, () -> config.soundBeaconAmbient, v -> config.soundBeaconAmbient = v));
        allEntries.add(SettingEntry.slider("Conduit Ambient",
                0f, 1f, true, () -> config.soundConduitAmbient, v -> config.soundConduitAmbient = v));
        allEntries.add(SettingEntry.slider("Sculk Sensor Clicking",
                0f, 1f, true, () -> config.soundSculkSensor, v -> config.soundSculkSensor = v));
        allEntries.add(SettingEntry.slider("Sculk Shrieker Shriek",
                0f, 1f, true, () -> config.soundSculkShrieker, v -> config.soundSculkShrieker = v));

        allEntries.add(SettingEntry.header("SOUND — Player"));
        allEntries.add(SettingEntry.slider("Player — Hurt",
                0f, 1f, true, () -> config.soundPlayerHurt, v -> config.soundPlayerHurt = v));
        allEntries.add(SettingEntry.slider("Player — Death",
                0f, 1f, true, () -> config.soundPlayerDeath, v -> config.soundPlayerDeath = v));
        allEntries.add(SettingEntry.slider("Player — Eat / Drink",
                0f, 1f, true, () -> config.soundPlayerEat, v -> config.soundPlayerEat = v));
        allEntries.add(SettingEntry.slider("Player — Burp",
                0f, 1f, true, () -> config.soundPlayerBurp, v -> config.soundPlayerBurp = v));
        allEntries.add(SettingEntry.slider("Player — Swim",
                0f, 1f, true, () -> config.soundPlayerSwim, v -> config.soundPlayerSwim = v));
        allEntries.add(SettingEntry.slider("Player — Splash",
                0f, 1f, true, () -> config.soundPlayerSplash, v -> config.soundPlayerSplash = v));
        allEntries.add(SettingEntry.slider("Player — Level Up",
                0f, 1f, true, () -> config.soundPlayerLevelUp, v -> config.soundPlayerLevelUp = v));
        allEntries.add(SettingEntry.slider("Player — Attack (strong)",
                0f, 1f, true, () -> config.soundPlayerAttack, v -> config.soundPlayerAttack = v));
        allEntries.add(SettingEntry.slider("Player — Teleport",
                0f, 1f, true, () -> config.soundPlayerTeleport, v -> config.soundPlayerTeleport = v));

        allEntries.add(SettingEntry.header("SOUND — Blocks"));
        allEntries.add(SettingEntry.slider("Block — Break (generic)",
                0f, 1f, true, () -> config.soundBlockBreak, v -> config.soundBlockBreak = v));
        allEntries.add(SettingEntry.slider("Block — Place (generic)",
                0f, 1f, true, () -> config.soundBlockPlace, v -> config.soundBlockPlace = v));
        allEntries.add(SettingEntry.slider("Chest — Open / Close",
                0f, 1f, true, () -> config.soundChest, v -> config.soundChest = v));
        allEntries.add(SettingEntry.slider("Door — Open / Close (wood)",
                0f, 1f, true, () -> config.soundDoorWood, v -> config.soundDoorWood = v));
        allEntries.add(SettingEntry.slider("Door — Open / Close (iron)",
                0f, 1f, true, () -> config.soundDoorIron, v -> config.soundDoorIron = v));
        allEntries.add(SettingEntry.slider("Trapdoor — Open / Close",
                0f, 1f, true, () -> config.soundTrapdoor, v -> config.soundTrapdoor = v));
        allEntries.add(SettingEntry.slider("Button / Lever Click",
                0f, 1f, true, () -> config.soundButton, v -> config.soundButton = v));
        allEntries.add(SettingEntry.slider("Piston Extend / Retract",
                0f, 1f, true, () -> config.soundPiston, v -> config.soundPiston = v));
        allEntries.add(SettingEntry.slider("TNT Primed (hissing)",
                0f, 1f, true, () -> config.soundTNT, v -> config.soundTNT = v));
        allEntries.add(SettingEntry.slider("Explosion",
                0f, 1f, true, () -> config.soundExplosion, v -> config.soundExplosion = v));
        allEntries.add(SettingEntry.slider("Anvil — Use / Land",
                0f, 1f, true, () -> config.soundAnvil, v -> config.soundAnvil = v));
        allEntries.add(SettingEntry.slider("Enchantment Table Use",
                0f, 1f, true, () -> config.soundEnchantTable, v -> config.soundEnchantTable = v));
        allEntries.add(SettingEntry.slider("Note Block",
                0f, 1f, true, () -> config.soundNoteBlock, v -> config.soundNoteBlock = v));
        allEntries.add(SettingEntry.slider("Amethyst Block / Cluster",
                0f, 1f, true, () -> config.soundAmethyst, v -> config.soundAmethyst = v));
        allEntries.add(SettingEntry.slider("Bell",
                0f, 1f, true, () -> config.soundBell, v -> config.soundBell = v));
        allEntries.add(SettingEntry.slider("Campfire Crackle",
                0f, 1f, true, () -> config.soundCampfire, v -> config.soundCampfire = v));
        allEntries.add(SettingEntry.slider("Grindstone Use",
                0f, 1f, true, () -> config.soundGrindstone, v -> config.soundGrindstone = v));
        allEntries.add(SettingEntry.slider("Brewing Stand Brew",
                0f, 1f, true, () -> config.soundBrewingStand, v -> config.soundBrewingStand = v));

        allEntries.add(SettingEntry.header("SOUND — Items / Combat"));
        allEntries.add(SettingEntry.slider("Arrow — Shoot",
                0f, 1f, true, () -> config.soundArrowShoot, v -> config.soundArrowShoot = v));
        allEntries.add(SettingEntry.slider("Arrow — Hit",
                0f, 1f, true, () -> config.soundArrowHit, v -> config.soundArrowHit = v));
        allEntries.add(SettingEntry.slider("Sword / Item Sweep",
                0f, 1f, true, () -> config.soundSweep, v -> config.soundSweep = v));
        allEntries.add(SettingEntry.slider("Shield Block",
                0f, 1f, true, () -> config.soundShieldBlock, v -> config.soundShieldBlock = v));
        allEntries.add(SettingEntry.slider("Shield Break",
                0f, 1f, true, () -> config.soundShieldBreak, v -> config.soundShieldBreak = v));
        allEntries.add(SettingEntry.slider("Item Pickup (popping sound)",
                0f, 1f, true, () -> config.soundItemPickup, v -> config.soundItemPickup = v));
        allEntries.add(SettingEntry.slider("Item Break",
                0f, 1f, true, () -> config.soundItemBreak, v -> config.soundItemBreak = v));
        allEntries.add(SettingEntry.slider("Crossbow — Load / Shoot",
                0f, 1f, true, () -> config.soundCrossbow, v -> config.soundCrossbow = v));
        allEntries.add(SettingEntry.slider("Trident — Throw / Hit",
                0f, 1f, true, () -> config.soundTrident, v -> config.soundTrident = v));
        allEntries.add(SettingEntry.slider("Totem of Undying — Use",
                0f, 1f, true, () -> config.soundTotem, v -> config.soundTotem = v));
        allEntries.add(SettingEntry.slider("Experience Orb — Pickup",
                0f, 1f, true, () -> config.soundXPPickup, v -> config.soundXPPickup = v));
        allEntries.add(SettingEntry.slider("Fishing — Cast / Reel",
                0f, 1f, true, () -> config.soundFishing, v -> config.soundFishing = v));
        allEntries.add(SettingEntry.slider("Firework — Launch / Blast",
                0f, 1f, true, () -> config.soundFirework, v -> config.soundFirework = v));
        allEntries.add(SettingEntry.slider("Elytra — Flying",
                0f, 1f, true, () -> config.soundElytra, v -> config.soundElytra = v));
        allEntries.add(SettingEntry.slider("Bucket — Fill / Empty",
                0f, 1f, true, () -> config.soundBucket, v -> config.soundBucket = v));
        allEntries.add(SettingEntry.slider("Ender Pearl — Throw",
                0f, 1f, true, () -> config.soundEnderPearl, v -> config.soundEnderPearl = v));
        allEntries.add(SettingEntry.slider("Potion — Splash / Throw",
                0f, 1f, true, () -> config.soundPotion, v -> config.soundPotion = v));

        allEntries.add(SettingEntry.header("SOUND — UI / Misc"));
        allEntries.add(SettingEntry.slider("Voice / Speech (category)",
                0f, 1f, true, () -> config.voiceVolume, v -> config.voiceVolume = v));
        allEntries.add(SettingEntry.slider("UI Button Click",
                0f, 1f, true, () -> config.soundUIClick, v -> config.soundUIClick = v));
        allEntries.add(SettingEntry.slider("Toast (achievement popup) Sound",
                0f, 1f, true, () -> config.soundToast, v -> config.soundToast = v));
        allEntries.add(SettingEntry.slider("Villager — Ambient",
                0f, 1f, true, () -> config.soundVillagerAmbient, v -> config.soundVillagerAmbient = v));
        allEntries.add(SettingEntry.slider("Villager — Trade",
                0f, 1f, true, () -> config.soundVillagerTrade, v -> config.soundVillagerTrade = v));
        allEntries.add(SettingEntry.slider("Villager — Hurt",
                0f, 1f, true, () -> config.soundVillagerHurt, v -> config.soundVillagerHurt = v));
        allEntries.add(SettingEntry.slider("Villager — Death",
                0f, 1f, true, () -> config.soundVillagerDeath, v -> config.soundVillagerDeath = v));
        allEntries.add(SettingEntry.slider("Lightning — Thunder / Strike",
                0f, 1f, true, () -> config.soundLightning, v -> config.soundLightning = v));

        // ==================== VIDEO ====================
        allEntries.add(SettingEntry.header("VIDEO"));
        allEntries.add(SettingEntry.slider("Brightness",
                0f, 1.5f, true, () -> config.brightness, v -> config.brightness = v));
        allEntries.add(SettingEntry.slider("Field of View / FOV",
                30f, 110f, false, () -> (float) config.fov, v -> config.fov = Math.round(v)));
        allEntries.add(SettingEntry.slider("Render Distance (chunks)",
                2f, 32f, false, () -> (float) config.renderDistance, v -> config.renderDistance = Math.round(v)));
        allEntries.add(SettingEntry.slider("Simulation Distance (chunks)",
                5f, 32f, false, () -> (float) config.simulationDistance, v -> config.simulationDistance = Math.round(v)));
        allEntries.add(SettingEntry.slider("Max Framerate (fps, 260 = unlimited)",
                10f, 260f, false, () -> (float) config.maxFramerate, v -> config.maxFramerate = Math.round(v)));
        allEntries.add(SettingEntry.slider("GUI Scale (0 = auto, 1-4)",
                0f, 4f, false, () -> (float) config.guiScale, v -> config.guiScale = Math.round(v)));
        allEntries.add(SettingEntry.slider("Mipmap Levels (texture quality, 0-4)",
                0f, 4f, false, () -> (float) config.mipmapLevels, v -> config.mipmapLevels = Math.round(v)));
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
        allEntries.add(SettingEntry.toggle("Clickable Chat Links",
                () -> config.chatLinks ? 1f : 0f, v -> config.chatLinks = v >= 0.5f));
        allEntries.add(SettingEntry.toggle("Prompt Before Opening Links",
                () -> config.chatLinksPrompt ? 1f : 0f, v -> config.chatLinksPrompt = v >= 0.5f));

        // ==================== GAMEPLAY ====================
        allEntries.add(SettingEntry.header("GAMEPLAY"));
        allEntries.add(SettingEntry.toggle("Auto Jump",
                () -> config.autoJump ? 1f : 0f, v -> config.autoJump = v >= 0.5f));
        allEntries.add(SettingEntry.toggle("Toggle Sprint",
                () -> config.toggleSprint ? 1f : 0f, v -> config.toggleSprint = v >= 0.5f));
        allEntries.add(SettingEntry.toggle("Toggle Crouch",
                () -> config.toggleCrouch ? 1f : 0f, v -> config.toggleCrouch = v >= 0.5f));
        allEntries.add(SettingEntry.toggle("Reduced Debug Info",
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
        allEntries.add(SettingEntry.toggle("Dark Mojang Loading Screen",
                () -> config.darkMojangScreen ? 1f : 0f, v -> config.darkMojangScreen = v >= 0.5f));
        allEntries.add(SettingEntry.toggle("Monochrome Logo",
                () -> config.monochromeLogo ? 1f : 0f, v -> config.monochromeLogo = v >= 0.5f));
        allEntries.add(SettingEntry.slider("Damage Tilt",
                0f, 1f, true, () -> config.damageTilt, v -> config.damageTilt = v));
        allEntries.add(SettingEntry.slider("Glint Speed",
                0f, 1f, true, () -> config.glintSpeed, v -> config.glintSpeed = v));
        allEntries.add(SettingEntry.slider("Glint Strength",
                0f, 1f, true, () -> config.glintStrength, v -> config.glintStrength = v));
        allEntries.add(SettingEntry.slider("Panorama Speed",
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
        mc.options.save();
    }

    @Override
    public void onClose() {
        MoreSettingsConfig.save();
        applyAll();
        this.minecraft.gui.setScreen(parent);
    }

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

        static SettingEntry header(String l) {
            SettingEntry e = new SettingEntry(); e.label = l; e.isHeader = true; return e;
        }
        static SettingEntry slider(String l, float min, float max, boolean pct,
                                   Supplier<Float> get, Consumer<Float> set) {
            SettingEntry e = new SettingEntry();
            e.label = l; e.min = min; e.max = max;
            e.isSlider = true; e.isPercent = pct;
            e.getValue = get; e.setValue = set; return e;
        }
        static SettingEntry toggle(String l, Supplier<Float> get, Consumer<Float> set) {
            SettingEntry e = new SettingEntry();
            e.label = l; e.isToggle = true;
            e.getValue = get; e.setValue = set; return e;
        }
        String formatValue(float v) {
            if (isPercent) return Math.round(v * 100) + "%";
            if (max - min <= 10 && v != Math.floor(v)) return String.format("%.2f", v);
            return String.valueOf(Math.round(v));
        }
    }
}