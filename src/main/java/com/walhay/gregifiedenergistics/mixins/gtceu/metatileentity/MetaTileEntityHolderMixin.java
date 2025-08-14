package com.walhay.gregifiedenergistics.mixins.gtceu.metatileentity;

import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.crafting.ICraftingProviderHelper;
import com.walhay.gregifiedenergistics.api.metatileentity.ICraftingProviderAccessor;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import net.minecraft.inventory.InventoryCrafting;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(MetaTileEntityHolder.class)
@Implements(@Interface(iface = ICraftingProvider.class, prefix = "craftingProvider$"))
public abstract class MetaTileEntityHolderMixin implements ICraftingProvider {

	@Shadow(remap = false)
	MetaTileEntity metaTileEntity;

	@Override
	public boolean isBusy() {
		return metaTileEntity == null ? true : ((ICraftingProviderAccessor) metaTileEntity).isBusy();
	}

	@Override
	public boolean pushPattern(ICraftingPatternDetails details, InventoryCrafting inventory) {
		return metaTileEntity == null
				? false
				: ((ICraftingProviderAccessor) metaTileEntity).pushPattern(details, inventory);
	}

	@Override
	public void provideCrafting(ICraftingProviderHelper helper) {
		if (metaTileEntity != null) ((ICraftingProviderAccessor) metaTileEntity).provideCrafting(helper);
	}
}
