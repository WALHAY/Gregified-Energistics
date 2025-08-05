package com.walhay.gregtechenergistics.api.capability.impl;

import com.walhay.gregtechenergistics.api.capability.AbstractPatternHelper;
import gregtech.api.recipes.Recipe;
import javax.annotation.Nonnull;
import net.minecraft.item.ItemStack;

public class RecipePatternHelper extends AbstractPatternHelper {

	private final Recipe recipe;

	public RecipePatternHelper(@Nonnull Recipe recipe) {
		this.recipe = recipe;
		parseRecipe(recipe);
	}

	public Recipe getRecipe() {
		return recipe;
	}

	@Override
	public ItemStack getPattern() {
		return ItemStack.EMPTY;
	}

	@Override
	public int hashCode() {
		return recipe.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Recipe rec) {
			return rec.equals(recipe);
		} else if (obj instanceof RecipePatternHelper rh) {
			return rh.getRecipe().equals(recipe);
		}

		return false;
	}
}
