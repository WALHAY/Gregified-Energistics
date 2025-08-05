package com.walhay.gregtechenergistics.api.util;

import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMaps;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class AssemblyLineRecipeUtility {

	private static final Map<Integer, Recipe> recipesHashes = RecipeMaps.ASSEMBLY_LINE_RECIPES.getRecipeList().stream()
			.collect(Collectors.toMap(Recipe::hashCode, Function.identity()));

	public static Recipe getRecipeByHash(int hash) {
		return recipesHashes.getOrDefault(hash, null);
	}
}
