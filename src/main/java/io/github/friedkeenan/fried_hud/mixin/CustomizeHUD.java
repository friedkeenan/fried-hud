package io.github.friedkeenan.fried_hud.mixin;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import com.mojang.blaze3d.systems.RenderSystem;

import io.github.friedkeenan.fried_hud.FriedHUDMod;
import io.github.friedkeenan.fried_hud.HUDManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import net.minecraft.client.gui.screens.ReceivingLevelScreen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.HorseInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PlayerRideableJumping;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.HitResult;

@Mixin(Gui.class)
public class CustomizeHUD implements HUDManager {
    private static final int   VERTICAL_SPEED = 2;
    private static final float FADE_OUT_TIME  = 10.0f;

    private static final int HOTBAR_HEIGHT = 23;

    private static final int HOTBAR_SHOW_TIME = 30;

    private static final int FOOD_LEVEL_DAMAGED_WARN   = 17;
    private static final int FOOD_LEVEL_UNDAMAGED_WARN = 10;

    private static final int HEALTH_SCREEN_SHOW_TIME    = 30;
    private static final int HEALTH_CONSUMING_SHOW_TIME = 40;
    private static final int HEALTH_HUNGER_SHOW_TIME    = 50;
    private static final int HEALTH_LOSE_FOOD_SHOW_TIME = 50;
    private static final int HEALTH_FREEZING_SHOW_TIME  = 40;
    private static final int HEALTH_DAMAGE_SHOW_TIME    = 40;
    private static final int HEALTH_REGEN_SHOW_TIME     = 50;
    private static final int HEALTH_AIR_SHOW_TIME       = 40;

    private static final int VEHICLE_HEALTH_SCREEN_SHOW_TIME       = 30;
    private static final int VEHICLE_HEALTH_START_RIDING_SHOW_TIME = 50;

    private static final int HEALTH_X = 1;
    private static final int HEALTH_Y = 1;

    private static final int FOOD_SIZE  = 9;
    private static final int HEART_SIZE = 9;
    private static final int AIR_SIZE   = 9;

    private static final int SPRITES_PER_ROW = 10;

    private static final int JUMP_METER_SHOW_TIME = 30;

    @Shadow
    private int tickCount;

    @Shadow
    @Final
    private RandomSource random;

    @Shadow
    @Final
    private Minecraft minecraft;

    @Shadow
    @Final
    private DebugScreenOverlay debugOverlay;

    private int hotbar_show_time = 0;
    private int prev_selected    = -1;

    private int health_show_time  = 0;
    private int last_food_level   = 0;
    private int last_total_health = 0;

    private int vehicle_health_show_time = 0;

    @Nullable
    private LivingEntity last_vehicle = null;

    private int jump_meter_show_time = 0;

    private boolean forcing_health_render = false;

    @WrapWithCondition(
        at = @At(
            value  = "INVOKE",
            target = "Lnet/minecraft/client/gui/Gui;renderArmor(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/world/entity/player/Player;IIII)V"
        ),

        method = "renderPlayerHealth"
    )
    private boolean removeArmorStatRendering(GuiGraphics graphics, Player player, int i, int j, int k, int l) {
        return false;
    }

    @Shadow
    @Nullable
    private Player getCameraPlayer() {
        throw new AssertionError();
    }

    private void showHotbarFor(int time) {
        this.hotbar_show_time = Math.max(this.hotbar_show_time, this.tickCount + time);
    }

    private void showHealthFor(int time) {
        this.health_show_time = Math.max(this.health_show_time, this.tickCount + time);
    }

    private void showVehicleHealthFor(int time) {
        this.vehicle_health_show_time = Math.max(this.vehicle_health_show_time, this.tickCount + time);
    }

    private void showJumpMeterForTime(int time) {
        this.jump_meter_show_time = Math.max(this.jump_meter_show_time, this.tickCount + time);
    }

    @Override
    public void showHealthAndHotbarFor(int time) {
        this.showHotbarFor(time);
        this.showHealthFor(time);
        this.showVehicleHealthFor(time);
    }

    private boolean shouldCurrentScreenShowElements() {
        return (
            this.minecraft.screen instanceof InventoryScreen ||

            this.minecraft.screen instanceof HorseInventoryScreen ||

            this.minecraft.screen instanceof CreativeModeInventoryScreen
        );
    }

    private boolean shouldCurrentScreenShowHotbar() {
        return (
            this.minecraft.screen instanceof AbstractContainerScreen ||

            this.shouldCurrentScreenShowElements()
        );
    }

