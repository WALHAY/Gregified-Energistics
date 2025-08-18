package com.walhay.gregifiedenergistics;

import com.walhay.gregifiedenergistics.api.capability.GregifiedEnergisticsCapabilities;
import com.walhay.gregifiedenergistics.api.render.GregifiedEnergisticsTextures;
import com.walhay.gregifiedenergistics.common.metatileentities.MetaTileEntities;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(
		modid = GregifiedEnergisticsMod.MOD_ID,
		name = GregifiedEnergisticsMod.NAME,
		version = GregifiedEnergisticsMod.VERSION,
		dependencies =
				"required-after:gregtech;required-after:appliedenergistics2;after:jei@[4.15.0,);after:JustEnoughEnergistics")
public class GregifiedEnergisticsMod {
	public static final String MOD_ID = Tags.MOD_ID;
	public static final String NAME = Tags.MOD_NAME;
	public static final String VERSION = Tags.VERSION;

	@Mod.Instance
	public static GregifiedEnergisticsMod INSTANCE;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		MetaTileEntities.init();
		GregifiedEnergisticsTextures.init();
		GregifiedEnergisticsCapabilities.register();
	}
}
