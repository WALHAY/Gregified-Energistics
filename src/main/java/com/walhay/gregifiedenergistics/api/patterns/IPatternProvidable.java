package com.walhay.gregifiedenergistics.api.patterns;

import appeng.api.networking.crafting.ICraftingMedium;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.crafting.ICraftingProviderHelper;

public interface IPatternProvidable extends ICraftingPatternDetails {

	void providePattern(ICraftingMedium medium, ICraftingProviderHelper helper);
}
