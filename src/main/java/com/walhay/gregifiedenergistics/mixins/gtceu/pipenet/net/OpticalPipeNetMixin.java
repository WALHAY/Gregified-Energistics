package com.walhay.gregifiedenergistics.mixins.gtceu.pipenet.net;

import com.walhay.gregifiedenergistics.api.capability.INetRecipeHandler;
import com.walhay.gregifiedenergistics.mixins.interfaces.IOpticalRouteAccessor;
import gregtech.api.pipenet.PipeNet;
import gregtech.api.pipenet.WorldPipeNet;
import gregtech.common.pipelike.optical.OpticalPipeProperties;
import gregtech.common.pipelike.optical.net.OpticalPipeNet;
import gregtech.common.pipelike.optical.net.OpticalRoutePath;
import java.util.Map;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(OpticalPipeNet.class)
public abstract class OpticalPipeNetMixin extends PipeNet<OpticalPipeProperties> {

	@Shadow(remap = false)
	@Final
	private Map<BlockPos, OpticalRoutePath> NET_DATA;

	@Shadow(remap = false)
	public abstract OpticalRoutePath getNetData(BlockPos pipePos, EnumFacing facing);

	private OpticalPipeNetMixin(WorldPipeNet<OpticalPipeProperties, ? extends PipeNet<OpticalPipeProperties>> world) {
		super(world);
	}

	@Override
	protected void onNodeConnectionsUpdate() {
		super.onNodeConnectionsUpdate();
		getAllNodes().keySet().stream().findFirst().ifPresent(pos -> {
			for (EnumFacing facing : EnumFacing.values()) {
				if (getNetData(pos, facing) instanceof IOpticalRouteAccessor accessor) {
					INetRecipeHandler handler = accessor.getDataHandler();
					if (handler != null) {
						handler.onRecipesUpdate();
						return;
					}
				}
			}
		});
	}
}
