package com.walhay.gregtechenergistics.mixins.gtceu;

import com.walhay.gregtechenergistics.api.capability.IOpticalDataHandler;
import com.walhay.gregtechenergistics.mixins.interfaces.IOpticalHandlerProperty;
import gregtech.api.capability.IDataAccessHatch;
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
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(OpticalNetHandler.class)
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

	@Inject(method = "<init>", at = @At("TAIL"), remap = false)
	public void constructorSetHandler(CallbackInfo ci) {
		((IOpticalHandlerProperty) net).setHandler(this);
	}

	@Override
	@Unique public void onRecipesUpdate() {
		traverseOnUpdate();
	}

	@Override
	@Unique public Collection<Recipe> getRecipes(Collection<IDataAccessHatch> seen) {
		Collection<Recipe> recipes = traverseGetRecipes(seen);
		if (recipes != null) callSetPipesActive();
		return recipes;
	}

	@Unique private void traverseOnUpdate() {
		if (callIsNetInvalidForTraversal()) return;

		OpticalRoutePath inv = net.getNetData(pipe.getPipePos(), facing);
		if (inv == null) return;

		IOpticalDataAccessHatch hatch = inv.getDataHatch();
		if (hatch == null) return;

		if (!hatch.isTransmitter()) {
			((IOpticalDataHandler) hatch).onRecipesUpdate();
		}
	}

	@Unique private Collection<Recipe> traverseGetRecipes(Collection<IDataAccessHatch> seen) {
		if (callIsNetInvalidForTraversal()) return null;

		OpticalRoutePath inv = net.getNetData(pipe.getPipePos(), facing);
		if (inv == null) return null;

		IOpticalDataAccessHatch hatch = inv.getDataHatch();
		if (hatch == null) return null;

		if (hatch.isTransmitter()) {
			return ((IOpticalDataHandler) hatch).getRecipes(seen);
		}
		return null;
	}
}
