package com.walhay.gregifiedenergistics.client.gui;

import com.walhay.gregifiedenergistics.GregifiedEnergisticsMod;
import gregtech.api.gui.resources.TextureArea;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GregifiedEnergisticsGuiTextures {
	public static final TextureArea BLOCKING_MODE = fullImage("textures/gui/blocking_mode.png");
	public static final TextureArea FLUID_MODE = fullImage("textures/gui/fluid_mode.png");

	public static TextureArea fullImage(String path) {
		return new TextureArea(GregifiedEnergisticsMod.gregifiedEnergisticsId(path), 0.0, 0.0, 1.0, 1.0);
	}
}
