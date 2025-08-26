package com.walhay.gregifiedenergistics.common.metatileentities.multiblockparts;

import static com.walhay.gregifiedenergistics.api.patterns.substitutions.SubstitutionStorage.STORAGE_TAG;
import static com.walhay.gregifiedenergistics.api.util.BlockingMode.BLOCKING_MODE_TAG;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.crafting.ICraftingProviderHelper;
import appeng.api.networking.events.MENetworkCraftingPatternChange;
import appeng.api.storage.channels.IFluidStorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.me.GridAccessException;
import appeng.util.InventoryAdaptor;
import appeng.util.Platform;
import appeng.util.inv.AdaptorItemHandler;
import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.walhay.gregifiedenergistics.GregifiedEnergisticsConfig;
import com.walhay.gregifiedenergistics.api.metatileentity.MetaTileEntityCraftingProvider;
import com.walhay.gregifiedenergistics.api.patterns.AbstractPatternHelper;
import com.walhay.gregifiedenergistics.api.patterns.ISubstitutionNotifiable;
import com.walhay.gregifiedenergistics.api.patterns.ISubstitutionStorage;
import com.walhay.gregifiedenergistics.api.patterns.substitutions.SubstitutionStorage;
import com.walhay.gregifiedenergistics.api.util.BlockingMode;
import com.walhay.gregifiedenergistics.client.gui.GregifiedEnergisticsGuiTextures;
import com.walhay.gregifiedenergistics.client.render.GregifiedEnergisticsTextures;
import com.walhay.gregifiedenergistics.common.gui.SubstitutionListWidget;
import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.impl.MultiblockRecipeLogic;
import gregtech.api.capability.impl.NotifiableItemStackHandler;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.ModularUI.Builder;
import gregtech.api.gui.widgets.*;
import gregtech.api.gui.widgets.TabGroup.TabLocation;
import gregtech.api.gui.widgets.tab.ItemTabInfo;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import gregtech.client.renderer.texture.cube.SimpleOverlayRenderer;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.items.IItemHandlerModifiable;

