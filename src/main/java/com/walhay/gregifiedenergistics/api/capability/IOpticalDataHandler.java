package com.walhay.gregifiedenergistics.api.capability;

import gregtech.api.recipes.Recipe;
import java.util.ArrayList;
import java.util.Collection;
import javax.annotation.Nullable;

public interface IOpticalDataHandler {

	void onRecipesUpdate(Collection<IOpticalDataHandler> seen);

	default void onRecipesUpdate() {
		Collection<IOpticalDataHandler> seen = new ArrayList<>();
		seen.add(this);
		onRecipesUpdate(seen);
	}

	@Nullable default Collection<Recipe> getRecipes() {
		Collection<IOpticalDataHandler> seen = new ArrayList<>();
		seen.add(this);
		return getRecipes(seen);
	}

	@Nullable Collection<Recipe> getRecipes(Collection<IOpticalDataHandler> seen);
}
