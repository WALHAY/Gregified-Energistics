package com.walhay.gregifiedenergistics.api.util;

import gregtech.api.GTValues;
import gregtech.api.recipes.ingredients.GTRecipeInput;
import net.minecraftforge.oredict.OreDictionary;

public class GTHelperUtility {

	public static String getRecipeInputName(GTRecipeInput input) {
		if (input == null) return null;

		if (input.isOreDict()) {
			int oreId = input.getOreDict();

			return OreDictionary.getOreName(oreId);
		}

		return input.toString();
	}

	public static int getTierFromName(String name) {
		if (name == null || name.isEmpty()) return -1;

		for (int i = 0; i < GTValues.VN.length; ++i) {
			if (GTValues.VN[i].equalsIgnoreCase(name)) {
				return i;
			}
		}

		return -1;
	}
}
