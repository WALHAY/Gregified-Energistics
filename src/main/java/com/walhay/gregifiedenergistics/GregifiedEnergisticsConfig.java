package com.walhay.gregifiedenergistics;

import net.minecraftforge.common.config.Config;

@Config(modid = GregifiedEnergisticsMod.MOD_ID)
public class GregifiedEnergisticsConfig {

	@Config.Name(value = "Ore Prefix To Expose")
	@Config.Comment(value = "Ore Prefixes used in substitutions so they will not work if ore prefix is not exposed")
	@Config.RequiresMcRestart
	public static String[] exposeOrePrefix = new String[] {"circuit"};

	@Config.Name(value = "Pattern Handler Size")
	@Config.Comment(value = "Slots amoun in ME Assembly Line Bus")
	@Config.RangeInt(min = 1)
	public static int patternHandlerSize = 36;
}
