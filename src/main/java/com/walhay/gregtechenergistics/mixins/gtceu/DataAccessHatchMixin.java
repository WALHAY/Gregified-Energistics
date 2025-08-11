package com.walhay.gregtechenergistics.mixins.gtceu;

import com.walhay.gregtechenergistics.api.capability.IOpticalDataHandler;
import com.walhay.gregtechenergistics.mixins.interfaces.IDataBankUpdateHandler;

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

	@Inject(method = "rebuildData", at = @At("RETURn"), remap = false)
	private void tailRebuildData(CallbackInfo ci) {
		MetaTileEntityMultiblockPart part = (MetaTileEntityMultiblockPart) (Object) this;

		if (part.isAttachedToMultiBlock() && part.getController() instanceof MetaTileEntityDataBank dataBank) {
			((IDataBankUpdateHandler) dataBank).updateData();
		}
	}

	@Override
	public void onRecipesUpdate(Collection<IOpticalDataHandler> seen) {
		seen.add(this);
		// TODO: split interfaces
	}

	@Override
	public Collection<Recipe> getRecipes(Collection<IDataAccessHatch> seen) {
		seen.add(this);

		return recipes;
	}
}
