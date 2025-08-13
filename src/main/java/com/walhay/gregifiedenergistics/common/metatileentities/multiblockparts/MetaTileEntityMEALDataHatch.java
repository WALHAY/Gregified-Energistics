package com.walhay.gregifiedenergistics.common.metatileentities.multiblockparts;

import static com.walhay.gregifiedenergistics.api.capability.GEDataCodes.CHANGE_OPTICAL_SIDE;

import appeng.api.networking.crafting.ICraftingPatternDetails;
import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.walhay.gregifiedenergistics.api.capability.GECapabilities;
import com.walhay.gregifiedenergistics.api.capability.GEDataCodes;
import com.walhay.gregifiedenergistics.api.capability.IOpticalDataHandler;
import com.walhay.gregifiedenergistics.api.capability.IRecipeAccessor;
import com.walhay.gregifiedenergistics.api.capability.IRecipeMapAccessor;
import com.walhay.gregifiedenergistics.api.patterns.ISubstitutionHandler;
import com.walhay.gregifiedenergistics.api.patterns.impl.RecipePatternHelper;
import com.walhay.gregifiedenergistics.common.gui.GhostGridWidget;
import gregtech.api.gui.Widget;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.recipes.ingredients.GTRecipeInput;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.pipelike.optical.tile.TileEntityOpticalPipe;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;

public class MetaTileEntityMEALDataHatch extends MetaTileEntityAbstractAssemblyLineHatch
		implements IOpticalDataHandler {

	private List<RecipePatternHelper> patterns = Collections.emptyList();
	private EnumFacing opticalFacing = EnumFacing.DOWN;

	public MetaTileEntityMEALDataHatch(ResourceLocation metaTileEntityId, int tier) {
		super(metaTileEntityId, tier);
	}

	@Override
	public boolean onScrewdriverClick(
			EntityPlayer playerIn, EnumHand hand, EnumFacing facing, CuboidRayTraceResult hitResult) {
		if (playerIn.isSneaking()) {
			if (opticalFacing != facing) {
				opticalFacing = facing;
				writeCustomData(CHANGE_OPTICAL_SIDE, buf -> buf.writeEnumValue(opticalFacing));
			}

			return true;
		}
		return super.onScrewdriverClick(playerIn, hand, facing, hitResult);
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound data) {
		super.writeToNBT(data);
		data.setString("opticalFacing", opticalFacing.toString());
		return data;
	}

	@Override
	public void readFromNBT(NBTTagCompound data) {
		super.readFromNBT(data);
		opticalFacing = EnumFacing.byName(data.getString("opticalFacing"));
	}

	@Override
	public void writeInitialSyncData(PacketBuffer buf) {
		super.writeInitialSyncData(buf);
		buf.writeEnumValue(opticalFacing);
	}

	@Override
	public void receiveInitialSyncData(PacketBuffer buf) {
		super.receiveInitialSyncData(buf);
		opticalFacing = buf.readEnumValue(EnumFacing.class);
	}

	@Override
	public void receiveCustomData(int dataId, PacketBuffer buf) {
		super.receiveCustomData(dataId, buf);
		if (dataId == CHANGE_OPTICAL_SIDE) {
			opticalFacing = buf.readEnumValue(EnumFacing.class);
			scheduleRenderUpdate();
		} else if (dataId == GEDataCodes.PATTERNS_CHANGE) {
			patterns.clear();

			int size = buf.readInt();
			if (size == 0) return;

			List<Integer> ids = new ArrayList<>(size);
			for (int i = 0; i < size; ++i) ids.add(buf.readInt());

			if (RecipeMaps.ASSEMBLY_LINE_RECIPES instanceof IRecipeMapAccessor recipeMap) {
				patterns = ids.stream()
						.map(recipeMap::getRecipeById)
						.map(RecipePatternHelper::new)
						.collect(Collectors.toList());
			}
		}
	}

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		if (facing == opticalFacing && capability == GECapabilities.CAPABILITY_DATA_HANDLER) {
			return GECapabilities.CAPABILITY_DATA_HANDLER.cast(this);
		}
		return super.getCapability(capability, facing);
	}

	@Override
	public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
		super.renderMetaTileEntity(renderState, translation, pipeline);
		Textures.OPTICAL_DATA_ACCESS_HATCH.renderSided(opticalFacing, renderState, translation, pipeline);
	}

	private void updatePatternData() {
		if (!isAttachedToMultiBlock()) return;

		TileEntity te = getWorld().getTileEntity(getPos().offset(opticalFacing));
		if (te instanceof TileEntityOpticalPipe) {
			IOpticalDataHandler data =
					te.getCapability(GECapabilities.CAPABILITY_DATA_HANDLER, opticalFacing.getOpposite());

			if (data == null) return;

			var recipes = data.getRecipes();
			if (recipes == null) {
				patterns = Collections.emptyList();
				writeCustomData(GEDataCodes.PATTERNS_CHANGE, buf -> buf.writeInt(0));
				return;
			}

			patterns = recipes.stream().map(RecipePatternHelper::new).collect(Collectors.toList());

			writeCustomData(GEDataCodes.PATTERNS_CHANGE, buf -> {
				buf.writeInt(recipes.size());
				for (Recipe recipe : recipes) {
					if (recipe instanceof IRecipeAccessor accessor) buf.writeInt(accessor.getRecipeId());
				}
			});
		}
	}

	@Override
	protected String usedPatternsInfo() {
		return Integer.toString(patterns.size());
	}

	@Override
	protected Widget createPatternListWidget(int x, int y) {
		Set<GTRecipeInput> inputs = getPatterns().stream()
				.filter(ISubstitutionHandler.class::isInstance)
				.map(ISubstitutionHandler.class::cast)
				.map(ISubstitutionHandler::getSubstitutions)
				.flatMap(Collection::stream)
				.collect(Collectors.toSet());

		GhostGridWidget grid = new GhostGridWidget(10, 40, substitutionStorage, this);

		grid.initGrid(inputs);

		return grid;
	}

	@Override
	public MetaTileEntity createMetaTileEntity(IGregTechTileEntity metaTileEntity) {
		return new MetaTileEntityMEALDataHatch(metaTileEntityId, getTier());
	}

	@Override
	public Collection<? extends ICraftingPatternDetails> getPatterns() {
		return patterns;
	}

	@Override
	public void onRecipesUpdate(Collection<IOpticalDataHandler> seen) {
		seen.add(this);
		updatePatternData();
		notifyPatternChange();
	}

	@Override
	public Collection<Recipe> getRecipes(Collection<IOpticalDataHandler> seen) {
		seen.add(this);
		return null;
	}

	@Override
	public boolean isTransmitter() {
		return false;
	}
}
