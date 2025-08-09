package com.walhay.gregtechenergistics.mixins;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.MCVersion;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.Name;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.SortingIndex;
import zone.rong.mixinbooter.IEarlyMixinLoader;

@Name("GregTechEnergisticsMixinLoader")
@MCVersion(ForgeVersion.mcVersion)
@SortingIndex(1002)
public class GregTechEnergisticsMixinLoader implements IFMLLoadingPlugin, IEarlyMixinLoader {

	@Override
	public String[] getASMTransformerClass() {
		return null;
	}

	@Override
	public String getModContainerClass() {
		return null;
	}

	@Override
	public String getSetupClass() {
		return null;
	}

	@Override
	public void injectData(Map<String, Object> data) {}

	@Override
	public String getAccessTransformerClass() {
		return null;
	}

	@Override
	public List<String> getMixinConfigs() {
		List<String> configs = new ArrayList<>();

		configs.add("mixins.gte.early.json");

		return configs;
	}
}
