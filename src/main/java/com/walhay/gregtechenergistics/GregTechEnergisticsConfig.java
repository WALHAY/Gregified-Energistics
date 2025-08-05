package com.walhay.gregtechenergistics;

import net.minecraftforge.common.config.Config;

@Config(modid = GregTechEnergisticsMod.MOD_ID)
public class GregTechEnergisticsConfig {

	@Config.Name("ME Hatch Update Time")
	@Config.RangeInt(min = 100, max = 600)
	public static int ME_AL_HATCH_UPDATE_TIME = 300;

	public static int getUpdateTime() {
		return ME_AL_HATCH_UPDATE_TIME;
	}
}
