package com.walhay.gregifiedenergistics.mixins.gtceu;

import com.walhay.gregifiedenergistics.api.capability.GECapabilities;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.pipenet.tile.IPipeTile;
import gregtech.common.pipelike.optical.BlockOpticalPipe;
import gregtech.common.pipelike.optical.OpticalPipeProperties;
import gregtech.common.pipelike.optical.OpticalPipeType;
import javax.annotation.Nullable;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(BlockOpticalPipe.class)
public class BlockOpticalPipeMixin {

	/**
	 * @author WALHAY
	 * @reason TODO: change to inject later
	 */
	@Overwrite(remap = false)
	public boolean canPipeConnectToBlock(
			IPipeTile<OpticalPipeType, OpticalPipeProperties> selfTile, EnumFacing side, @Nullable TileEntity tile) {
		if (tile == null) return false;
		if (tile.hasCapability(GregtechTileCapabilities.CAPABILITY_DATA_ACCESS, side.getOpposite())) return true;
		else if (tile.hasCapability(GECapabilities.CAPABILITY_DATA_HANDLER, side.getOpposite())) return true;
		return tile.hasCapability(GregtechTileCapabilities.CABABILITY_COMPUTATION_PROVIDER, side.getOpposite());
	}
}
