package com.walhay.gregifiedenergistics.mixins.gtceu.pipenet;

import com.walhay.gregifiedenergistics.api.capability.GECapabilities;
import com.walhay.gregifiedenergistics.api.capability.IOpticalDataHandler;
import gregtech.api.recipes.Recipe;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityMultiblockNotifiablePart;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityOpticalDataHatch;
import gregtech.common.pipelike.optical.tile.TileEntityOpticalPipe;
import java.util.Collection;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(MetaTileEntityOpticalDataHatch.class)
@Implements(@Interface(iface = IOpticalDataHandler.class, prefix = "recipes$"))
public abstract class OpticalDataHatchMixin extends MetaTileEntityMultiblockNotifiablePart
		implements IOpticalDataHandler {

	private OpticalDataHatchMixin(ResourceLocation metaTileEntityId, int tier, boolean isExportHatch) {
		super(metaTileEntityId, tier, isExportHatch);
	}

	@Shadow(remap = false)
	private boolean isTransmitter;

	@Override
	public Collection<Recipe> getRecipes(Collection<IOpticalDataHandler> seen) {
		seen.add(this);

		if (isAttachedToMultiBlock()) {
			if (isTransmitter) {
				if (getController() instanceof IOpticalDataHandler handler) {
					if (seen.contains(handler)) return null;

					return handler.getRecipes(seen);
				}
			} else {
				TileEntity te = getWorld().getTileEntity(getPos().offset(getFrontFacing()));
				if (te == null) return null;

				if (te instanceof TileEntityOpticalPipe) {
					IOpticalDataHandler cap = te.getCapability(
							GECapabilities.CAPABILITY_DATA_HANDLER,
							getFrontFacing().getOpposite());

					if (cap == null) return null;
					return cap.getRecipes(seen);
				}
			}
		}
		return null;
	}

	@Override
	@Unique public void onRecipesUpdate(Collection<IOpticalDataHandler> seen) {
		seen.add(this);

		if (isTransmitter) {
			TileEntity te = getWorld().getTileEntity(getPos().offset(getFrontFacing()));

			if (te instanceof TileEntityOpticalPipe pipe) {
				IOpticalDataHandler data = pipe.getCapability(
						GECapabilities.CAPABILITY_DATA_HANDLER, getFrontFacing().getOpposite());

				if (data != null) data.onRecipesUpdate(seen);
			}
		} else {
			if (getController() instanceof IOpticalDataHandler handler) {
				if (seen.contains(handler)) return;

				handler.onRecipesUpdate(seen);
			}
		}
	}
}
