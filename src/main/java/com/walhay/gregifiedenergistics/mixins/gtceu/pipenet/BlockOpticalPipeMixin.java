package com.walhay.gregifiedenergistics.mixins.gtceu.pipenet;

import com.walhay.gregifiedenergistics.api.capability.GECapabilities;
import gregtech.api.pipenet.tile.IPipeTile;
import gregtech.common.pipelike.optical.BlockOpticalPipe;
import gregtech.common.pipelike.optical.OpticalPipeProperties;
import gregtech.common.pipelike.optical.OpticalPipeType;
import javax.annotation.Nullable;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockOpticalPipe.class)
public class BlockOpticalPipeMixin {

	@Inject(
			method = "canPipeConnectToBlock",
			at = @At(value = "RETURN", ordinal = 2),
			remap = false,
			cancellable = true)
	private void canPipeConnectToBlock(
			IPipeTile<OpticalPipeType, OpticalPipeProperties> selfTile,
			EnumFacing side,
			@Nullable TileEntity tile,
			CallbackInfoReturnable<Boolean> cir) {
		if (tile.hasCapability(GECapabilities.CAPABILITY_RECIPE_HANDLER, side.getOpposite())) cir.setReturnValue(true);
	}
}
