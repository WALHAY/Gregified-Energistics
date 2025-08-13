package com.walhay.gregifiedenergistics.mixins.gtceu.pipenet;

import com.walhay.gregifiedenergistics.api.capability.INetRecipeHandler;
import com.walhay.gregifiedenergistics.api.capability.IOpticalNetRecipeHandler;
import com.walhay.gregifiedenergistics.mixins.interfaces.IOpticalRouteAccessor;
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
@Implements(@Interface(iface = INetRecipeHandler.class, prefix = "dataHandler$"))
public abstract class OpticalNetHandlerMixin implements INetRecipeHandler {
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
	@Unique public void onRecipesUpdate(Collection<INetRecipeHandler> seen) {
		traverseOnUpdate(seen);
	}

	@Override
	@Unique public Collection<Recipe> getRecipes(Collection<INetRecipeHandler> seen) {
		var recipes = traverseGetRecipes(seen);
		if (recipes != null) callSetPipesActive();
		return recipes;
	}

	@Unique private void traverseOnUpdate(Collection<INetRecipeHandler> seen) {
		if (callIsNetInvalidForTraversal()) return;

		OpticalRoutePath inv = net.getNetData(pipe.getPipePos(), facing);
		if (inv instanceof IOpticalRouteAccessor accessor) {
			IOpticalNetRecipeHandler handler = accessor.getDataHandler();
			if (handler == null) return;

			if (!handler.isTransmitter()) {
				handler.onRecipesUpdate(seen);
			}
		}
	}

	@Unique private Collection<Recipe> traverseGetRecipes(Collection<INetRecipeHandler> seen) {
		if (callIsNetInvalidForTraversal()) return null;

		OpticalRoutePath inv = net.getNetData(pipe.getPipePos(), facing);
		if (inv instanceof IOpticalRouteAccessor accessor) {
			IOpticalNetRecipeHandler handler = accessor.getDataHandler();
			if (handler == null) return null;

			if (handler.isTransmitter()) {
				return handler.getRecipes(seen);
			}
		}

		return null;
	}
}
