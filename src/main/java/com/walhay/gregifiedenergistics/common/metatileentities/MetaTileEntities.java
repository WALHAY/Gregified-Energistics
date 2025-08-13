package com.walhay.gregifiedenergistics.common.metatileentities;

import static gregtech.common.metatileentities.MetaTileEntities.registerMetaTileEntity;

import com.walhay.gregifiedenergistics.GregifiedEnergisticsMod;
import com.walhay.gregifiedenergistics.common.metatileentities.multiblockparts.MetaTileEntityMEALDataHatch;
import com.walhay.gregifiedenergistics.common.metatileentities.multiblockparts.MetaTileEntityMEALHatch;
import net.minecraft.util.ResourceLocation;

public class MetaTileEntities {
	private static int id = 11000;

	public static MetaTileEntityMEALHatch ME_ASSEMBLY_LINE_HATCH;
	public static MetaTileEntityMEALDataHatch ME_ASSEMBLY_LINE_DATA_HATCH;

	public static void init() {
		ME_ASSEMBLY_LINE_HATCH =
				registerMetaTileEntity(autoId(), new MetaTileEntityMEALHatch(location("me_assembly_line_hatch"), 5));
		ME_ASSEMBLY_LINE_DATA_HATCH = registerMetaTileEntity(
				autoId(), new MetaTileEntityMEALDataHatch(location("me_assembly_line_data_hatch"), 7));
	}

	private static ResourceLocation location(String location) {
		return new ResourceLocation(GregifiedEnergisticsMod.MOD_ID, location);
	}

	private static int autoId() {
		return id++;
	}
}