    private int heightOffsetForTime(int time) {
        return Math.max(0, VERTICAL_SPEED * (this.tickCount - time));
    }

    private float alphaForTime(int time) {
        final var difference = time - this.tickCount;
        if (difference >= 0) {
            return 1.0f;
        }

        return 1.0f - Mth.clamp(-difference / FADE_OUT_TIME, 0.0f, 1.0f);
    }

    @WrapWithCondition(
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/Gui;renderItemHotbar(Lnet/minecraft/client/gui/GuiGraphics;F)V"
        ),

        method = "renderHotbarAndDecorations"
    )
    private boolean disableHotbarRendering(Gui gui, GuiGraphics graphics, float delta) {
        /*
            NOTE: We could mimic the spectator hotbar behavior,
            but it's not worth it and it looks worse I think.
        */

        @Nullable final var player = this.getCameraPlayer();
        if (player == null) {
            return false;
        }

        if (this.shouldCurrentScreenShowHotbar()) {
            this.showHotbarFor(HOTBAR_SHOW_TIME);

            return true;
        }

        if (this.prev_selected < 0) {
            this.prev_selected = player.getInventory().selected;
        }

        if (player.getInventory().selected != this.prev_selected) {
            this.prev_selected = player.getInventory().selected;

            this.showHotbarFor(HOTBAR_SHOW_TIME);

            return true;
        }

        if (this.heightOffsetForTime(this.hotbar_show_time) < HOTBAR_HEIGHT) {
            return true;
        }

        return false;
    }

    @Inject(at = @At("HEAD"), method = "renderItemHotbar")
    private void moveHotbar(GuiGraphics graphics, float delta, CallbackInfo info) {
        graphics.pose().pushPose();

        graphics.pose().translate(0, this.heightOffsetForTime(this.hotbar_show_time), 0);
    }

    @Inject(at = @At("RETURN"), method = "renderItemHotbar")
    private void cleanupMoveHotbar(GuiGraphics graphics, float delta, CallbackInfo info) {
        graphics.pose().popPose();
    }

    @ModifyReturnValue(at = @At("RETURN"), method = "isExperienceBarVisible")
    private boolean removeExperienceBar(boolean original) {
        return false;
    }

    @ModifyExpressionValue(
        at = @At(
            value  = "INVOKE",
            target = "Lnet/minecraft/client/gui/Gui;getPlayerVehicleWithHealth()Lnet/minecraft/world/entity/LivingEntity;"
        ),

        method = "renderPlayerHealth"
    )
    @Nullable
    private LivingEntity alwaysAttemptRenderFood(LivingEntity original) {
        return null;
    }

    @Shadow
    private void renderPlayerHealth(GuiGraphics graphics) {
        throw new AssertionError();
    }

    @Shadow
    private void renderVehicleHealth(GuiGraphics graphics) {
        throw new AssertionError();
    }

    @Override
    public void forceRenderHealth(GuiGraphics graphics) {
        this.forcing_health_render = true;

        try {
            if (this.minecraft.gameMode.canHurtPlayer()) {
                this.renderPlayerHealth(graphics);
            }

            this.renderVehicleHealth(graphics);
        } finally {
            this.forcing_health_render = false;
        }
    }

    @Inject(at = @At("HEAD"), method = "renderPlayerHealth", cancellable = true)
    private void updateHealthVisible(GuiGraphics graphics, CallbackInfo info) {
        if (this.shouldCurrentScreenShowElements()) {
            this.showHealthFor(HEALTH_SCREEN_SHOW_TIME);

            /*
                NOTE: We don't render the health in the inventory until
                we've been forced to in order to not double-render the
                health, which could be a problem with the randomness involved.
            */
            if (!this.forcing_health_render) {
                info.cancel();

                return;
            }
        }

        @Nullable final var player = this.getCameraPlayer();
        if (player == null) {
            return;
        }

        if (player.getTicksFrozen() > 0) {
            this.showHealthFor(HEALTH_FREEZING_SHOW_TIME);
        }

        final var consuming_item = (
            player.isUsingItem() &&

            (
                player.getUseItem().getUseAnimation() == UseAnim.EAT ||
                player.getUseItem().getUseAnimation() == UseAnim.DRINK
            )
        );

        if (consuming_item) {
            this.showHealthFor(HEALTH_CONSUMING_SHOW_TIME);
        }

        if (player.hasEffect(MobEffects.HUNGER)) {
            this.showHealthFor(HEALTH_HUNGER_SHOW_TIME);
        }

        final var food_level = player.getFoodData().getFoodLevel();

        /* NOTE: We don't take into account if max health changes. */
        final var health       = Mth.ceil(player.getHealth());
        final var total_health = health + Mth.ceil(player.getAbsorptionAmount());

        /* NOTE: This is done to eliminate confusion about entering a level/different world. */
        if (this.minecraft.screen instanceof ReceivingLevelScreen) {
            this.last_food_level   = food_level;
            this.last_total_health = total_health;
        }

        if (
            food_level < this.last_food_level && (
                food_level <= FOOD_LEVEL_UNDAMAGED_WARN ||

                (health < player.getMaxHealth() && food_level <= FOOD_LEVEL_DAMAGED_WARN)
            )
        ) {
            this.showHealthFor(HEALTH_LOSE_FOOD_SHOW_TIME);
        }

        this.last_food_level = food_level;

        /* NOTE: This is done to eliminate confusion about respawning. */
        if (this.last_total_health <= 0) {
            this.last_total_health = total_health;
        }

        if (total_health < this.last_total_health) {
            this.showHealthFor(HEALTH_DAMAGE_SHOW_TIME);
        } else if (total_health > this.last_total_health) {
            this.showHealthFor(HEALTH_REGEN_SHOW_TIME);
        }

        this.last_total_health = total_health;

        final var would_lose_breath = (
            player.isEyeInFluid(FluidTags.WATER) &&

            !player.level().getBlockState(
                BlockPos.containing(player.getX(), player.getEyeY(), player.getZ())
            ).is(Blocks.BUBBLE_COLUMN)
        );

        if (would_lose_breath) {
            final var can_breathe_underwater = (
                player.canBreatheUnderwater() || MobEffectUtil.hasWaterBreathing(player) || player.getAbilities().invulnerable
            );

            if (!can_breathe_underwater) {
                this.showHealthFor(HEALTH_AIR_SHOW_TIME);
            }
        } else if (player.getAirSupply() < player.getMaxAirSupply()) {
            this.showHealthFor(HEALTH_AIR_SHOW_TIME);
        }
    }

    private int getFoodY() {
        return HEALTH_Y - this.heightOffsetForTime(this.health_show_time);
    }

    private void invertedRenderFood(GuiGraphics graphics, Player player, int x, int y) {
        final var food_data  = player.getFoodData();
        final var food_level = food_data.getFoodLevel();

        RenderSystem.enableBlend();

        for (int i = 0; i < SPRITES_PER_ROW; ++i) {
            var real_y = y;
            if (food_data.getSaturationLevel() <= 0.0f && this.tickCount % (3 * food_level + 1) == 0) {
                real_y += this.random.nextInt(3) - 1;
            }

            final ResourceLocation full_food;
            final ResourceLocation half_food;
            final ResourceLocation empty_food;
            if (player.hasEffect(MobEffects.HUNGER)) {
                full_food  = FriedHUDMod.FOOD_FULL_HUNGER_SPRITE;
                half_food  = FriedHUDMod.FOOD_HALF_HUNGER_SPRITE;
                empty_food = FriedHUDMod.FOOD_EMPTY_HUNGER_SPRITE;
            } else {
                full_food  = FriedHUDMod.FOOD_FULL_SPRITE;
                half_food  = FriedHUDMod.FOOD_HALF_SPRITE;
                empty_food = FriedHUDMod.FOOD_EMPTY_SPRITE;
            }

            final var real_x = x + i * (FOOD_SIZE - 1);

            graphics.blitSprite(empty_food, real_x, real_y, FOOD_SIZE, FOOD_SIZE);

            final var half_drawn_food_level = i * 2 + 1;

            if (half_drawn_food_level == food_level) {
                graphics.blitSprite(half_food, real_x, real_y, FOOD_SIZE, FOOD_SIZE);
            } else if (half_drawn_food_level < food_level) {
                graphics.blitSprite(full_food, real_x, real_y, FOOD_SIZE, FOOD_SIZE);
            }
        }

        RenderSystem.disableBlend();
    }

    @WrapOperation(
        at = @At(
            value  = "INVOKE",
            target = "Lnet/minecraft/client/gui/Gui;renderFood(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/world/entity/player/Player;II)V"
        ),

        method = "renderPlayerHealth"
    )
    private void moveFood(Gui gui, GuiGraphics graphics, Player player, int y, int x, Operation<Void> original) {
        final var food_y = this.getFoodY();

        if (food_y + FOOD_SIZE < 0) {
            return;
        }

        this.invertedRenderFood(graphics, player, HEALTH_X, food_y);
    }

    private int getHeartsY(float max_health, int absorption_health, int vertical_spacing) {
        final var additional_rows = (Mth.ceil(max_health / 2.0) + Mth.ceil(absorption_health / 2.0) - 1) / SPRITES_PER_ROW;

        return this.getFoodY() + FOOD_SIZE + 1 + additional_rows * vertical_spacing;
    }

    @WrapOperation(
        at = @At(
            value  = "INVOKE",
            target = "Lnet/minecraft/client/gui/Gui;renderHearts(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/world/entity/player/Player;IIIIFIIIZ)V"
        ),

        method = "renderPlayerHealth"
    )
    private void moveHearts(
        Gui gui,
        GuiGraphics graphics,
        Player player,
        int x,
        int y,
        int vertical_spacing,
        int bump_index,
        float max_health,
        int health,
        int display_health,
        int absorption_health,
        boolean blinking,

        Operation<Void> original
    ) {
        final var hearts_y = this.getHeartsY(max_health, absorption_health, vertical_spacing);
        if (hearts_y + HEART_SIZE < 0) {
            return;
        }

        original.call(
            gui,
            graphics,
            player,
            HEALTH_X,
            hearts_y,
            vertical_spacing,
            bump_index,
            max_health,
            health,
            display_health,
            absorption_health,
            blinking
        );
    }

    private int getAirY(float max_health, int absorption_health, int vertical_spacing) {
        return this.getHeartsY(max_health, absorption_health, vertical_spacing) + HEART_SIZE + 1;
    }

    private void invertedRenderAir(GuiGraphics graphics, int x, int y, int displayed_air, int max_air) {
        final var intact_bubbles = Mth.ceil(((double) SPRITES_PER_ROW * (displayed_air - 2)) / max_air);
        final var popped_bubbles = Mth.ceil(((double) SPRITES_PER_ROW * displayed_air) / max_air) - intact_bubbles;

        RenderSystem.enableBlend();

        for (int i = 0; i < intact_bubbles + popped_bubbles; ++i) {
            final var real_x = x + i * (AIR_SIZE - 1);

            if (i < intact_bubbles) {
                graphics.blitSprite(FriedHUDMod.AIR_SPRITE, real_x, y, AIR_SIZE, AIR_SIZE);
            } else {
                graphics.blitSprite(FriedHUDMod.AIR_BURSTING_SPRITE, real_x, y, AIR_SIZE, AIR_SIZE);
            }
        }

        RenderSystem.disableBlend();
    }

    @Inject(
        at = @At(
            value  = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/Player;getMaxAirSupply()I"
        ),

        method      = "renderPlayerHealth",
        cancellable = true
    )
    private void moveAirBubbles(
        GuiGraphics graphics, CallbackInfo info,

        @Local float max_health,
        @Local(ordinal = 5) int absorption_health,
        @Local(ordinal = 7) int vertical_spacing
    ) {
        final var air_y = this.getAirY(max_health, absorption_health, vertical_spacing);
        if (air_y + AIR_SIZE >= 0) {
            /* NOTE: At this point, we know this is not null. */
            final var player = this.getCameraPlayer();

            final var max_air       = player.getMaxAirSupply();
            final var displayed_air = Math.min(player.getAirSupply(), max_air);

            if (player.isEyeInFluid(FluidTags.WATER) || displayed_air < max_air) {
                this.invertedRenderAir(graphics, HEALTH_X, air_y, displayed_air, max_air);
            }
        }

        this.minecraft.getProfiler().pop();
        info.cancel();
    }

    private int getVehicleHeartsY(GuiGraphics graphics) {
        return graphics.guiHeight() - HEART_SIZE - HEALTH_Y + this.heightOffsetForTime(this.vehicle_health_show_time);
    }

    @Shadow
    @Nullable
    private LivingEntity getPlayerVehicleWithHealth() {
        throw new AssertionError();
    }

    @Inject(at = @At("HEAD"), method = "renderVehicleHealth", cancellable = true)
    private void updateVehicleHealthVisible(GuiGraphics graphics, CallbackInfo info) {
        if (this.shouldCurrentScreenShowElements()) {
            this.showVehicleHealthFor(VEHICLE_HEALTH_SCREEN_SHOW_TIME);

            /*
                NOTE: We don't render the health in the inventory until
                we've been forced to in order to not double-render it.
            */
            if (!this.forcing_health_render) {
                info.cancel();

                return;
            }
        }

        @Nullable final var vehicle = this.getPlayerVehicleWithHealth();

        /* NOTE: This is done to eliminate confusion about entering a level/different world. */
        if (this.minecraft.screen instanceof ReceivingLevelScreen) {
            this.last_vehicle = vehicle;
        }

        if (vehicle != this.last_vehicle && vehicle != null) {
            this.showVehicleHealthFor(VEHICLE_HEALTH_START_RIDING_SHOW_TIME);
        }

        this.last_vehicle = vehicle;
    }

    @Inject(
        at = @At(
            value  = "INVOKE",
            target = "Lcom/mojang/blaze3d/systems/RenderSystem;enableBlend()V"
        ),

        method = "renderVehicleHealth",

        cancellable = true
    )
    private void adjustVehicleHealth(
        GuiGraphics graphics, CallbackInfo info,

        @Local(ordinal = 0) int max_hearts,
        @Local(ordinal = 3) LocalIntRef x,
        @Local(ordinal = 4) LocalIntRef y
    ) {
        final var hearts_y = this.getVehicleHeartsY(graphics);

        final var rows = Mth.ceil(max_hearts / (double) SPRITES_PER_ROW);

        if (hearts_y - (HEART_SIZE + 1) * (rows - 1) > graphics.guiHeight()) {
            info.cancel();
        }

        x.set(graphics.guiWidth()  - HEALTH_X);
        y.set(hearts_y);
    }

    private boolean shouldShowDebugCrosshair() {
        return (
            this.debugOverlay.showDebugScreen() &&

            !this.minecraft.player.isReducedDebugInfo() &&

            !this.minecraft.options.reducedDebugInfo().get().booleanValue()
        );
    }

    private static boolean ShouldShowCrossHairForAnim(UseAnim anim) {
        /*
            NOTE: We exclude certain animations in case other mods
            maybe define other animations that warrant a crosshair.
        */

        return (
            anim != UseAnim.NONE      &&
            anim != UseAnim.EAT       &&
            anim != UseAnim.DRINK     &&
            anim != UseAnim.CROSSBOW  &&
            anim != UseAnim.SPYGLASS  &&
            anim != UseAnim.TOOT_HORN &&
            anim != UseAnim.BRUSH
        );
    }

    @Inject(at = @At("HEAD"), method = "renderCrosshair", cancellable = true)
    private void removeCrosshair(GuiGraphics graphics, float delta, CallbackInfo info) {
        if (this.shouldShowDebugCrosshair()) {
            return;
        }

        if (this.minecraft.hitResult != null && this.minecraft.hitResult.getType() != HitResult.Type.MISS) {
            return;
        }

        @Nullable final var player = this.getCameraPlayer();
        if (player == null) {
            return;
        }

        if (player.isUsingItem() && ShouldShowCrossHairForAnim(player.getUseItem().getUseAnimation())) {
            return;
        }

        if (player.isHolding(item -> item.is(Items.CROSSBOW) && CrossbowItem.isCharged(item))) {
            return;
        }

        info.cancel();
    }

    @Inject(
        at = @At(
            value  = "INVOKE",
            target = "Lcom/mojang/blaze3d/systems/RenderSystem;enableBlend()V",
            shift  = At.Shift.AFTER
        ),

        method = "renderJumpMeter"
    )
    private void setJumpMeterAlpha(PlayerRideableJumping vehicle, GuiGraphics graphics, int x, CallbackInfo info) {
        if (this.minecraft.player.getJumpRidingScale() > 0) {
            this.showJumpMeterForTime(JUMP_METER_SHOW_TIME);
        }

        if (vehicle.getJumpCooldown() > 0) {
            this.showJumpMeterForTime(JUMP_METER_SHOW_TIME);
        }

        graphics.setColor(1.0f, 1.0f, 1.0f, this.alphaForTime(this.jump_meter_show_time));
    }

    @Inject(
        at = @At(
            value  = "INVOKE",
            target = "Lcom/mojang/blaze3d/systems/RenderSystem;disableBlend()V",
            shift  = At.Shift.BEFORE
        ),

        method = "renderJumpMeter"
    )
    private void unsetJumpMeterAlpha(PlayerRideableJumping vehicle, GuiGraphics graphics, int x, CallbackInfo info) {
        graphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
    }
}
