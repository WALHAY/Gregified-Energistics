package com.walhay.gregtechenergistics.common.metatileentities.multiblockparts;

import static com.walhay.gregtechenergistics.api.capability.GTEDataCodes.CHANGE_OPTICAL_SIDE;

import appeng.api.networking.crafting.ICraftingPatternDetails;
import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.walhay.gregtechenergistics.GregTechEnergisticsConfig;
import com.walhay.gregtechenergistics.api.capability.GTEDataCodes;
import com.walhay.gregtechenergistics.api.capability.IRecipeMixinAccessor;
import com.walhay.gregtechenergistics.api.capability.ISubstitutionHandler;
import com.walhay.gregtechenergistics.api.capability.impl.RecipePatternHelper;
import com.walhay.gregtechenergistics.common.gui.GhostGridWidget;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IDataAccessHatch;
import gregtech.api.gui.Widget;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.recipes.ingredients.GTRecipeInput;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.pipelike.optical.tile.TileEntityOpticalPipe;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;

public class MetaTileEntityMEALDataHatch extends MetaTileEntityAbstractAssemblyLineHatch {

	private final RecipeSyncHandler recipeSyncHandler = new RecipeSyncHandler();

	private final Set<RecipePatternHelper> patterns = new HashSet<>();
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
				updatePatternData();
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
			recipeSyncHandler.decode(buf);
			if (!recipeSyncHandler.isEmpty()) {
				recipeSyncHandler.getAddition().stream()
						.map(RecipePatternHelper::new)
						.forEach(patterns::add);

				recipeSyncHandler.getDeletion().stream()
						.map(RecipePatternHelper::new)
						.forEach(patterns::remove);
			}
		}
	}

	@Override
	public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
		super.renderMetaTileEntity(renderState, translation, pipeline);
		Textures.OPTICAL_DATA_ACCESS_HATCH.renderSided(opticalFacing, renderState, translation, pipeline);
	}

	@Override
	public void update() {
		super.update();
		if (updateTick % GregTechEnergisticsConfig.getUpdateTime() == 0 || isFirstTick()) {
			updatePatternData();
		}
	}

	private void updatePatternData() {
		if (!isAttachedToMultiBlock()) return;

		TileEntity te = getWorld().getTileEntity(getPos().offset(opticalFacing));
		if (te instanceof TileEntityOpticalPipe) {
			IDataAccessHatch data =
					te.getCapability(GregtechTileCapabilities.CAPABILITY_DATA_ACCESS, opticalFacing.getOpposite());

			if (data == null) return;

			RecipeMaps.ASSEMBLY_LINE_RECIPES.getRecipeList().stream()
					.filter(data::isRecipeAvailable)
					.filter(r -> patterns.stream()
							.map(RecipePatternHelper::getRecipe)
							.noneMatch(rec -> rec.equals(r)))
					.forEach(r -> {
						patterns.add(new RecipePatternHelper(r));
						recipeSyncHandler.addAddition(r);
					});

			patterns.stream()
					.map(RecipePatternHelper::getRecipe)
					.filter(r -> !data.isRecipeAvailable(r))
					.forEach(recipeSyncHandler::addDeletion);
		}
		if (!recipeSyncHandler.isEmpty()) {
			patterns.removeIf(r -> recipeSyncHandler.getDeletion().contains(r.getRecipe()));
			writeCustomData(GTEDataCodes.PATTERNS_CHANGE, recipeSyncHandler::encode);
			notifyPatternChange();
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

	private class RecipeSyncHandler {

		private Set<Recipe> addition = new HashSet<>();
		private Set<Recipe> deletion = new HashSet<>();

		private void clear() {
			addition.clear();
			deletion.clear();
		}

		private boolean isEmpty() {
			return addition.isEmpty() && deletion.isEmpty();
		}

		private void addDeletion(Recipe recipe) {
			deletion.add(recipe);
		}

		private void addAddition(Recipe recipe) {
			addition.add(recipe);
		}

		private Set<Recipe> getAddition() {
			return addition;
		}

		private Set<Recipe> getDeletion() {
			return deletion;
		}

		private void decode(PacketBuffer buf) {
			clear();
			decodeRecipes(addition, buf);
			decodeRecipes(deletion, buf);
		}

		private void encode(PacketBuffer buf) {
			encodeRecipes(addition, buf);
			encodeRecipes(deletion, buf);
			clear();
		}

		private void decodeRecipes(Set<Recipe> recipes, PacketBuffer buf) {
			int size = buf.readShort();
			if(size == 0) return;
			for (int i = 0; i < size; ++i) {
				int id = buf.readInt();
				RecipeMaps.ASSEMBLY_LINE_RECIPES.getRecipeList().stream()
						.filter(recipe -> ((IRecipeMixinAccessor) recipe).getRecipeId() == id)
						.forEach(recipes::add);
			}
		}

		public void encodeRecipes(Set<Recipe> recipes, PacketBuffer buf) {
			int size = recipes.size();
			buf.writeShort(size);
			if(size == 0) return;

			recipes.stream()
					.map(recipe -> ((IRecipeMixinAccessor) recipe).getRecipeId())
					.forEach(buf::writeInt);
		}
	}
}
