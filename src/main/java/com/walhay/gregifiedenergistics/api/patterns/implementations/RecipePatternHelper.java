package com.walhay.gregifiedenergistics.api.patterns.implementations;

import com.walhay.gregifiedenergistics.api.patterns.AbstractPatternHelper;
import gregtech.api.recipes.Recipe;
import javax.annotation.Nonnull;
import net.minecraft.item.ItemStack;

public class RecipePatternHelper extends AbstractPatternHelper {

	private final Recipe recipe;
	private final ItemStack item;

	public RecipePatternHelper(@Nonnull Recipe recipe, @Nonnull ItemStack item) {
		this.recipe = recipe;
		this.item = item;
		parseRecipe(recipe);
	}

	public RecipePatternHelper(@Nonnull Recipe recipe) {
		this(recipe, ItemStack.EMPTY);
	}

	public Recipe getRecipe() {
		return recipe;
	}

	@Override
	public ItemStack getPattern() {
		return item;
	}
}
