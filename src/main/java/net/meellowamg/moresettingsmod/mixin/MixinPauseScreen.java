package net.meellowamg.moresettingsmod.mixin;

import net.meellowamg.moresettingsmod.MoreSettingsScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PauseScreen.class)
public class MixinPauseScreen {

    @Inject(method = "init", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        PauseScreen self = (PauseScreen)(Object)this;
        self.addRenderableWidget(Button.builder(
                Component.literal("Full Settings"),
                btn -> self.minecraft.gui.setScreen(new MoreSettingsScreen(self))
        ).bounds(self.width / 2 - 102, self.height / 4 + 155, 204, 20).build());
    }
}