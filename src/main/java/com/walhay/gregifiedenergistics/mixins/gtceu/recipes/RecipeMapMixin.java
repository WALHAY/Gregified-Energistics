package com.walhay.gregifiedenergistics.mixins.gtceu.recipes;

import com.walhay.gregifiedenergistics.api.capability.*;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RecipeMap.class)
public class RecipeMapMixin implements IRecipeMapAccessor {

	@Unique private final Int2ObjectOpenHashMap<Recipe> recipeIdMap = new Int2ObjectOpenHashMap<>();

	@Override
	@Unique public Recipe getRecipeById(int id) {
		return recipeIdMap.getOrDefault(id, null);
	}

	@Inject(method = "compileRecipe", at = @At(value = "RETURN", ordinal = 1), remap = false)
	public void injectRecipeId(Recipe recipe, CallbackInfoReturnable<Boolean> cib) {
		IRecipeAccessor accessor = (IRecipeAccessor) recipe;
		recipeIdMap.put(accessor.getRecipeId(), recipe);
	}
}
