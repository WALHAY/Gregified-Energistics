package com.walhay.gregtechenergistics.mixins.gtceu;

import static com.walhay.gregtechenergistics.common.metatileentities.MetaTileEntities.ME_ASSEMBLY_LINE_DATA_HATCH;
import static com.walhay.gregtechenergistics.common.metatileentities.MetaTileEntities.ME_ASSEMBLY_LINE_HATCH;
import static gregtech.api.metatileentity.multiblock.MultiblockControllerBase.abilities;
import static gregtech.api.metatileentity.multiblock.MultiblockControllerBase.any;
import static gregtech.api.metatileentity.multiblock.MultiblockControllerBase.metaTileEntities;
import static gregtech.api.metatileentity.multiblock.MultiblockControllerBase.states;
import static gregtech.api.util.RelativeDirection.FRONT;
import static gregtech.api.util.RelativeDirection.RIGHT;
import static gregtech.api.util.RelativeDirection.UP;
import static gregtech.common.metatileentities.MetaTileEntities.ASSEMBLY_LINE;

import gregtech.api.GTValues;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.TraceabilityPredicate;
import gregtech.common.ConfigHolder;
import gregtech.common.blocks.BlockGlassCasing;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.BlockMultiblockCasing;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.metatileentities.MetaTileEntities;
import gregtech.common.metatileentities.multi.electric.MetaTileEntityAssemblyLine;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityMultiFluidHatch;
import net.minecraft.block.state.IBlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(MetaTileEntityAssemblyLine.class)
public class AssemblyLineMixin {

	/**
	 * @reason inject AL Hatch into structure
	 * @author WALHAY Change it later to get more perfomance
	 */
	@Overwrite(remap = false)
	protected BlockPattern createStructurePattern() {
		FactoryBlockPattern pattern = FactoryBlockPattern.start(FRONT, UP, RIGHT)
				.aisle("FEF", "RTR", "SAG", " Y ")
				.aisle("FIF", "RTR", "DAG", " Y ")
				.setRepeatable(3, 15)
				.aisle("FOF", "RTR", "DAG", " Y ")
				.where('S', ASSEMBLY_LINE.selfPredicate())
				.where(
						'E',
						metaTileEntities(
								MetaTileEntities.ITEM_IMPORT_BUS[GTValues.ULV],
								ME_ASSEMBLY_LINE_DATA_HATCH,
								ME_ASSEMBLY_LINE_HATCH))
				.where(
						'F',
						states(getCasingState())
								.or(ASSEMBLY_LINE.autoAbilities(false, true, false, false, false, false, false))
								.or(fluidInputPredicate()))
				.where(
						'O',
						abilities(MultiblockAbility.EXPORT_ITEMS)
								.addTooltips("gregtech.multiblock.pattern.location_end"))
				.where(
						'Y',
						states(getCasingState())
								.or(abilities(MultiblockAbility.INPUT_ENERGY)
										.setMinGlobalLimited(1)
										.setMaxGlobalLimited(3)))
				.where('I', metaTileEntities(MetaTileEntities.ITEM_IMPORT_BUS[GTValues.ULV]))
				.where('G', states(getGrateState()))
				.where(
						'A',
						states(MetaBlocks.MULTIBLOCK_CASING.getState(
								BlockMultiblockCasing.MultiblockCasingType.ASSEMBLY_CONTROL)))
				.where('R', states(MetaBlocks.TRANSPARENT_CASING.getState(BlockGlassCasing.CasingType.LAMINATED_GLASS)))
				.where(
						'T',
						states(MetaBlocks.MULTIBLOCK_CASING.getState(
								BlockMultiblockCasing.MultiblockCasingType.ASSEMBLY_LINE_CASING)))
				.where('D', dataHatchPredicate())
				.where(' ', any());
		return pattern.build();
	}

	protected IBlockState getCasingState() {
		return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STEEL_SOLID);
	}

	protected IBlockState getGrateState() {
		return MetaBlocks.MULTIBLOCK_CASING.getState(BlockMultiblockCasing.MultiblockCasingType.GRATE_CASING);
	}

	protected TraceabilityPredicate fluidInputPredicate() {
		// block multi-fluid hatches if ordered fluids is enabled
		if (ConfigHolder.machines.orderedFluidAssembly) {
			return metaTileEntities(MultiblockAbility.REGISTRY.get(MultiblockAbility.IMPORT_FLUIDS).stream()
							.filter(mte -> !(mte instanceof MetaTileEntityMultiFluidHatch))
							.toArray(MetaTileEntity[]::new))
					.setMaxGlobalLimited(4);
		}
		return abilities(MultiblockAbility.IMPORT_FLUIDS);
	}

	protected TraceabilityPredicate dataHatchPredicate() {
		// if research is enabled, require the data hatch, otherwise use a grate instead
		if (ConfigHolder.machines.enableResearch) {
			return abilities(MultiblockAbility.DATA_ACCESS_HATCH, MultiblockAbility.OPTICAL_DATA_RECEPTION)
					.setExactLimit(1)
					.or(states(getGrateState()));
		}
		return states(getGrateState());
	}
}
