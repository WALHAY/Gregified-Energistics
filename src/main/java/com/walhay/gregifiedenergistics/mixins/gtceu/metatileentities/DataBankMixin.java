package com.walhay.gregifiedenergistics.mixins.gtceu.metatileentities;

import com.walhay.gregifiedenergistics.api.capability.IOpticalDataHandler;
import gregtech.api.capability.IDataAccessHatch;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.recipes.Recipe;
import gregtech.common.metatileentities.multi.electric.MetaTileEntityDataBank;
import java.util.ArrayList;
import java.util.Collection;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MetaTileEntityDataBank.class)
public class DataBankMixin implements IOpticalDataHandler {

	@Shadow(remap = false)
	private boolean isActive;

	@Inject(
			method = "setActive",
			at = @At(value = "FIELD", opcode = Opcodes.PUTFIELD, ordinal = 0, shift = Shift.AFTER),
			remap = false)
	public void onSetActive(CallbackInfo ci) {
		onRecipesUpdate();
	}

	@Inject(method = "writeInitialSyncData", at = @At("TAIL"), remap = false)
	public void reloadOnLoad(CallbackInfo ci) {
		onRecipesUpdate();
	}

	@Inject(method = "invalidateStructure", at = @At("HEAD"), remap = false)
	public void headInvalidateStructure(CallbackInfo ci) {
		isActive = false;
		onRecipesUpdate();
	}

	@Override
	public boolean isCreative() {
		return false;
	}

	@Override
	public boolean isRecipeAvailable(Recipe recipe, Collection<IDataAccessHatch> seen) {
		throw new UnsupportedOperationException("Method isRecipeAvailable shouldn't be used in case of databank");
	}

	@Override
	public void onRecipesUpdate(Collection<IOpticalDataHandler> seen) {
		seen.add(this);
		MetaTileEntityDataBank dataBank = (MetaTileEntityDataBank) (Object) this;
		for (IDataAccessHatch hatch : dataBank.getAbilities(MultiblockAbility.OPTICAL_DATA_TRANSMISSION)) {
			if (seen.contains(hatch)) continue;

			((IOpticalDataHandler) hatch).onRecipesUpdate(seen);
		}
	}

	@Override
	public Collection<Recipe> getRecipes(Collection<IDataAccessHatch> seen) {
		seen.add(this);

		if (!isActive) return null;

		MetaTileEntityDataBank dataBank = (MetaTileEntityDataBank) (Object) this;
		Collection<Recipe> recipes = new ArrayList<>();

		getRecipesCollector(recipes, dataBank.getAbilities(MultiblockAbility.DATA_ACCESS_HATCH), seen);
		getRecipesCollector(recipes, dataBank.getAbilities(MultiblockAbility.OPTICAL_DATA_RECEPTION), seen);

		return recipes;
	}

	private void getRecipesCollector(
			Collection<Recipe> recipes,
			Iterable<? extends IDataAccessHatch> hatches,
			Collection<IDataAccessHatch> seen) {
		for (IDataAccessHatch hatch : hatches) {
			if (seen.contains(hatch)) continue;

			Collection<Recipe> hatchRecipes = ((IOpticalDataHandler) hatch).getRecipes(seen);

			if (hatchRecipes != null) recipes.addAll(hatchRecipes);
		}
	}
}
