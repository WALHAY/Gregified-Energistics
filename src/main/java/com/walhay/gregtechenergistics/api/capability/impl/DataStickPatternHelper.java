package com.walhay.gregtechenergistics.api.capability.impl;

import com.walhay.gregtechenergistics.api.capability.AbstractPatternHelper;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.recipes.machines.IResearchRecipeMap;
import gregtech.api.util.AssemblyLineManager;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Objects;
import javax.annotation.Nonnull;
import net.minecraft.item.ItemStack;

public class DataStickPatternHelper extends AbstractPatternHelper {

	private final ItemStack dataStick;

	public DataStickPatternHelper(@Nonnull ItemStack dataStick) {
		if (!AssemblyLineManager.isStackDataItem(dataStick, true))
			throw new IllegalArgumentException("Invalid data stick");

		this.dataStick = dataStick;
		if (!AssemblyLineManager.hasResearchTag(dataStick)) return;

		String researchId = AssemblyLineManager.readResearchId(dataStick);

		if (researchId == null) return;

		ObjectOpenHashSet<Recipe> recipes = (ObjectOpenHashSet<Recipe>)
				((IResearchRecipeMap) RecipeMaps.ASSEMBLY_LINE_RECIPES).getDataStickEntry(researchId);

		if (recipes == null) return;

		recipes.stream().filter(Objects::nonNull).findFirst().ifPresent(this::parseRecipe);
	}

	@Override
	public ItemStack getPattern() {
		return dataStick;
	}
}
