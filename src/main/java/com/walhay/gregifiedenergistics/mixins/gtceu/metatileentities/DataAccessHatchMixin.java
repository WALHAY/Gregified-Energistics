package com.walhay.gregifiedenergistics.mixins.gtceu.metatileentities;

import com.walhay.gregifiedenergistics.api.capability.IOpticalDataHandler;
import gregtech.api.capability.IDataAccessHatch;
import gregtech.api.recipes.Recipe;
import gregtech.common.metatileentities.multi.electric.MetaTileEntityDataBank;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityDataAccessHatch;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityMultiblockPart;
import java.util.Collection;
import java.util.Set;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MetaTileEntityDataAccessHatch.class)
public abstract class DataAccessHatchMixin implements IOpticalDataHandler {
	@Shadow(remap = false)
	@Final
	private Set<Recipe> recipes;

	@Inject(method = "rebuildData", at = @At("RETURN"), remap = false)
	private void tailRebuildData(CallbackInfo ci) {
		onRecipesUpdate();
	}

	@Override
	public void onRecipesUpdate(Collection<IOpticalDataHandler> seen) {
		MetaTileEntityMultiblockPart part = (MetaTileEntityMultiblockPart) (Object) this;

		seen.add(this);

		if (part.isAttachedToMultiBlock() && part.getController() instanceof MetaTileEntityDataBank dataBank) {
			IOpticalDataHandler handler = (IOpticalDataHandler) dataBank;
			if (seen.contains(handler)) return;

			handler.onRecipesUpdate(seen);
		}
	}

	@Override
	public Collection<Recipe> getRecipes(Collection<IDataAccessHatch> seen) {
		seen.add(this);

		return recipes;
	}
}
