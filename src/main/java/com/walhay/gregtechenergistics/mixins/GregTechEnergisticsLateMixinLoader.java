package com.walhay.gregtechenergistics.mixins;

import java.util.ArrayList;
import java.util.List;
import zone.rong.mixinbooter.ILateMixinLoader;

public class GregTechEnergisticsLateMixinLoader implements ILateMixinLoader {

	@Override
	public List<String> getMixinConfigs() {
		List<String> configs = new ArrayList<>();

		configs.add("mixins.gte.gtceu.json");

		return configs;
	}
}
