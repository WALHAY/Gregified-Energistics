package com.walhay.gregifiedenergistics.api.capability;

import gregtech.api.capability.IDataAccessHatch;
import gregtech.api.recipes.Recipe;
import java.util.ArrayList;
import java.util.Collection;

public interface IOpticalDataHandler extends IDataAccessHatch {

	void onRecipesUpdate(Collection<IOpticalDataHandler> seen);

	default void onRecipesUpdate() {
		Collection<IOpticalDataHandler> seen = new ArrayList<>();
		seen.add(this);
		onRecipesUpdate(seen);
	}

	default Collection<Recipe> getRecipes() {
		Collection<IDataAccessHatch> seen = new ArrayList<>();
		seen.add(this);
		return getRecipes(seen);
	}

	Collection<Recipe> getRecipes(Collection<IDataAccessHatch> seen);
}
