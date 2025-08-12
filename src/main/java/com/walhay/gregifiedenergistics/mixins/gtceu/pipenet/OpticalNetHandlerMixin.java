package com.walhay.gregifiedenergistics.mixins.gtceu.pipenet;

import com.walhay.gregifiedenergistics.api.capability.IOpticalDataHandler;
import gregtech.api.capability.IOpticalDataAccessHatch;
import gregtech.api.recipes.Recipe;
import gregtech.common.pipelike.optical.net.OpticalNetHandler;
import gregtech.common.pipelike.optical.net.OpticalPipeNet;
import gregtech.common.pipelike.optical.net.OpticalRoutePath;
import gregtech.common.pipelike.optical.tile.TileEntityOpticalPipe;
import java.util.Collection;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(OpticalNetHandler.class)
@Implements(@Interface(iface = IOpticalDataHandler.class, prefix = "recipes$"))
public abstract class OpticalNetHandlerMixin implements IOpticalDataHandler {
	@Shadow(remap = false)
	private TileEntityOpticalPipe pipe;

	@Shadow(remap = false)
	@Final
	private World world;

	@Shadow(remap = false)
	@Final
	private EnumFacing facing;

	@Shadow(remap = false)
	private OpticalPipeNet net;

	@Invoker(remap = false)
	protected abstract boolean callIsNetInvalidForTraversal();

	@Invoker(remap = false)
	protected abstract void callSetPipesActive();

	@Override
	@Unique public void onRecipesUpdate(Collection<IOpticalDataHandler> seen) {
		traverseOnUpdate(seen);
	}

	@Override
	@Unique public Collection<Recipe> getRecipes(Collection<IOpticalDataHandler> seen) {
		callSetPipesActive();
		return traverseGetRecipes(seen);
	}

	@Unique private void traverseOnUpdate(Collection<IOpticalDataHandler> seen) {
		if (callIsNetInvalidForTraversal()) return;

		OpticalRoutePath inv = net.getNetData(pipe.getPipePos(), facing);
		if (inv == null) return;

		IOpticalDataAccessHatch hatch = inv.getDataHatch();
		if (hatch == null) return;

		if (!hatch.isTransmitter() && hatch instanceof IOpticalDataHandler handler) {
			handler.onRecipesUpdate(seen);
		}
	}

	@Unique private Collection<Recipe> traverseGetRecipes(Collection<IOpticalDataHandler> seen) {
		if (callIsNetInvalidForTraversal()) return null;

		OpticalRoutePath inv = net.getNetData(pipe.getPipePos(), facing);
		if (inv == null) return null;

		IOpticalDataAccessHatch hatch = inv.getDataHatch();
		if (hatch == null) return null;

		if (hatch.isTransmitter() && hatch instanceof IOpticalDataHandler handler) {
			return handler.getRecipes(seen);
		}
		return null;
	}
}
