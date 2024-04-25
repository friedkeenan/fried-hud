package io.github.friedkeenan.fried_hud;

import net.minecraft.client.gui.GuiGraphics;

public interface HUDManager {
    public void forceRenderHealth(GuiGraphics graphics);

    public void showHealthAndHotbarFor(int time);
}
