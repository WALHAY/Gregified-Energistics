package com.walhay.gregifiedenergistics.api.patterns.implementations;

import appeng.api.networking.crafting.ICraftingMedium;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.crafting.ICraftingProviderHelper;
import com.walhay.gregifiedenergistics.api.patterns.AbstractPatternHelper;
import com.walhay.gregifiedenergistics.api.patterns.ISubstitutionStorage;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.recipes.machines.IResearchRecipeMap;
import gregtech.api.util.AssemblyLineManager;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
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

		ObjectOpenHashSet<Recipe> recipes = (ObjectOpenHashSet<Recipe>)
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
	public void injectSubstitutions(ISubstitutionStorage storage) {
		if (patterns == null) return;

		for (AbstractPatternHelper pattern : patterns) {
			if (pattern == null) continue;

			pattern.injectSubstitutions(storage);
		}
	}

	@Override
	public void providePattern(ICraftingMedium medium, ICraftingProviderHelper helper) {
		if (patterns == null) return;

		for (AbstractPatternHelper pattern : patterns) {
			if (pattern == null) continue;

			pattern.providePattern(medium, helper);
		}
	}

	public boolean contains(ICraftingPatternDetails pattern) {
		if (patterns == null) return false;

		return Arrays.asList(patterns).contains(pattern);
	}

	public Collection<AbstractPatternHelper> getPatterns() {
		return Arrays.asList(patterns);
	}
}
