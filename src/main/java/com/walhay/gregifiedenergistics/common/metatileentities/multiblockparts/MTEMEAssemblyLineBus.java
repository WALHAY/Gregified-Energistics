package com.walhay.gregifiedenergistics.common.metatileentities.multiblockparts;

import static gregtech.api.GTValues.LuV;

import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.items.misc.ItemEncodedPattern;
import com.walhay.gregifiedenergistics.GregifiedEnergisticsConfig;
import com.walhay.gregifiedenergistics.api.capability.AbstractPatternItemHandler;
import com.walhay.gregifiedenergistics.api.patterns.implementations.DataStickPatternHelper;
import com.walhay.gregifiedenergistics.common.gui.DataStickGridWidget;
import gregtech.api.gui.widgets.AbstractWidgetGroup;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.util.AssemblyLineManager;
import java.io.IOException;
import java.util.Collection;
import javax.annotation.Nonnull;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;

public class MTEMEAssemblyLineBus extends MTEAbstractAssemblyLineBus {

	public static final String PATTERN_INVENTORY_TAG = "PatternInventory";

	private final DataStickHandler patternHandler =
			new DataStickHandler(GregifiedEnergisticsConfig.machineConfig.patternHandlerSize);

	public MTEMEAssemblyLineBus(ResourceLocation metaTileEntityId) {
		super(metaTileEntityId, LuV);
	}

	@Override
	public MetaTileEntity createMetaTileEntity(IGregTechTileEntity metaTileEntity) {
		return new MTEMEAssemblyLineBus(metaTileEntityId);
	}

	@Override
	protected AbstractWidgetGroup createPatternsGrid() {
		int slotsPerLine = GregifiedEnergisticsConfig.guiConfig.patternSlotsPerLine;
		if (slotsPerLine == 0) slotsPerLine = (int) Math.sqrt(patternHandler.getSlots());
		return new DataStickGridWidget(slotsPerLine, patternHandler);
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound data) {
		super.writeToNBT(data);
		data.setTag(PATTERN_INVENTORY_TAG, patternHandler.serializeNBT());
		return data;
	}

	@Override
	public void readFromNBT(NBTTagCompound data) {
		super.readFromNBT(data);
		patternHandler.deserializeNBT(data.getCompoundTag(PATTERN_INVENTORY_TAG));
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
		protected ICraftingPatternDetails getPatternFromStack(ItemStack stack) {
			if (stack.isEmpty()) return null;

			if (AssemblyLineManager.isStackDataItem(stack, true)) {
				DataStickPatternHelper helper = new DataStickPatternHelper(stack);
				helper.injectSubstitutions(substitutionStorage);
				return helper;
			} else if (stack.getItem() instanceof ItemEncodedPattern itemPattern) {
				return itemPattern.getPatternForItem(stack, getWorld());
			}
			return null;
		}

		@Override
		protected void onPatternUpdate() {
			notifyPatternChange();
		}

		@Override
		public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
			return super.isItemValid(slot, stack) || AssemblyLineManager.isStackDataItem(stack, true);
		}
	}
}
