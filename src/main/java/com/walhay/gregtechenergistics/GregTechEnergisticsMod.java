package com.walhay.gregtechenergistics;

import com.walhay.gregtechenergistics.api.render.GTETextures;
import com.walhay.gregtechenergistics.common.metatileentities.MetaTileEntities;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(
		modid = GregTechEnergisticsMod.MOD_ID,
		name = GregTechEnergisticsMod.NAME,
		version = GregTechEnergisticsMod.VERSION,
		dependencies =
				"required-after:gregtech;required-after:appliedenergistics2;after:jei@[4.15.0,);after:JustEnoughEnergistics")
public class GregTechEnergisticsMod {
	public static final String MOD_ID = Tags.MOD_ID;
	public static final String NAME = Tags.MOD_NAME;
	public static final String VERSION = Tags.VERSION;

	@Mod.Instance
	public static GregTechEnergisticsMod INSTANCE;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		MetaTileEntities.init();
		GTETextures.init();
	}
}
