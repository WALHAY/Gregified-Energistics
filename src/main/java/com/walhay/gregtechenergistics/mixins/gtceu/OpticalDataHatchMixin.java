package com.walhay.gregtechenergistics.mixins.gtceu;

import com.walhay.gregtechenergistics.api.capability.IOpticalDataHandler;
import com.walhay.gregtechenergistics.mixins.interfaces.IDataBankUpdateHandler;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IDataAccessHatch;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.api.recipes.Recipe;
import gregtech.common.metatileentities.multi.electric.MetaTileEntityDataBank;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityOpticalDataHatch;
import gregtech.common.pipelike.optical.tile.TileEntityOpticalPipe;
import java.util.Collection;
import java.util.LinkedList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(MetaTileEntityOpticalDataHatch.class)
public abstract class OpticalDataHatchMixin implements IOpticalDataHandler {

	@Shadow(remap = false)
	private boolean isTransmitter;

	@Override
	public Collection<Recipe> getRecipes(Collection<IDataAccessHatch> seen) {
		MetaTileEntityOpticalDataHatch hatch = (MetaTileEntityOpticalDataHatch) (Object) this;

		seen.add(this);
		EnumFacing facing = hatch.getFrontFacing();
		if (hatch.isAttachedToMultiBlock()) {
			if (hatch.isTransmitter()) {
				MultiblockControllerBase controller = hatch.getController();
				if (!controller.isActive()) return null;

				var recipes = getRecipes(controller.getAbilities(MultiblockAbility.DATA_ACCESS_HATCH), seen);
				recipes.addAll(getRecipes(controller.getAbilities(MultiblockAbility.OPTICAL_DATA_RECEPTION), seen));
				return recipes;
			} else {
				BlockPos pos = hatch.getPos();
				TileEntity tileEntity = hatch.getWorld().getTileEntity(pos.offset(facing));
				if (tileEntity == null) return null;

				if (tileEntity instanceof TileEntityOpticalPipe) {
					IOpticalDataHandler cap = (IOpticalDataHandler) tileEntity.getCapability(
							GregtechTileCapabilities.CAPABILITY_DATA_ACCESS, facing.getOpposite());

					if (cap == null) return null;
					return cap.getRecipes(seen);
				}
			}
		}
		return null;
	}

	private static Collection<Recipe> getRecipes(
			Iterable<? extends IDataAccessHatch> hatches, Collection<IDataAccessHatch> seen) {
		var result = new LinkedList<Recipe>();
		for (IDataAccessHatch hatch : hatches) {
			if (seen.contains(hatch)) continue;

			var recipes = ((IOpticalDataHandler) hatch).getRecipes(seen);
			if (recipes != null) result.addAll(recipes);
		}
		return result;
	}

	@Override
	@Unique public void onRecipesUpdate() {
		MetaTileEntityOpticalDataHatch hatch = (MetaTileEntityOpticalDataHatch) (Object) this;

		if (isTransmitter) {
			EnumFacing facing = hatch.getFrontFacing();

			BlockPos pos = hatch.getPos();
			TileEntity te = hatch.getWorld().getTileEntity(pos.offset(facing));

			if (te instanceof TileEntityOpticalPipe pipe) {
				IDataAccessHatch data =
						pipe.getCapability(GregtechTileCapabilities.CAPABILITY_DATA_ACCESS, facing.getOpposite());

				IOpticalDataHandler updated = (IOpticalDataHandler) data;

				updated.onRecipesUpdate();
			}
		} else {
			if (hatch.getController() instanceof MetaTileEntityDataBank dataBank) {
				((IDataBankUpdateHandler) dataBank).updateData();
			}
		}
	}
}
