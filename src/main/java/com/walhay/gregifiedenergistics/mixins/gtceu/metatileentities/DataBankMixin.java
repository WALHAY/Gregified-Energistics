package com.walhay.gregifiedenergistics.mixins.gtceu.metatileentities;

import com.walhay.gregifiedenergistics.api.capability.IOpticalDataHandler;
import com.walhay.gregifiedenergistics.mixins.interfaces.IDataBankUpdateHandler;
import gregtech.api.capability.IOpticalDataAccessHatch;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.common.metatileentities.multi.electric.MetaTileEntityDataBank;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MetaTileEntityDataBank.class)
public class DataBankMixin implements IDataBankUpdateHandler {

	@Override
	@Unique public void updateData() {
		MetaTileEntityDataBank dataBank = (MetaTileEntityDataBank) (Object) this;

		List<IOpticalDataAccessHatch> transmitters = dataBank.getAbilities(MultiblockAbility.OPTICAL_DATA_TRANSMISSION);
		Collection<IOpticalDataHandler> seen = new ArrayList<>();
		for (IOpticalDataAccessHatch hatch : transmitters) {
			IOpticalDataHandler updater = (IOpticalDataHandler) hatch;

			if (seen.contains(updater)) continue;

			updater.onRecipesUpdate(seen);
		}
	}

	@Inject(method = "formStructure", at = @At("HEAD"), remap = false)
	public void headFormStructure(CallbackInfo ci) {
		updateData();
	}

	@Inject(method = "invalidateStructure", at = @At("HEAD"), remap = false)
	public void headInvalidateStructure(CallbackInfo ci) {
		updateData();
	}
}