public abstract class MTEAbstractAssemblyLineBus extends MetaTileEntityCraftingProvider<IAEFluidStack>
		implements IMultiblockAbilityPart<IItemHandlerModifiable>, ISubstitutionNotifiable {

	public static final String WORKING_ENABLED_TAG = "WorkingEnabled";
	public static final String USE_FLUID_TAG = "FluidMode";

	private Int2ObjectOpenHashMap<ItemStack> waitingToSend;
	private Int2ObjectOpenHashMap<FluidStack> fluidWaitingToSend;
	private BlockingMode blockingMode = BlockingMode.NO_BLOCKING;
	private boolean useFluids = true;
	private boolean workingEnabled = true;
	protected final ISubstitutionStorage substitutionStorage = new SubstitutionStorage(this);

	/* ###########################
	###     MTE METHODS     ###
	########################### */

	public MTEAbstractAssemblyLineBus(ResourceLocation metaTileEntityId, int tier) {
		super(metaTileEntityId, tier, false, IFluidStorageChannel.class);
	}

	@Override
	public void update() {
		super.update();
		if (getWorld().isRemote) return;

		if (isWorkingEnabled() && shouldSyncME() && updateMEStatus()) {
			if (hasItemsToSend()) pushItemsOut();

			if (hasFluidsToSend()) pushFluidsOut();
		}
	}

	@Override
	protected IItemHandlerModifiable createImportItemHandler() {
		return new NotifiableItemStackHandler(this, 1, getController(), false);
	}

	// GUI helpers

	@Override
	public void addToolUsages(ItemStack stack, World world, List<String> tooltip, boolean advanced) {
		super.addToolUsages(stack, world, tooltip, advanced);
		tooltip.add(I18n.format("gregifiedenergistics.tool_action.memory_card.copy_substitution"));
	}

	@Override
	protected ModularUI createUI(EntityPlayer player) {
		ModularUI.Builder builder = createUITemplate(player);
		return builder.build(getHolder(), player);
	}

	protected AbstractWidgetGroup createPatternsGrid() {
		return null;
	}

	protected AbstractWidgetGroup createSubstitutionGrid() {
		int slotsPerLine = GregifiedEnergisticsConfig.guiConfig.substitutionSlotsPerLine;
		return new SubstitutionListWidget(slotsPerLine, substitutionStorage);
	}

	private ModularUI.Builder createUITemplate(EntityPlayer player) {
		Size patternsSize = Size.ZERO;
		Size substitutionSize = Size.ZERO;

		AbstractWidgetGroup patternsGrid = createPatternsGrid();
		AbstractWidgetGroup substitutionGrid = createSubstitutionGrid();

		if (patternsGrid != null) {
			patternsSize = patternsGrid.getSize();
		}

		if (substitutionGrid != null) {
			substitutionSize = substitutionGrid.getSize();
		}

		int backgroundWidth = Math.max(176, Math.max(patternsSize.width, substitutionSize.width) + 14);

		int height = Math.max(18 * 4, Math.max(patternsSize.height, substitutionSize.height));
		int center = backgroundWidth / 2;

		int inventoryStartX = center - 9 - 4 * 18;
		int inventoryStartY = 40 + height + 12;

		Builder builder = ModularUI.builder(GuiTextures.BACKGROUND, backgroundWidth, 40 + height + 94);

		if (patternsGrid != null || substitutionGrid != null) {
			TabGroup<AbstractWidgetGroup> tabGroup =
					new TabGroup<AbstractWidgetGroup>(TabLocation.HORIZONTAL_TOP_LEFT, Position.ORIGIN);

			if (patternsGrid != null) {
				patternsGrid.setSelfPosition(new Position(center - patternsSize.width / 2, 40));

				tabGroup.addTab(
						new ItemTabInfo(
								"gregifiedenergistics.gui.patterns_grid",
								AEApi.instance()
										.definitions()
										.items()
										.encodedPattern()
										.maybeStack(1)
										.get()),
						patternsGrid);
			}

			if (substitutionGrid != null) {
				substitutionGrid.setSelfPosition(new Position(center - substitutionSize.width / 2, 40));

				tabGroup.addTab(
						new ItemTabInfo(
								"gregifiedenergistics.gui.substitutions_grid",
								AEApi.instance()
										.definitions()
										.items()
										.memoryCard()
										.maybeStack(1)
										.get()),
						substitutionGrid);
			}

			builder.widget(tabGroup);
		}

		builder.label(10, 5, getMetaFullName());

		builder.dynamicLabel(
				10,
				15,
				() -> isOnline
						? I18n.format("gregtech.gui.me_network.online")
						: I18n.format("gregtech.gui.me_network.offline"),
				0xFFFFFFFF);

		builder.widget(new SlotWidget(importItems, 0, 140, 14)
				.setBackgroundTexture(GuiTextures.SLOT)
				.setTooltipText(I18n.format("gregifiedenergistics.gui.item_slot")));

		builder.widget(new ImageCycleButtonWidget(
						-18,
						6,
						16,
						16,
						GregifiedEnergisticsGuiTextures.BLOCKING_MODE,
						BlockingMode.values().length,
						() -> blockingMode.ordinal(),
						index -> blockingMode = BlockingMode.values()[index])
				.setTooltipHoverString(index -> I18n.format("gregifiedenergistics.gui."
						+ BlockingMode.values()[index].toString().toLowerCase())));

		builder.widget(new ToggleButtonWidget(
						-18,
						24,
						16,
						16,
						GregifiedEnergisticsGuiTextures.FLUID_MODE,
						this::getUsingFluids,
						this::setUsingFluids)
				.setTooltipText("gregifiedenergistics.gui.fluid_mode"));

		builder.bindPlayerInventory(player.inventory, GuiTextures.SLOT, inventoryStartX, inventoryStartY);

		return builder;
	}

	@Override
	public boolean onRightClick(
			EntityPlayer player, EnumHand hand, EnumFacing facing, CuboidRayTraceResult traceResult) {
		ItemStack heldItem = player.getHeldItem(hand);

		if (AEApi.instance().definitions().items().memoryCard().isSameAs(heldItem)) {
			if (player.isSneaking()) {
				var nbt = new NBTTagCompound();
				heldItem.writeToNBT(nbt);

				nbt.setTag(STORAGE_TAG, substitutionStorage.serializeNBT());

				heldItem.setTagCompound(nbt);
				player.sendStatusMessage(
						new TextComponentString(I18n.format("gregifiedenergistics.machine.me.copy_settings")), true);
			} else {
				var tag = heldItem.getSubCompound(STORAGE_TAG);
				if (tag != null) {
					substitutionStorage.deserializeNBT(tag);

					notifySubstitutionChange();
					player.sendStatusMessage(
							new TextComponentString(I18n.format("gregifiedenergistics.machine.me.paste_settings")),
							true);
				}
			}
			return true;
		}
		return super.onRightClick(player, hand, facing, traceResult);
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound data) {
		super.writeToNBT(data);
		data.setBoolean(WORKING_ENABLED_TAG, workingEnabled);
		data.setString(BLOCKING_MODE_TAG, blockingMode.toString());
		data.setBoolean(USE_FLUID_TAG, useFluids);
		return data;
	}

	@Override
	public void readFromNBT(NBTTagCompound data) {
		super.readFromNBT(data);
		workingEnabled = data.getBoolean(WORKING_ENABLED_TAG);
		blockingMode = BlockingMode.valueOf(data.getString(BLOCKING_MODE_TAG));
		useFluids = data.getBoolean(USE_FLUID_TAG);
	}

	@Override
	public void writeInitialSyncData(PacketBuffer buf) {
		super.writeInitialSyncData(buf);
		buf.writeBoolean(workingEnabled);
		buf.writeEnumValue(blockingMode);
		buf.writeBoolean(useFluids);
	}

	@Override
	public void receiveInitialSyncData(PacketBuffer buf) {
		super.receiveInitialSyncData(buf);
		workingEnabled = buf.readBoolean();
		blockingMode = buf.readEnumValue(BlockingMode.class);
		useFluids = buf.readBoolean();
	}

	@Override
	public void receiveCustomData(int descriptor, PacketBuffer buf) {
		super.receiveCustomData(descriptor, buf);
		if (descriptor == GregtechDataCodes.WORKING_ENABLED) {
			this.workingEnabled = buf.readBoolean();
			scheduleRenderUpdate();
		}
	}

	@Override
	public MultiblockAbility<IItemHandlerModifiable> getAbility() {
		return MultiblockAbility.IMPORT_ITEMS;
	}

	@Override
	public void registerAbilities(List<IItemHandlerModifiable> list) {
		list.add(importItems);
	}

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		if (capability == GregtechTileCapabilities.CAPABILITY_CONTROLLABLE) {
			return GregtechTileCapabilities.CAPABILITY_CONTROLLABLE.cast(this);
		}
		return super.getCapability(capability, facing);
	}

	protected SimpleOverlayRenderer getOverlay() {
		if (isOnline) {
			if (isWorkingEnabled()) {
				return GregifiedEnergisticsTextures.ME_AL_HATCH_CONNECTOR_ACTIVE;
			}
			return GregifiedEnergisticsTextures.ME_AL_HATCH_CONNECTOR_WAITING;
		}
		return GregifiedEnergisticsTextures.ME_AL_HATCH_CONNECTOR_INACTIVE;
	}

	@Override
	public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
		super.renderMetaTileEntity(renderState, translation, pipeline);
		getOverlay().renderSided(getFrontFacing(), renderState, translation, pipeline);
	}

	// provide crafting patterns to the network
	@Override
	public void provideCrafting(ICraftingProviderHelper craftingHelper) {
		if (isAttachedToMultiBlock() && isWorkingEnabled()) {
			for (ICraftingPatternDetails details : getPatterns()) {
				if (details instanceof AbstractPatternHelper helper) {
					helper.providePatterns(getCraftingProvider(), craftingHelper);
				} else if (details != null) {
					craftingHelper.addCraftingOption(getCraftingProvider(), details);
				}
			}
		}
	}

	// check if hatch can be used for pushing pattern right now
	@Override
	public boolean isBusy() {
		if (!isAttachedToMultiBlock() || hasItemsToSend() || hasFluidsToSend()) {
			return true;
		}

		if (blockingMode == BlockingMode.CRAFTING_BLOCKING_MODE) {
			if (getController() != null && getController() instanceof RecipeMapMultiblockController controller) {
				MultiblockRecipeLogic workable = controller.getRecipeMapWorkable();
				if (workable.getProgressPercent() < 0.95 && workable.getProgress() > 0) {
					return true;
				}
			}
		}

		if (blockingMode.isBlockingEnabled()) {
			return getController().getAbilities(MultiblockAbility.IMPORT_ITEMS).stream()
					.map(AdaptorItemHandler::new)
					.anyMatch(InventoryAdaptor::containsItems);
		}

		return false;
	}

	// try to push pattern to import buses
	@Override
	public boolean pushPattern(ICraftingPatternDetails pattern, InventoryCrafting inventoryCrafting) {
		if (hasItemsToSend()
				|| (useFluids && hasFluidsToSend())
				|| getProxy() == null
				|| !getProxy().isActive()) {
			return false;
		}

		IAEFluidStack[] fluids = null;
		if (pattern instanceof AbstractPatternHelper patternHelper) {
			fluids = patternHelper.getFluidInputs();
		}

		if (acceptsItems(inventoryCrafting) && (!useFluids || acceptsFluids(fluids))) {
			int slot = 0;
			for (int i = 0; i < inventoryCrafting.getSizeInventory(); ++i) {
				ItemStack stack = inventoryCrafting.getStackInSlot(i);
				if (!stack.isEmpty()) {
					addToSendList(slot++, stack);
				}
			}

			if (useFluids && fluids != null) {
				slot = 0;
				for (IAEFluidStack fluidStack : fluids) {
					FluidStack stack = fluidStack.getFluidStack();
					if (stack != null && stack.amount != 0) {
						addToSendList(slot++, stack);
					}
				}
				pushFluidsOut();
			}
			pushItemsOut();
			return true;
		}

		return false;
	}

	// check if buses is able to accept items from pattern
	private boolean acceptsItems(final InventoryCrafting inventoryCrafting) {
		List<InventoryAdaptor> inventoryAdaptors = getController().getAbilities(MultiblockAbility.IMPORT_ITEMS).stream()
				.map(AdaptorItemHandler::new)
				.collect(Collectors.toList());
		Iterator<InventoryAdaptor> it = inventoryAdaptors.iterator();

		for (int i = 0; i < inventoryCrafting.getSizeInventory(); ++i) {
			ItemStack stack = inventoryCrafting.getStackInSlot(i);
			if (stack.isEmpty()) {
				continue;
			}

			if (!it.hasNext()) {
				return false;
			}

			if (!it.next().simulateAdd(stack).isEmpty()) {
				return false;
			}
		}

		return true;
	}

	private boolean containsFluids(IAEFluidStack[] fluids) {
		if (getProxy() == null) return false;

		for (IAEFluidStack fluidStack : fluids) {
			try {
				IAEFluidStack find = getProxy()
						.getStorage()
						.getInventory(getStorageChannel())
						.getStorageList()
						.findPrecise(fluidStack);
				if (find == null || find.getStackSize() < fluidStack.getStackSize()) {
					return false;
				}
			} catch (GridAccessException e) {
				return false;
			}
		}
		return true;
	}

	private boolean acceptsFluids(IAEFluidStack[] inputs) {
		if (inputs == null || inputs.length == 0) {
			return true;
		}

		if (getProxy() == null || !containsFluids(inputs)) {
			return false;
		}

		List<IFluidTank> inputHandlers = getController().getAbilities(MultiblockAbility.IMPORT_FLUIDS);

		Iterator<IFluidTank> it = inputHandlers.iterator();

		for (IAEFluidStack fluidStack : inputs) {
			if (fluidStack == null || fluidStack.getStackSize() == 0) {
				continue;
			}

			if (!it.hasNext()) {
				return false;
			}

			if (it.next().fill(fluidStack.getFluidStack(), false) < fluidStack.getStackSize()) {
				return false;
			}

			try {
				IAEFluidStack result = getProxy()
						.getStorage()
						.getInventory(getStorageChannel())
						.extractItems(fluidStack, Actionable.SIMULATE, getActionSource());
				if (!result.equals(fluidStack)) {
					return false;
				}
			} catch (GridAccessException e) {
				return false;
			}
		}
		return true;
	}

	// Push items from auto-crafting to input buses
	private void pushItemsOut() {
		List<InventoryAdaptor> inventoryAdaptors = getController().getAbilities(MultiblockAbility.IMPORT_ITEMS).stream()
				.map(AdaptorItemHandler::new)
				.collect(Collectors.toList());

		Iterator<Map.Entry<Integer, ItemStack>> it = waitingToSend.entrySet().iterator();

		while (it.hasNext()) {
			Map.Entry<Integer, ItemStack> entry = it.next();

			int index = entry.getKey();
			ItemStack stack = entry.getValue();

			if (stack.isEmpty()) {
				continue;
			}

			if (index <= inventoryAdaptors.size()) {
				ItemStack result = inventoryAdaptors.get(index).addItems(stack);
				if (result.isEmpty()) {
					it.remove();
				} else {
					stack.setCount(result.getCount());
				}
			}
		}

		if (waitingToSend.isEmpty()) {
			waitingToSend = null;
		}
	}

	private void pushFluidsOut() {
		if (getProxy() == null) return;

		List<IFluidTank> inputHandlers = getController().getAbilities(MultiblockAbility.IMPORT_FLUIDS);

		Iterator<Map.Entry<Integer, FluidStack>> it =
				fluidWaitingToSend.entrySet().iterator();

		while (it.hasNext()) {
			Map.Entry<Integer, FluidStack> entry = it.next();

			int slot = entry.getKey();
			FluidStack stack = entry.getValue();

			if (stack == null || stack.amount == 0) {
				continue;
			}

			if (slot < inputHandlers.size()) {
				try {
					IAEFluidStack extracted = getProxy()
							.getStorage()
							.getInventory(getStorageChannel())
							.extractItems(
									getStorageChannel().createStack(stack), Actionable.MODULATE, getActionSource());
					int filled = inputHandlers.get(slot).fill(extracted.getFluidStack(), true);
					if (filled == stack.amount) {
						it.remove();
					} else {
						stack.amount -= filled;
					}
				} catch (GridAccessException e) {
					// :3
				}
			}
		}

		if (fluidWaitingToSend.isEmpty()) {
			fluidWaitingToSend = null;
		}
	}

	// notify grid network when patterns should be recalculated
	public void notifyPatternChange() {
		if (Platform.isServer() && getProxy() != null) {
			try {
				getProxy()
						.getGrid()
						.postEvent(new MENetworkCraftingPatternChange(
								getCraftingProvider(), getProxy().getNode()));
			} catch (GridAccessException ignored) {
			}
		}
	}

	@Override
	public void notifySubstitutionChange() {
		for (ICraftingPatternDetails details : getPatterns()) {
			if (details instanceof AbstractPatternHelper helper) {
				helper.injectSubstitutions(substitutionStorage);
			}
		}

		notifyPatternChange();
	}

	/* ###################################
	###    GETTER/SETTER METHODS    ###
	################################### */

	public abstract Collection<? extends ICraftingPatternDetails> getPatterns();

	private void addToSendList(int slot, ItemStack stack) {
		if (waitingToSend == null) {
			waitingToSend = new Int2ObjectOpenHashMap<>();
		}
		waitingToSend.put(slot, stack);
	}

	public boolean hasItemsToSend() {
		return this.waitingToSend != null && !this.waitingToSend.isEmpty();
	}

	private void addToSendList(int slot, FluidStack stack) {
		if (fluidWaitingToSend == null) {
			fluidWaitingToSend = new Int2ObjectOpenHashMap<>();
		}
		fluidWaitingToSend.put(slot, stack);
	}

	public boolean hasFluidsToSend() {
		return this.fluidWaitingToSend != null && !this.fluidWaitingToSend.isEmpty();
	}

	public void setBlockingMode(BlockingMode blockingMode) {
		this.blockingMode = blockingMode;
	}

	public BlockingMode getBlockingMode() {
		return blockingMode;
	}

	public void setUsingFluids(boolean useFluids) {
		this.useFluids = useFluids;
	}

	public boolean getUsingFluids() {
		return useFluids;
	}

	@Override
	public void setWorkingEnabled(boolean workingEnabled) {
		this.workingEnabled = workingEnabled;
		World world = getWorld();
		if (world != null && !world.isRemote) {
			writeCustomData(GregtechDataCodes.WORKING_ENABLED, buf -> buf.writeBoolean(workingEnabled));
			notifyPatternChange();
		}
	}

	@Override
	public boolean isWorkingEnabled() {
		return workingEnabled;
	}
}
