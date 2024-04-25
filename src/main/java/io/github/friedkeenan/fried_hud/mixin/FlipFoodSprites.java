package io.github.friedkeenan.fried_hud.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.github.friedkeenan.fried_hud.FriedHUDMod;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;

@Mixin(TextureAtlasSprite.class)
public class FlipFoodSprites {
    @Shadow
    @Final
    @Mutable
    private float u0;

    @Shadow
    @Final
    @Mutable
    private float u1;

    @Inject(at = @At("TAIL"), method = "<init>")
    private void flipFoodSprite(ResourceLocation atlas, SpriteContents sprite, int i, int j, int k, int l, CallbackInfo info) {
        if (!(
            sprite.name().equals(FriedHUDMod.FOOD_EMPTY_HUNGER_SPRITE) ||
            sprite.name().equals(FriedHUDMod.FOOD_HALF_HUNGER_SPRITE)  ||
            sprite.name().equals(FriedHUDMod.FOOD_FULL_HUNGER_SPRITE)  ||
            sprite.name().equals(FriedHUDMod.FOOD_EMPTY_SPRITE)        ||
            sprite.name().equals(FriedHUDMod.FOOD_HALF_SPRITE)         ||
            sprite.name().equals(FriedHUDMod.FOOD_FULL_SPRITE)
        )) {
            return;
        }

        final var tmp = this.u0;

        this.u0 = this.u1;
        this.u1 = tmp;
    }
}
