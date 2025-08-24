package com.walhay.gregifiedenergistics.mixins.interfaces;

import gregtech.api.recipes.Recipe;

public interface IResearchRecipeMapAccessor {
	Recipe getRecipeById(int id);

	int getIdFromRecipe(Recipe recipe);
}
