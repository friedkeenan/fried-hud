package io.github.friedkeenan.fried_hud.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.github.friedkeenan.fried_hud.HUDManager;
import net.minecraft.client.gui.screens.ReceivingLevelScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

@Mixin(ReceivingLevelScreen.class)
public abstract class ShowHudOnSpawn extends Screen {
    private static final int ELEMENTS_SPAWN_SHOW_TIME = 30;

    protected ShowHudOnSpawn(Component component) {
        super(component);
    }

    @Inject(at = @At("TAIL"), method = "onClose")
    private void showHud(CallbackInfo info) {
        final var manager = (HUDManager) this.minecraft.gui;

        /* NOTE: We do not show vehicle elements here. */
        manager.showHealthAndHotbarFor(ELEMENTS_SPAWN_SHOW_TIME);
    }
}
