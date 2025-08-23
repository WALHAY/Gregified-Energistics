package com.walhay.gregifiedenergistics.api.capability;

import com.walhay.gregifiedenergistics.api.patterns.ISubstitutionStorage;
import gregtech.api.capability.SimpleCapabilityManager;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

public class GregifiedEnergisticsCapabilities {
	@CapabilityInject(INetRecipeHandler.class)
	public static Capability<INetRecipeHandler> CAPABILITY_RECIPE_HANDLER = null;

	@CapabilityInject(ISubstitutionStorage.class)
	public static Capability<ISubstitutionStorage> CAPABILITY_SUBSTITUTION_STORAGE = null;

	public static void register() {
		SimpleCapabilityManager.registerCapabilityWithNoDefault(INetRecipeHandler.class);
		SimpleCapabilityManager.registerCapabilityWithNoDefault(ISubstitutionStorage.class);
	}
}
