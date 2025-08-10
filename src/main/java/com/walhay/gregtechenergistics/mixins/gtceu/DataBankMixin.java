package com.walhay.gregtechenergistics.mixins.gtceu;

import com.walhay.gregtechenergistics.api.capability.IOpticalDataHandler;
import com.walhay.gregtechenergistics.mixins.interfaces.IDataBankUpdateHandler;
import gregtech.api.capability.IOpticalDataAccessHatch;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.common.metatileentities.multi.electric.MetaTileEntityDataBank;
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
		for (IOpticalDataAccessHatch hatch : transmitters) {
			IOpticalDataHandler updater = (IOpticalDataHandler) hatch;

			updater.onRecipesUpdate();
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
