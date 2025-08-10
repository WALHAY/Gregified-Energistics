package com.walhay.gregtechenergistics.mixins.gtceu;

import com.walhay.gregtechenergistics.api.capability.IOpticalDataHandler;
import gregtech.api.capability.IDataAccessHatch;
import gregtech.api.capability.IOpticalDataAccessHatch;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.recipes.Recipe;
import gregtech.common.metatileentities.multi.electric.MetaTileEntityDataBank;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityDataAccessHatch;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityMultiblockPart;
import java.util.Collection;
import java.util.List;
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

	@Inject(method = "rebuildData", at = @At("TAIL"), remap = false)
	private void tailRebuildData(CallbackInfo ci) {
		MetaTileEntityMultiblockPart part = (MetaTileEntityMultiblockPart) (Object) this;

		if (part.isAttachedToMultiBlock() && part.getController() instanceof MetaTileEntityDataBank dataBank) {
			List<IOpticalDataAccessHatch> transmitters =
					dataBank.getAbilities(MultiblockAbility.OPTICAL_DATA_TRANSMISSION);

			for (IOpticalDataAccessHatch hatch : transmitters) {
				IOpticalDataHandler updateAccessor = (IOpticalDataHandler) hatch;
				updateAccessor.onRecipesUpdate();
			}
		}
	}

	@Override
	public void onRecipesUpdate() {
		// TODO: split interfaces
	}

	@Override
	public Collection<Recipe> getRecipes(Collection<IDataAccessHatch> seen) {
		seen.add(this);

		return recipes;
	}
}
