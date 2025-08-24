package com.walhay.gregifiedenergistics.mixins.gtceu.recipes;

import com.walhay.gregifiedenergistics.mixins.interfaces.IResearchRecipeMapAccessor;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.machines.RecipeMapAssemblyLine;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RecipeMapAssemblyLine.class)
@Implements(@Interface(iface = IResearchRecipeMapAccessor.class, prefix = "recipeMap$"))
public class RecipeMapMixin implements IResearchRecipeMapAccessor {
	@Unique private static int id = 0;

	@Unique private final Int2ObjectOpenHashMap<Recipe> idToRecipeMap = new Int2ObjectOpenHashMap<>();

	@Unique private final Object2IntOpenHashMap<Recipe> recipeToIdMap = new Object2IntOpenHashMap<>();

	@Unique private static int autoId() {
		return id++;
	}

	@Override
	public int getIdFromRecipe(Recipe recipe) {
		return recipeToIdMap.getOrDefault(recipe, -1);
	}

	@Override
	@Unique public Recipe getRecipeById(int id) {
		return idToRecipeMap.getOrDefault(id, null);
	}

	@Inject(method = "compileRecipe", at = @At(value = "RETURN", ordinal = 1), remap = false)
	public void injectRecipeId(Recipe recipe, CallbackInfoReturnable<Boolean> cib) {
		int id = autoId();

		idToRecipeMap.put(id, recipe);
		recipeToIdMap.put(recipe, id);
	}
}
