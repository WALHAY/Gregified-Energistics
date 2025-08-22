package com.walhay.gregifiedenergistics.api.util;

import com.walhay.gregifiedenergistics.GregifiedEnergisticsMod;
import net.minecraft.util.ResourceLocation;

public class GEUtility {
	public static ResourceLocation gregifiedEnergisticsId(String path) {
		return new ResourceLocation(GregifiedEnergisticsMod.MOD_ID, path);
	}
}
