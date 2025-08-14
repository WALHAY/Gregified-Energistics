package com.walhay.gregifiedenergistics.api.metatileentity;

import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.crafting.ICraftingProviderHelper;
import net.minecraft.inventory.InventoryCrafting;

public interface ICraftingProviderAccessor {

	boolean isBusy();

	boolean pushPattern(ICraftingPatternDetails pattern, InventoryCrafting inventory);

	void provideCrafting(ICraftingProviderHelper provider);
}
