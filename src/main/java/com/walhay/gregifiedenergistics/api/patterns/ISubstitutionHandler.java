package com.walhay.gregifiedenergistics.api.patterns;

import gregtech.api.recipes.ingredients.GTRecipeInput;
import java.util.Collection;

public interface ISubstitutionHandler {
	Collection<GTRecipeInput> getSubstitutions();

	void injectSubstitutions(ISubstitutionStorage storage);

	ISubstitutionStorage getSubstitutionStorage();
}
