package com.walhay.gregtechenergistics.api.capability.impl;

import appeng.api.networking.crafting.ICraftingMedium;
import appeng.api.networking.crafting.ICraftingProviderHelper;
import com.walhay.gregtechenergistics.api.capability.AbstractPatternHelper;
import com.walhay.gregtechenergistics.api.capability.ISubstitutionStorage;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.recipes.machines.IResearchRecipeMap;
import gregtech.api.util.AssemblyLineManager;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Objects;
import javax.annotation.Nonnull;
import net.minecraft.item.ItemStack;

public class DataStickPatternHelper extends AbstractPatternHelper {

	private AbstractPatternHelper[] patterns;

	public DataStickPatternHelper(@Nonnull ItemStack dataStick) {
		if (!AssemblyLineManager.isStackDataItem(dataStick, true))
			throw new IllegalArgumentException("Invalid data stick");

		if (!AssemblyLineManager.hasResearchTag(dataStick)) return;

		String researchId = AssemblyLineManager.readResearchId(dataStick);

		if (researchId == null) return;

		ObjectOpenHashSet<Recipe> recipes = (ObjectOpenHashSet<Recipe>)
				((IResearchRecipeMap) RecipeMaps.ASSEMBLY_LINE_RECIPES).getDataStickEntry(researchId);

		if (recipes == null) return;

		patterns = recipes.stream()
				.filter(Objects::nonNull)
				.map(recipe -> new RecipePatternHelper(recipe, dataStick))
				.toArray(AbstractPatternHelper[]::new);
	}

	@Override
	public ItemStack getPattern() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void injectSubstitutions(ISubstitutionStorage storage) {
		for (AbstractPatternHelper pattern : patterns) {
			pattern.injectSubstitutions(storage);
		}
	}

	@Override
	public void providePattern(ICraftingMedium medium, ICraftingProviderHelper helper) {
		for (AbstractPatternHelper pattern : patterns) {
			pattern.providePattern(medium, helper);
		}
	}
}
