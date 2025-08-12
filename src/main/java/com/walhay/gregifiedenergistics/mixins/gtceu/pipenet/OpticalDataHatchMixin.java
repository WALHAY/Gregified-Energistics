package com.walhay.gregifiedenergistics.mixins.gtceu.pipenet;

import com.walhay.gregifiedenergistics.api.capability.IOpticalDataHandler;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IDataAccessHatch;
import gregtech.api.recipes.Recipe;
import gregtech.common.metatileentities.multi.electric.MetaTileEntityDataBank;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityOpticalDataHatch;
import gregtech.common.pipelike.optical.tile.TileEntityOpticalPipe;
import java.util.Collection;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(MetaTileEntityOpticalDataHatch.class)
public abstract class OpticalDataHatchMixin implements IOpticalDataHandler {

	@Shadow(remap = false)
	private boolean isTransmitter;

	@Override
	public Collection<Recipe> getRecipes(Collection<IDataAccessHatch> seen) {
		MetaTileEntityOpticalDataHatch hatch = (MetaTileEntityOpticalDataHatch) (Object) this;
		seen.add(this);

		if (hatch.isAttachedToMultiBlock()) {
			if (hatch.isTransmitter()) {
				if (hatch.getController() instanceof MetaTileEntityDataBank dataBank) {
					IOpticalDataHandler handler = (IOpticalDataHandler) dataBank;
					if (seen.contains(handler)) return null;

					return handler.getRecipes(seen);
				}
			} else {
				BlockPos pos = hatch.getPos();
				EnumFacing facing = hatch.getFrontFacing();
				TileEntity te = hatch.getWorld().getTileEntity(pos.offset(facing));
				if (te == null) return null;

				if (te instanceof TileEntityOpticalPipe) {
					IOpticalDataHandler cap = (IOpticalDataHandler)
							te.getCapability(GregtechTileCapabilities.CAPABILITY_DATA_ACCESS, facing.getOpposite());

					if (cap == null) return null;
					return cap.getRecipes(seen);
				}
			}
		}
		return null;
	}

	@Override
	@Unique public void onRecipesUpdate(Collection<IOpticalDataHandler> seen) {
		MetaTileEntityOpticalDataHatch hatch = (MetaTileEntityOpticalDataHatch) (Object) this;
		seen.add(this);

		if (isTransmitter) {
			BlockPos pos = hatch.getPos();
			EnumFacing facing = hatch.getFrontFacing();
			TileEntity te = hatch.getWorld().getTileEntity(pos.offset(facing));

			if (te instanceof TileEntityOpticalPipe pipe) {
				IDataAccessHatch data =
						pipe.getCapability(GregtechTileCapabilities.CAPABILITY_DATA_ACCESS, facing.getOpposite());

				((IOpticalDataHandler) data).onRecipesUpdate(seen);
			}
		} else {
			if (hatch.getController() instanceof MetaTileEntityDataBank dataBank) {
				IOpticalDataHandler handler = (IOpticalDataHandler) dataBank;
				if (seen.contains(handler)) return;

				handler.onRecipesUpdate(seen);
			}
		}
	}
}
