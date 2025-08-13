package com.walhay.gregifiedenergistics.mixins.gtceu.metatileentities;

import com.walhay.gregifiedenergistics.api.capability.INetRecipeHandler;
import gregtech.api.capability.IControllable;
import gregtech.api.capability.IDataAccessHatch;
import gregtech.api.capability.IOpticalDataAccessHatch;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.MultiblockWithDisplayBase;
import gregtech.api.recipes.Recipe;
import gregtech.common.metatileentities.multi.electric.MetaTileEntityDataBank;
import java.util.ArrayList;
import java.util.Collection;
import net.minecraft.util.ResourceLocation;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MetaTileEntityDataBank.class)
@Implements(@Interface(iface = INetRecipeHandler.class, prefix = "dataHandler$"))
public abstract class DataBankMixin extends MultiblockWithDisplayBase implements INetRecipeHandler {

	private DataBankMixin(ResourceLocation metaTileEntityId) {
		super(metaTileEntityId);
	}

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
	@Unique public void onRecipesUpdate(Collection<INetRecipeHandler> seen) {
		seen.add(this);

		for (IOpticalDataAccessHatch hatch : getAbilities(MultiblockAbility.OPTICAL_DATA_TRANSMISSION)) {
			if (hatch instanceof INetRecipeHandler handler) {
				if (seen.contains(handler)) continue;

				handler.onRecipesUpdate(seen);
			}
		}
	}

	@Override
	@Unique public Collection<Recipe> getRecipes(Collection<INetRecipeHandler> seen) {
		seen.add(this);

		if (!isActive()) return null;

		Collection<Recipe> recipes = new ArrayList<>();

		getRecipesCollector(recipes, getAbilities(MultiblockAbility.DATA_ACCESS_HATCH), seen);
		getRecipesCollector(recipes, getAbilities(MultiblockAbility.OPTICAL_DATA_RECEPTION), seen);

		return recipes;
	}

	@Unique private void getRecipesCollector(
			Collection<Recipe> recipes,
			Iterable<? extends IDataAccessHatch> hatches,
			Collection<INetRecipeHandler> seen) {
		for (IDataAccessHatch hatch : hatches) {
			if (hatch instanceof INetRecipeHandler handler) {
				if (seen.contains(handler)) continue;

				Collection<Recipe> hatchRecipes = handler.getRecipes(seen);

				if (hatchRecipes != null) recipes.addAll(hatchRecipes);
			}
		}
	}
}
