package com.walhay.gregifiedenergistics.mixins.gtceu.pipenet;

import com.walhay.gregifiedenergistics.api.capability.GECapabilities;
import gregtech.common.pipelike.optical.net.OpticalNetWalker;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(OpticalNetWalker.class)
public class OpticalNetWalkerMixin {

	@Redirect(
			method = "checkNeighbour",
			at =
					@At(
							value = "INVOKE",
							target =
									"Lnet/minecraft/tileentity/TileEntity;hasCapability(Lnet/minecraftforge/common/capabilities/Capability;Lnet/minecraft/util/EnumFacing;)Z",
							ordinal = 1),
			remap = false)
	private boolean modifyCapabilityCheck(TileEntity tile, Capability<?> capability, EnumFacing facing) {
		return tile.hasCapability(capability, facing)
				|| tile.hasCapability(GECapabilities.CAPABILITY_DATA_HANDLER, facing);
	}
}
