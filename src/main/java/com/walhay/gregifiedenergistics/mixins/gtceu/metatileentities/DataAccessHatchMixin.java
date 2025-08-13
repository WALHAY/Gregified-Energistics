package com.walhay.gregifiedenergistics.mixins.gtceu.metatileentities;

import com.walhay.gregifiedenergistics.api.capability.INetRecipeHandler;
import gregtech.api.recipes.Recipe;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityDataAccessHatch;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityMultiblockNotifiablePart;
import java.util.Collection;
import java.util.Set;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MetaTileEntityDataAccessHatch.class)
@Implements(@Interface(iface = INetRecipeHandler.class, prefix = "dataHandler$"))
public abstract class DataAccessHatchMixin extends MetaTileEntityMultiblockNotifiablePart implements INetRecipeHandler {

	private DataAccessHatchMixin(ResourceLocation metaTileEntityId, int tier, boolean isExportHatch) {
		super(metaTileEntityId, tier, isExportHatch);
	}

	@Shadow(remap = false)
	@Final
	private Set<Recipe> recipes;

	@Inject(method = "rebuildData", at = @At("RETURN"), remap = false)
	private void updateOnRebuildData(CallbackInfo ci) {
		onRecipesUpdate();
	}

	@Override
	@Unique public void onRecipesUpdate(Collection<INetRecipeHandler> seen) {
		seen.add(this);

		if (isAttachedToMultiBlock() && getController() instanceof INetRecipeHandler handler) {
			if (seen.contains(handler)) return;

			handler.onRecipesUpdate(seen);
		}
	}

	@Override
	@Unique public Collection<Recipe> getRecipes(Collection<INetRecipeHandler> seen) {
		seen.add(this);

		return recipes;
	}
}
