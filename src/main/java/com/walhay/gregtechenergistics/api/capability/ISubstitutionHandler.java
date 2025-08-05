package com.walhay.gregtechenergistics.api.capability;

import gregtech.api.recipes.ingredients.GTRecipeInput;
import java.util.Collection;

public interface ISubstitutionHandler {
	Collection<GTRecipeInput> getSubstitutions();

	void injectSubstitutions(ISubstitutionStorage storage);
}
