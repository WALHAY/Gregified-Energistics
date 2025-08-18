package com.walhay.gregifiedenergistics.api.capability;

import gregtech.api.capability.SimpleCapabilityManager;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

public class GregifiedEnergisticsCapabilities {
	@CapabilityInject(INetRecipeHandler.class)
	public static Capability<INetRecipeHandler> CAPABILITY_RECIPE_HANDLER = null;

	public static void register() {
		SimpleCapabilityManager.registerCapabilityWithNoDefault(INetRecipeHandler.class);
	}
}
