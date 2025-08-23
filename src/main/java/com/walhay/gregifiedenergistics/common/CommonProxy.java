package com.walhay.gregifiedenergistics.common;

import com.walhay.gregifiedenergistics.api.capability.GregifiedEnergisticsCapabilities;
import com.walhay.gregifiedenergistics.common.metatileentities.GregifiedEnergisticsMetaTileEntities;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class CommonProxy {

	public void preInit(FMLPreInitializationEvent event) {
		GregifiedEnergisticsMetaTileEntities.init();
		GregifiedEnergisticsCapabilities.register();
	}
}
