package com.walhay.gregtechenergistics.api.capability;

import gregtech.api.capability.IDataAccessHatch;
import gregtech.api.recipes.Recipe;
import java.util.ArrayList;
import java.util.Collection;

public interface IOpticalDataHandler extends IDataAccessHatch {

	void onRecipesUpdate();

	default Collection<Recipe> getRecipes() {
		Collection<IDataAccessHatch> seen = new ArrayList<>();
		seen.add(this);
		return getRecipes(seen);
	}

	Collection<Recipe> getRecipes(Collection<IDataAccessHatch> seen);
}
