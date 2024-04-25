package io.github.friedkeenan.fried_hud.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;

import io.github.friedkeenan.fried_hud.FriedHUDMod;
import net.minecraft.client.gui.components.ChatComponent;

@Mixin(ChatComponent.class)
public class MoveChatComponent {
    @WrapOperation(
        at = @At(
            value   = "INVOKE",
            target  = "Lcom/mojang/blaze3d/vertex/PoseStack;translate(FFF)V",
            ordinal = 0
        ),

        method = "render"
    )
    private void moveUpward(PoseStack pose, float x, float y, float z, Operation<Void> original) {
        original.call(pose, x, y - FriedHUDMod.NEEDED_VEHICLE_HEALTH_PADDING, z);
    }
}
