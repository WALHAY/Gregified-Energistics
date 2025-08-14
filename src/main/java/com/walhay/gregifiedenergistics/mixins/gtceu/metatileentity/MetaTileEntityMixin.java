package com.walhay.gregifiedenergistics.mixins.gtceu.metatileentity;

import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.crafting.ICraftingProviderHelper;
import com.walhay.gregifiedenergistics.api.metatileentity.ICraftingProviderAccessor;
import gregtech.api.metatileentity.MetaTileEntity;
import net.minecraft.inventory.InventoryCrafting;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(MetaTileEntity.class)
@Implements(@Interface(iface = ICraftingProviderAccessor.class, prefix = "craftingProviderAccessor$"))
public class MetaTileEntityMixin implements ICraftingProviderAccessor {

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
