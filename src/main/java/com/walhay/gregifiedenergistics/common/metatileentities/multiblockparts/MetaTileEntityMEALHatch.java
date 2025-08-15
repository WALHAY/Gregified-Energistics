package com.walhay.gregifiedenergistics.common.metatileentities.multiblockparts;

import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.items.misc.ItemEncodedPattern;
import com.walhay.gregifiedenergistics.api.capability.AbstractPatternItemHandler;
import com.walhay.gregifiedenergistics.api.patterns.ISubstitutionHandler;
import com.walhay.gregifiedenergistics.api.patterns.impl.DataStickPatternHelper;
import com.walhay.gregifiedenergistics.common.gui.GhostGridWidget;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.ScrollableListWidget;
import gregtech.api.gui.widgets.SlotWidget;
import gregtech.api.gui.widgets.WidgetGroup;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.recipes.ingredients.GTRecipeInput;
import gregtech.api.util.AssemblyLineManager;
import gregtech.api.util.Position;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;

public class MetaTileEntityMEALHatch extends MetaTileEntityAbstractAssemblyLineHatch {

	private final DataStickHandler patternHandler = new DataStickHandler(16);

	public MetaTileEntityMEALHatch(ResourceLocation metaTileEntityId, int tier) {
		super(metaTileEntityId, tier);
	}

	@Override
	public MetaTileEntity createMetaTileEntity(IGregTechTileEntity metaTileEntity) {
		return new MetaTileEntityMEALHatch(metaTileEntityId, getTier());
	}

	@Override
	protected boolean containsPattern(ICraftingPatternDetails pattern) {
		for (ICraftingPatternDetails details : getPatterns()) {
			if (details == null) continue;

			if (details.equals(pattern)) return true;

			if (details instanceof DataStickPatternHelper helper) if (helper.contains(pattern)) return true;
		}
		return false;
	}

	@Override
	protected String usedPatternsInfo() {
		return String.format(
				"%d/%d",
				patternHandler.getPatterns().stream().filter(Objects::nonNull).count(), patternHandler.getSlots());
	}

	@Override
	protected Widget createPatternListWidget(int x, int y) {
		List<GTRecipeInput> inputs = getPatterns().stream()
				.filter(ISubstitutionHandler.class::isInstance)
				.map(ISubstitutionHandler.class::cast)
				.map(ISubstitutionHandler::getSubstitutions)
				.flatMap(Collection::stream)
				.collect(Collectors.toList());

		WidgetGroup group = new WidgetGroup(new Position(x, y));
		ScrollableListWidget list = new ScrollableListWidget(0, 0, 30, 50);
		GhostGridWidget grid = new GhostGridWidget(40, 0, substitutionStorage, this);

		for (int i = 0; i < patternHandler.getSlots(); ++i) {
			list.addWidget(new SlotWidget(patternHandler, i, 0, 0).setBackgroundTexture(GuiTextures.SLOT));
		}

		grid.initGrid(inputs);

		group.addWidget(list);
		group.addWidget(grid);

		return group;
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound data) {
		super.writeToNBT(data);
		data.setTag("patterns", patternHandler.serializeNBT());
		return data;
	}

	@Override
	public void readFromNBT(NBTTagCompound data) {
		super.readFromNBT(data);
		patternHandler.deserializeNBT(data.getCompoundTag("patterns"));
	}

	@Override
	public void writeInitialSyncData(PacketBuffer buf) {
		super.writeInitialSyncData(buf);
		buf.writeCompoundTag(patternHandler.serializeNBT());
	}

	@Override
	public void receiveInitialSyncData(PacketBuffer buf) {
		super.receiveInitialSyncData(buf);
		try {
			patternHandler.deserializeNBT(buf.readCompoundTag());
		} catch (IOException ignored) {
			// :#
		}
	}

	@Override
	public Collection<? extends ICraftingPatternDetails> getPatterns() {
		return patternHandler.getPatterns();
	}

	class DataStickHandler extends AbstractPatternItemHandler {

		public DataStickHandler(int size) {
			super(size);
		}

		@Override
		protected ICraftingPatternDetails getPatternFromItemStack(ItemStack stack) {
			if (stack.isEmpty()) return null;

			if (AssemblyLineManager.isStackDataItem(stack, true)) {
				return new DataStickPatternHelper(stack);
			} else if (stack.getItem() instanceof ItemEncodedPattern itemPattern) {
				return itemPattern.getPatternForItem(stack, getWorld());
			}
			return null;
		}

		@Override
		protected void onContentsChanged(int slot) {
			super.onContentsChanged(slot);
			notifyPatternChange();
		}

		@Override
		public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
			return super.isItemValid(slot, stack) || AssemblyLineManager.isStackDataItem(stack, true);
		}
	}
}
