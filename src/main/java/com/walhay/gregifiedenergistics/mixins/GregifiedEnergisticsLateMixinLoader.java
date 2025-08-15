package com.walhay.gregifiedenergistics.mixins;

import java.util.ArrayList;
import java.util.List;
import zone.rong.mixinbooter.ILateMixinLoader;

public class GregifiedEnergisticsLateMixinLoader implements ILateMixinLoader {

	@Override
	public List<String> getMixinConfigs() {
		List<String> configs = new ArrayList<>();

		configs.add("mixins.ge.gtceu.json");
		configs.add("mixins.ge.appeng.json");

		return configs;
	}
}
