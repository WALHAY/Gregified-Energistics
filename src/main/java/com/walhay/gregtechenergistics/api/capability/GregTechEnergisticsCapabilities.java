package com.walhay.gregtechenergistics.api.capability;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

public class GregTechEnergisticsCapabilities {
	@CapabilityInject(IOpticalDataHandler.class)
	public static Capability<IOpticalDataHandler> CAPABILITY_DATA_HANDLER = null;
}
