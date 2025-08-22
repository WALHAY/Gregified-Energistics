package com.walhay.gregifiedenergistics.api.patterns.implementations;

import appeng.api.networking.crafting.ICraftingMedium;
import appeng.api.networking.crafting.ICraftingProviderHelper;
import com.walhay.gregifiedenergistics.api.patterns.AbstractPatternHelper;
import com.walhay.gregifiedenergistics.api.patterns.IProvidablePattern;
import com.walhay.gregifiedenergistics.api.patterns.ISubstitutionStorage;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.recipes.machines.IResearchRecipeMap;
import gregtech.api.util.AssemblyLineManager;
import java.util.Arrays;
import java.util.Collection;
import javax.annotation.Nonnull;
import net.minecraft.item.ItemStack;

public class DataStickPatternHelper extends AbstractPatternHelper {

	private final ItemStack dataStick;
	private AbstractPatternHelper[] patterns;

	public DataStickPatternHelper(@Nonnull ItemStack dataStick) {
		assert AssemblyLineManager.isStackDataItem(dataStick, true) : "ItemStack is not DataStick";
		this.dataStick = dataStick;

		if (!AssemblyLineManager.hasResearchTag(dataStick)) return;

		String researchId = AssemblyLineManager.readResearchId(dataStick);

		if (researchId == null) return;

		Collection<Recipe> recipes =
				((IResearchRecipeMap) RecipeMaps.ASSEMBLY_LINE_RECIPES).getDataStickEntry(researchId);

		if (recipes == null) return;

		patterns = recipes.stream()
				.map(recipe -> new RecipePatternHelper(recipe, dataStick))
				.toArray(AbstractPatternHelper[]::new);
	}

	@Override
	public ItemStack getPattern() {
		return dataStick;
	}

	@Override
	public void injectSubstitutions(ISubstitutionStorage<String> storage) {
		if (patterns == null) return;

		for (AbstractPatternHelper pattern : patterns) {
			if (pattern == null) continue;

			pattern.injectSubstitutions(storage);
		}
	}

	@Override
	public void providePatterns(ICraftingMedium medium, ICraftingProviderHelper helper) {
		if (patterns == null) return;

		for (AbstractPatternHelper pattern : patterns) {
			if (pattern == null) continue;

			pattern.providePatterns(medium, helper);
		}
	}

	@Override
	public Collection<IProvidablePattern> getProvidedPatterns() {
		return Arrays.asList(patterns);
	}
}
