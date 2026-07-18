package net.meellowamg.moresettingsmod;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class MoreSettingsScreen extends Screen {

    private final Screen parent;

    public static final String CAT_SOUND         = "Sound";
    public static final String CAT_VIDEO         = "Video";
    public static final String CAT_CHAT          = "Chat";
    public static final String CAT_GAMEPLAY      = "Gameplay";
    public static final String CAT_ACCESSIBILITY = "Accessibility";

    public MoreSettingsScreen(Screen parent) {
        super(Component.literal("Full Settings"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int cx    = this.width / 2;
        int btnW  = 180;
        int btnH  = 40;
        int gap   = 10;
        int startY = 60;

        addCategoryButton(CAT_SOUND,         cx - btnW - gap / 2, startY,                       btnW, btnH);
        addCategoryButton(CAT_VIDEO,         cx + gap / 2,         startY,                       btnW, btnH);
        addCategoryButton(CAT_CHAT,          cx - btnW - gap / 2, startY + btnH + gap,           btnW, btnH);
        addCategoryButton(CAT_GAMEPLAY,      cx + gap / 2,         startY + btnH + gap,           btnW, btnH);
        addCategoryButton(CAT_ACCESSIBILITY, cx - btnW / 2,        startY + (btnH + gap) * 2,    btnW, btnH);

        this.addRenderableWidget(Button.builder(
                Component.literal("Back"),
                btn -> this.minecraft.gui.setScreen(parent)
        ).bounds(cx - 75, this.height - 32, 150, 20).build());
    }

    private void addCategoryButton(String category, int x, int y, int w, int h) {
        this.addRenderableWidget(Button.builder(
                Component.literal(getCategoryIcon(category) + "  " + category),
                btn -> this.minecraft.gui.setScreen(new MoreSettingsCategoryScreen(this, category))
        ).bounds(x, y, w, h).build());
    }

    private String getCategoryIcon(String cat) {
        return switch (cat) {
            case CAT_SOUND         -> "♪";
            case CAT_VIDEO         -> "◉";
            case CAT_CHAT          -> "✉";
            case CAT_GAMEPLAY      -> "◈";
            case CAT_ACCESSIBILITY -> "✦";
            default -> "•";
        };
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        graphics.fill(0, 0, this.width, this.height, 0xCC000000);
        graphics.fill(0, 0, this.width, 24, 0xFF111111);
        graphics.text(this.font, "Full Settings", this.width / 2, 7, 0xFFFFFF, true);
        graphics.text(this.font, "Select a category", this.width / 2, 46, 0xAAAAAA, true);
        super.extractRenderState(graphics, mouseX, mouseY, delta);
    }

    @Override
    public void onClose() {
        this.minecraft.gui.setScreen(parent);
    }
}