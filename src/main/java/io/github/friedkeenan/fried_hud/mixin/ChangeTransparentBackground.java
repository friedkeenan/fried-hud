package io.github.friedkeenan.fried_hud.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;

@Mixin(Screen.class)
public class ChangeTransparentBackground {
    private static final int START_COLOR = 0xE0101010;
    private static final int END_COLOR   = 0xF0101010;

    @WrapOperation(
    at = @At(
        value  = "INVOKE",
        target = "Lnet/minecraft/client/gui/GuiGraphics;fillGradient(IIIIII)V"
    ),

        method = "renderTransparentBackground"
    )
    private void lightenBackground(GuiGraphics graphics, int x, int y, int width, int height, int start_color, int end_color, Operation<Void> original) {
        original.call(graphics, x, y, width, height, START_COLOR, END_COLOR);
    }
}
