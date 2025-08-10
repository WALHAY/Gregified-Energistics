package com.walhay.gregtechenergistics.mixins.gtceu;

import com.walhay.gregtechenergistics.api.capability.IOpticalDataHandler;
import com.walhay.gregtechenergistics.mixins.interfaces.IOpticalHandlerProperty;
import gregtech.common.pipelike.optical.net.OpticalPipeNet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(OpticalPipeNet.class)
public class OpticalPipeNetMixin implements IOpticalHandlerProperty {

	@Unique private IOpticalDataHandler handler;

	@Override
	public void setHandler(IOpticalDataHandler handler) {
		this.handler = handler;
	}

	public void onNodeConnectionsUpdate() {
		System.out.println("Pipe change");
		if (handler != null) handler.onRecipesUpdate();
	}
}
