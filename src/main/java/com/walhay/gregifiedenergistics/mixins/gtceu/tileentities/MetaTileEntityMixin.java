package com.walhay.gregifiedenergistics.mixins.gtceu.tileentities;

import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.crafting.ICraftingProviderHelper;
import gregtech.api.metatileentity.MetaTileEntity;
import net.minecraft.inventory.InventoryCrafting;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(MetaTileEntity.class)
@Implements(@Interface(iface = ICraftingProvider.class, prefix = "craftingProvider$"))
public class MetaTileEntityMixin implements ICraftingProvider {

	@Override
	public boolean isBusy() {
		return true;
	}

	@Override
	public boolean pushPattern(ICraftingPatternDetails pattern, InventoryCrafting inventory) {
		return false;
	}

	@Override
	public void provideCrafting(ICraftingProviderHelper provider) {}
}
