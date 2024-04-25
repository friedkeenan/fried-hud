package io.github.friedkeenan.fried_hud;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.resources.ResourceLocation;

public class FriedHUDMod implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("fried_hud");

    public static final ResourceLocation FOOD_EMPTY_HUNGER_SPRITE = new ResourceLocation("minecraft:hud/food_empty_hunger");
    public static final ResourceLocation FOOD_HALF_HUNGER_SPRITE  = new ResourceLocation("minecraft:hud/food_half_hunger");
    public static final ResourceLocation FOOD_FULL_HUNGER_SPRITE  = new ResourceLocation("minecraft:hud/food_full_hunger");
    public static final ResourceLocation FOOD_EMPTY_SPRITE        = new ResourceLocation("minecraft:hud/food_empty");
    public static final ResourceLocation FOOD_HALF_SPRITE         = new ResourceLocation("minecraft:hud/food_half");
    public static final ResourceLocation FOOD_FULL_SPRITE         = new ResourceLocation("minecraft:hud/food_full");

    public static final ResourceLocation AIR_SPRITE          = new ResourceLocation("minecraft:hud/air");
    public static final ResourceLocation AIR_BURSTING_SPRITE = new ResourceLocation("minecraft:hud/air_bursting");

    public static final int NEEDED_VEHICLE_HEALTH_PADDING = 1;

    @Override
    public void onInitializeClient() {
        LOGGER.info("fried_hud initialized!");
    }
}
