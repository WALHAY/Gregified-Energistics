package com.walhay.gregifiedenergistics.api.util;

import gregtech.api.recipes.ingredients.GTRecipeInput;
import java.util.ArrayList;
import java.util.Collection;
import net.minecraftforge.oredict.OreDictionary;

public class GTRecipeInputHelper {

	public static String getRecipeInputName(GTRecipeInput input) {
		if (input == null) return null;

		if (input.isOreDict()) {
			int oreId = input.getOreDict();

			return OreDictionary.getOreName(oreId);
		}

		return input.toString();
	}

	public static Collection<GTRecipeInput> filterUniqueInputs(Collection<GTRecipeInput> inputs) {
		Collection<GTRecipeInput> result = new ArrayList<>();

		inputs.stream()
				.filter(input -> result.stream().noneMatch(i -> i.equalIgnoreAmount(input)))
				.forEach(result::add);

		return result;
	}
}
