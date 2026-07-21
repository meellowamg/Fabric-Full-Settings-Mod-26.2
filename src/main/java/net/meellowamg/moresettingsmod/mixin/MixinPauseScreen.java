package net.meellowamg.moresettingsmod.mixin;

import net.meellowamg.moresettingsmod.MoreSettingsScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public class MixinPauseScreen {

    @Shadow protected int width;
    @Shadow protected int height;

    @Shadow
    protected <T extends GuiEventListener & Renderable & NarratableEntry> T addRenderableWidget(T widget) {
        throw new AssertionError();
    }

    @Inject(method = "init(Lnet/minecraft/client/Minecraft;II)V", at = @At("TAIL"))
    private void onInit(Minecraft minecraft, int width, int height, CallbackInfo ci) {
        if (!((Object)this instanceof PauseScreen)) return;

        addRenderableWidget(Button.builder(
                Component.literal("Full Settings"),
                btn -> minecraft.gui.setScreen(new MoreSettingsScreen((Screen)(Object)this))
        ).bounds(width / 2 - 102, height / 4 + 155, 204, 20).build());
    }
}