package com.walhay.gregifiedenergistics.mixins.gtceu.metatileentities;

import static gregtech.common.metatileentities.multi.electric.MetaTileEntityAssemblyLine.metaTileEntities;

import com.walhay.gregifiedenergistics.common.metatileentities.GregifiedEnergisticsMetaTileEntities;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.TraceabilityPredicate;
import gregtech.common.metatileentities.MetaTileEntities;
import gregtech.common.metatileentities.multi.electric.MetaTileEntityAssemblyLine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MetaTileEntityAssemblyLine.class)
public class AssemblyLineMixin {

	@ModifyConstant(
			method = "createStructurePattern",
			constant = @Constant(stringValue = "FIF", ordinal = 0),
			remap = false)
	private String changeStructure(String original) {
		return "FBF";
	}

	@Redirect(
			method = "createStructurePattern",
			at =
					@At(
							value = "INVOKE",
							target =
									"Lgregtech/api/pattern/FactoryBlockPattern;where(CLgregtech/api/pattern/TraceabilityPredicate;)Lgregtech/api/pattern/FactoryBlockPattern;",
							ordinal = 0),
			remap = false)
	private FactoryBlockPattern injectBusPredicate(
			FactoryBlockPattern pattern, char c, TraceabilityPredicate predicate) {
		return pattern.where(c, predicate)
				.where(
						'B',
						metaTileEntities(
								MetaTileEntities.ITEM_IMPORT_BUS[0],
								GregifiedEnergisticsMetaTileEntities.ME_ASSEMBLY_LINE_BUS,
								GregifiedEnergisticsMetaTileEntities.ME_ASSEMBLY_LINE_OPTICAL_BUS));
	}
}
