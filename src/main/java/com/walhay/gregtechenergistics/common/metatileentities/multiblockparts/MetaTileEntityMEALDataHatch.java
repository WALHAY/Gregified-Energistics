package com.walhay.gregtechenergistics.common.metatileentities.multiblockparts;

import static com.walhay.gregtechenergistics.api.capability.GTEDataCodes.CHANGE_OPTICAL_SIDE;

import appeng.api.networking.crafting.ICraftingPatternDetails;
import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.walhay.gregtechenergistics.api.capability.GTEDataCodes;
import com.walhay.gregtechenergistics.api.capability.GregTechEnergisticsCapabilities;
import com.walhay.gregtechenergistics.api.capability.IOpticalDataHandler;
import com.walhay.gregtechenergistics.api.capability.IRecipeMixinAccessor;
import com.walhay.gregtechenergistics.api.capability.ISubstitutionHandler;
import com.walhay.gregtechenergistics.api.capability.impl.RecipePatternHelper;
import com.walhay.gregtechenergistics.common.gui.GhostGridWidget;

import gregtech.GregTechMod;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IDataAccessHatch;
import gregtech.api.capability.IOpticalDataAccessHatch;
import gregtech.api.gui.Widget;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.recipes.ingredients.GTRecipeInput;
import gregtech.api.util.GTLog;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.pipelike.optical.tile.TileEntityOpticalPipe;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
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
		implements IOpticalDataHandler, IOpticalDataAccessHatch {

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
		} else if (dataId == GTEDataCodes.PATTERNS_CHANGE) {
			patterns.clear();

			int size = buf.readInt();
			if (size == 0) return;

			IntOpenHashSet ids = new IntOpenHashSet();
			for (int i = 0; i < size; ++i) ids.add(buf.readInt());

			patterns = RecipeMaps.ASSEMBLY_LINE_RECIPES.getRecipeList().stream()
					.filter(recipe -> ids.contains(((IRecipeMixinAccessor) recipe).getRecipeId()))
					.map(RecipePatternHelper::new)
					.collect(Collectors.toList());
		}
	}

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		if (facing == opticalFacing && capability == GregTechEnergisticsCapabilities.CAPABILITY_DATA_HANDLER) {
			return GregTechEnergisticsCapabilities.CAPABILITY_DATA_HANDLER.cast(this);
		} else if (facing == opticalFacing && capability == GregtechTileCapabilities.CAPABILITY_DATA_ACCESS) {
			return GregtechTileCapabilities.CAPABILITY_DATA_ACCESS.cast(this);
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
			IOpticalDataHandler data = (IOpticalDataHandler)
					te.getCapability(GregtechTileCapabilities.CAPABILITY_DATA_ACCESS, opticalFacing.getOpposite());

			if (data == null) return;

			var recipes = data.getRecipes();
			if (recipes == null) {
				patterns = Collections.emptyList();
				return;
			}

			patterns = recipes.stream().map(RecipePatternHelper::new).collect(Collectors.toList());

			writeCustomData(GTEDataCodes.PATTERNS_CHANGE, buf -> {
				buf.writeInt(recipes.size());
				for (Recipe recipe : recipes) {
					buf.writeInt(((IRecipeMixinAccessor) recipe).getRecipeId());
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
	public boolean isCreative() {
		return false;
	}

	@Override
	public boolean isRecipeAvailable(Recipe arg0, Collection<IDataAccessHatch> arg1) {
		return false;
	}

	@Override
	public void onRecipesUpdate(Collection<IOpticalDataHandler> seen) {
		if(seen.contains(this)) return;
		seen.add(this);
		updatePatternData();
		notifyPatternChange();
	}

	@Override
	public Collection<Recipe> getRecipes(Collection<IDataAccessHatch> seen) {
		seen.add(this);
		return null;
	}

	@Override
	public boolean isTransmitter() {
		return false;
	}
}
