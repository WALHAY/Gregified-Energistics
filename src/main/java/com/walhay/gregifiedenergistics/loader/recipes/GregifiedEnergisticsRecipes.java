package com.walhay.gregifiedenergistics.loader.recipes;

import static com.walhay.gregifiedenergistics.common.metatileentities.GregifiedEnergisticsMetaTileEntities.*;
import static gregtech.api.GTValues.L;
import static gregtech.api.GTValues.LuV;
import static gregtech.api.GTValues.VA;
import static gregtech.api.recipes.RecipeMaps.ASSEMBLY_LINE_RECIPES;
import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.ore.OrePrefix.*;
import static gregtech.common.items.MetaItems.*;
import static gregtech.common.metatileentities.MetaTileEntities.HULL;

import appeng.api.AEApi;
import gregtech.api.unification.material.MarkerMaterials.Tier;
import net.minecraft.item.Item;

public class GregifiedEnergisticsRecipes {

	public static void load() {
		Item iface = AEApi.instance().definitions().blocks().iface().maybeItem().get();

		ASSEMBLY_LINE_RECIPES
				.recipeBuilder()
				.input(HULL[LuV])
				.input(iface, 4)
				.input(ROBOT_ARM_LuV, 2)
				.input(ELECTRIC_MOTOR_LuV, 4)
				.input(CONVEYOR_MODULE_LuV, 2)
				.input(circuit, Tier.LuV, 2)
				.input(circuit, Tier.IV, 4)
				.input(wireGtQuadruple, IndiumTinBariumTitaniumCuprate, 8)
				.fluidInputs(SolderingAlloy.getFluid(16 * L))
				.fluidInputs(VanadiumGallium.getFluid(16 * L))
				.output(ME_ASSEBLY_LINE_BUS)
				.EUt(VA[LuV])
				.duration(1200)
				.scannerResearch(r -> r.researchStack(iface.getDefaultInstance())
						.duration(3600)
						.EUt(VA[LuV]))
				.buildAndRegister();
	}
}
