package com.walhay.gregifiedenergistics.mixins.gtceu.pipenet;

import com.walhay.gregifiedenergistics.api.capability.GECapabilities;
import com.walhay.gregifiedenergistics.api.capability.IOpticalDataHandler;
import com.walhay.gregifiedenergistics.mixins.interfaces.IOpticalRouteAccessor;
import gregtech.api.pipenet.IRoutePath;
import gregtech.common.pipelike.optical.net.OpticalRoutePath;
import gregtech.common.pipelike.optical.tile.TileEntityOpticalPipe;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(OpticalRoutePath.class)
@Implements(@Interface(iface = IOpticalRouteAccessor.class, prefix = "routeAccessor$"))
public abstract class OpticalRoutePathMixin implements IRoutePath<TileEntityOpticalPipe>, IOpticalRouteAccessor {

	@Override
	@Unique public IOpticalDataHandler getDataHandler() {
		return getTargetCapability(GECapabilities.CAPABILITY_DATA_HANDLER);
	}
}
