package com.walhay.gregifiedenergistics.api.patterns;

import gregtech.api.recipes.ingredients.GTRecipeInput;
import java.util.Collection;

public interface ISubstitutionHandler<T> {
	Collection<GTRecipeInput> getSubstitutions();

	void injectSubstitutions(ISubstitutionStorage<T> storage);

	ISubstitutionStorage<T> getSubstitutionStorage();
}
