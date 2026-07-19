package net.meellowamg.moresettingsmod.mixin;

import net.meellowamg.moresettingsmod.MoreSettingsScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(targets = "net.minecraft.client.gui.screens.options.OptionsScreen")
public class MixinOptionsScreen {

    @Shadow protected Minecraft minecraft;
    @Shadow protected int width;
    @Shadow protected int height;

    @Shadow
    public List<? extends GuiEventListener> children() {
        throw new AssertionError();
    }

    @Shadow
    protected <T extends GuiEventListener & Renderable & NarratableEntry> T addRenderableWidget(T widget) {
        throw new AssertionError();
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        Screen self = (Screen)(Object)this;

        int lowestY = height / 2;
        for (GuiEventListener child : children()) {
            if (child instanceof Button btn) {
                int btnBottom = btn.getY() + btn.getHeight();
                if (btnBottom > lowestY) lowestY = btnBottom;
            }
        }

        addRenderableWidget(Button.builder(
                Component.literal("Full Settings"),
                btn -> minecraft.gui.setScreen(new MoreSettingsScreen(self))
        ).bounds(width / 2 - 102, lowestY + 5, 204, 20).build());
    }
}