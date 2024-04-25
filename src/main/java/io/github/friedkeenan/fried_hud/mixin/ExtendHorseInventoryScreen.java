package io.github.friedkeenan.fried_hud.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.github.friedkeenan.fried_hud.InventoryExtensions;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.HorseInventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.HorseInventoryMenu;

@Mixin(HorseInventoryScreen.class)
public abstract class ExtendHorseInventoryScreen extends AbstractContainerScreen<HorseInventoryMenu> {
    public ExtendHorseInventoryScreen(HorseInventoryMenu abstractContainerMenu, Inventory inventory, Component component) {
        super(abstractContainerMenu, inventory, component);
    }

    @Inject(at = @At("HEAD"), method = "renderBg")
    private void renderExtensions(GuiGraphics graphics, float delta, int mouse_x, int mouse_y, CallbackInfo info) {
        InventoryExtensions.RenderExtensions(graphics, this.minecraft, this.leftPos, this.topPos, this.imageWidth);
    }
}
