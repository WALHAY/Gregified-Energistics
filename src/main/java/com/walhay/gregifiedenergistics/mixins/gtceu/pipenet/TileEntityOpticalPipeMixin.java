package com.walhay.gregifiedenergistics.mixins.gtceu.pipenet;

import com.walhay.gregifiedenergistics.api.capability.GECapabilities;
import com.walhay.gregifiedenergistics.api.capability.IOpticalDataHandler;
import gregtech.api.pipenet.tile.TileEntityPipeBase;
import gregtech.api.recipes.Recipe;
import gregtech.common.pipelike.optical.OpticalPipeProperties;
import gregtech.common.pipelike.optical.OpticalPipeType;
import gregtech.common.pipelike.optical.net.OpticalNetHandler;
import gregtech.common.pipelike.optical.tile.TileEntityOpticalPipe;
import java.util.Collection;
import java.util.EnumMap;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TileEntityOpticalPipe.class)
public abstract class TileEntityOpticalPipeMixin extends TileEntityPipeBase<OpticalPipeType, OpticalPipeProperties> {

	@Unique private final IOpticalDataHandler clientRecipesHandler = new DefaultRecipesHandler();

	@Shadow(remap = false)
	@Final
	private EnumMap<EnumFacing, OpticalNetHandler> handlers;

	@Shadow(remap = false)
	private OpticalNetHandler defaultHandler;

	@Invoker(remap = false)
	protected abstract void invokeInitHandlers();

	@Invoker(remap = false)
	protected abstract void invokeCheckNetwork();

	@Inject(method = "getCapabilityInternal", at = @At(value = "HEAD"), remap = false, cancellable = true)
	private <I> void injectRecipeHandler(Capability<I> capability, EnumFacing facing, CallbackInfoReturnable<I> cir) {
		if (capability == GECapabilities.CAPABILITY_DATA_HANDLER) {
			if (world.isRemote) {
				cir.setReturnValue(GECapabilities.CAPABILITY_DATA_HANDLER.cast(clientRecipesHandler));
			}

			if (handlers.isEmpty()) invokeInitHandlers();

			invokeCheckNetwork();
			OpticalNetHandler handler = handlers.getOrDefault(facing, defaultHandler);
			if (handler instanceof IOpticalDataHandler dataHandler)
				cir.setReturnValue(GECapabilities.CAPABILITY_DATA_HANDLER.cast(dataHandler));
		}
	}

	@Unique private static class DefaultRecipesHandler implements IOpticalDataHandler {

		@Override
		public void onRecipesUpdate(Collection<IOpticalDataHandler> seen) {}

		@Override
		public Collection<Recipe> getRecipes(Collection<IOpticalDataHandler> seen) {
			return null;
		}
	}
}
