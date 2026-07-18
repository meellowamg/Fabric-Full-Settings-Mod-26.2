package net.meellowamg.moresettingsmod.mixin;

import net.meellowamg.moresettingsmod.MoreSettingsScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PauseScreen.class)
public class MixinPauseScreen {

    @Shadow protected Minecraft minecraft;
    @Shadow protected int width;
    @Shadow protected int height;

    @Shadow
    protected <T extends GuiEventListener & Renderable & NarratableEntry> T addRenderableWidget(T widget) {
        throw new AssertionError();
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        PauseScreen self = (PauseScreen)(Object)this;
        // Place button below "Save and Quit to Title"
        // In 26.2 the last button is at height/4 + 116, so we go below that
        addRenderableWidget(Button.builder(
                Component.literal("Full Settings"),
                btn -> minecraft.gui.setScreen(new MoreSettingsScreen(self))
        ).bounds(width / 2 - 102, height / 4 + 145, 204, 20).build());
    }
}