package com.walhay.gregifiedenergistics.mixins.gtceu.pipenet;

import com.walhay.gregifiedenergistics.api.capability.GECapabilities;
import com.walhay.gregifiedenergistics.api.capability.INetRecipeHandler;
import com.walhay.gregifiedenergistics.api.capability.IOpticalNetRecipeHandler;
import gregtech.api.recipes.Recipe;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityMultiblockNotifiablePart;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityOpticalDataHatch;
import gregtech.common.pipelike.optical.tile.TileEntityOpticalPipe;
import java.util.Collection;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MetaTileEntityOpticalDataHatch.class)
@Implements(@Interface(iface = IOpticalNetRecipeHandler.class, prefix = "dataHandler$"))
public abstract class OpticalDataHatchMixin extends MetaTileEntityMultiblockNotifiablePart
		implements IOpticalNetRecipeHandler {

	private OpticalDataHatchMixin(ResourceLocation metaTileEntityId, int tier, boolean isExportHatch) {
		super(metaTileEntityId, tier, isExportHatch);
	}

	@Shadow(remap = false)
	@Final
	private boolean isTransmitter;

	@Inject(method = "getCapability", at = @At("HEAD"), remap = false, cancellable = true)
	private <I> void injectNewCapability(Capability<I> capability, EnumFacing facing, CallbackInfoReturnable<I> cir) {
		if (capability == GECapabilities.CAPABILITY_RECIPE_HANDLER) {
			cir.setReturnValue(GECapabilities.CAPABILITY_RECIPE_HANDLER.cast(this));
		}
	}

	@Override
	@Unique public Collection<Recipe> getRecipes(Collection<INetRecipeHandler> seen) {
		seen.add(this);

		if (isAttachedToMultiBlock()) {
			if (isTransmitter) {
				if (getController() instanceof INetRecipeHandler handler) {
					if (seen.contains(handler)) return null;

					return handler.getRecipes(seen);
				}
			} else {
				TileEntity te = getWorld().getTileEntity(getPos().offset(getFrontFacing()));
				if (te == null) return null;

				if (te instanceof TileEntityOpticalPipe) {
					INetRecipeHandler cap = te.getCapability(
							GECapabilities.CAPABILITY_RECIPE_HANDLER,
							getFrontFacing().getOpposite());

					if (cap == null || seen.contains(cap)) return null;
					return cap.getRecipes(seen);
				}
			}
		}
		return null;
	}

	@Override
	@Unique public void onRecipesUpdate(Collection<INetRecipeHandler> seen) {
		seen.add(this);

		if (isTransmitter) {
			TileEntity te = getWorld().getTileEntity(getPos().offset(getFrontFacing()));

			if (te instanceof TileEntityOpticalPipe pipe) {
				INetRecipeHandler data = pipe.getCapability(
						GECapabilities.CAPABILITY_RECIPE_HANDLER,
						getFrontFacing().getOpposite());

				if (data != null && !seen.contains(data)) data.onRecipesUpdate(seen);
			}
		} else {
			if (getController() instanceof INetRecipeHandler handler) {
				if (seen.contains(handler)) return;

				handler.onRecipesUpdate(seen);
			}
		}
	}
}
