package com.walhay.gregtechenergistics.mixins.gtceu;

import com.walhay.gregtechenergistics.api.capability.IRecipeMixinAccessor;
import gregtech.api.recipes.Recipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Recipe.class)
public class RecipeMixin implements IRecipeMixinAccessor {
	@Unique private static int id = 0;

	@Unique private static int autoId() {
		return id++;
	}

	@Unique public int recipeId;

	@Override
	@Unique public int getRecipeId() {
		return recipeId;
	}

	@Inject(method = "<init>", at = @At("RETURN"), remap = false)
	private void onConstructorHead(CallbackInfo ci) {
		this.recipeId = autoId();
	}
}
