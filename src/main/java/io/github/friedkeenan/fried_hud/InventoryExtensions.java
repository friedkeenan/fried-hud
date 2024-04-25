package io.github.friedkeenan.fried_hud;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public class InventoryExtensions {
    private static final ResourceLocation EXPERIENCE_BAR_BACKGROUND_SPRITE = new ResourceLocation("minecraft:hud/experience_bar_background");
    private static final ResourceLocation EXPERIENCE_BAR_PROGRESS_SPRITE   = new ResourceLocation("minecraft:hud/experience_bar_progress");

    private static final int EXPERIENCE_BAR_WIDTH  = 182;
    private static final int EXPERIENCE_BAR_HEIGHT = 5;

    private static final int EXPERIENCE_LEVEL_HEIGHT_OFFSET = 6;
    private static final int EXPERIENCE_LEVEL_COLOR = 0x80FF20;

    public static void RenderExtensions(GuiGraphics graphics, Minecraft minecraft, int left_pos, int top_pos, int width) {
        final var manager = (HUDManager) minecraft.gui;

        manager.forceRenderHealth(graphics);

        if (minecraft.gameMode.hasExperience()) {
            RenderExperienceBar(graphics, minecraft, left_pos, top_pos, width);
            RenderExperienceLevel(graphics, minecraft, left_pos, top_pos, width);
        }
    }

    private static void RenderExperienceBar(GuiGraphics graphics, Minecraft minecraft, int left_pos, int top_pos, int width) {
        minecraft.getProfiler().push("expBar");

        if (minecraft.player.getXpNeededForNextLevel() > 0) {
            final var progress = (int) (minecraft.player.experienceProgress * ((float) EXPERIENCE_BAR_WIDTH + 1));

            final var x = left_pos + (width / 2) - (EXPERIENCE_BAR_WIDTH / 2);
            final var y = top_pos - EXPERIENCE_BAR_HEIGHT - 1;

            RenderSystem.enableBlend();

            graphics.blitSprite(EXPERIENCE_BAR_BACKGROUND_SPRITE, x, y, EXPERIENCE_BAR_WIDTH, EXPERIENCE_BAR_HEIGHT);

            if (progress > 0) {
                graphics.blitSprite(
                    EXPERIENCE_BAR_PROGRESS_SPRITE,
                    EXPERIENCE_BAR_WIDTH,
                    EXPERIENCE_BAR_HEIGHT,
                    0,
                    0,
                    x,
                    y,
                    progress,
                    EXPERIENCE_BAR_HEIGHT
                );
            }

            RenderSystem.disableBlend();
        }

        minecraft.getProfiler().pop();
    }

    private static void RenderExperienceLevel(GuiGraphics graphics, Minecraft minecraft, int left_pos, int top_pos, int width) {
        final var level = minecraft.player.experienceLevel;

        if (level > 0) {
            minecraft.getProfiler().push("expLevel");

            final var level_text = "" + level;

            final var x = left_pos + (width - minecraft.font.width(level_text)) / 2;
            final var y = top_pos - EXPERIENCE_BAR_HEIGHT - 1 - EXPERIENCE_LEVEL_HEIGHT_OFFSET;

            /* Render outline. */
            graphics.drawString(minecraft.font, level_text, x + 1, y + 0, 0x000000, false);
            graphics.drawString(minecraft.font, level_text, x - 1, y - 0, 0x000000, false);
            graphics.drawString(minecraft.font, level_text, x + 0, y + 1, 0x000000, false);
            graphics.drawString(minecraft.font, level_text, x - 0, y - 1, 0x000000, false);

            /* Render colored text. */
            graphics.drawString(minecraft.font, level_text, x, y, EXPERIENCE_LEVEL_COLOR, false);

            minecraft.getProfiler().pop();
        }
    }
}
