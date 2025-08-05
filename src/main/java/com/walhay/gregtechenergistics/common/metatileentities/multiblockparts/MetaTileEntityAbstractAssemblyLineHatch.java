package com.walhay.gregtechenergistics.common.metatileentities.multiblockparts;

import static com.walhay.gregtechenergistics.api.capability.GTEDataCodes.ONLINE_STATUS_UPDATE;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.crafting.ICraftingProviderHelper;
import appeng.api.networking.events.MENetworkCraftingPatternChange;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.channels.IFluidStorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.util.AECableType;
import appeng.api.util.AEPartLocation;
import appeng.api.util.DimensionalCoord;
import appeng.me.GridAccessException;
import appeng.me.helpers.AENetworkProxy;
import appeng.me.helpers.IGridProxyable;
import appeng.me.helpers.MachineSource;
import appeng.util.InventoryAdaptor;
import appeng.util.Platform;
import appeng.util.inv.AdaptorItemHandler;
import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.walhay.gregtechenergistics.api.capability.AbstractPatternHelper;
import com.walhay.gregtechenergistics.api.capability.impl.SubstitutionStorage;
import com.walhay.gregtechenergistics.api.gui.GTEGuiTextures;
import com.walhay.gregtechenergistics.api.render.GTETextures;
import com.walhay.gregtechenergistics.api.util.BlockingMode;
import gregtech.api.capability.impl.MultiblockRecipeLogic;
import gregtech.api.capability.impl.NotifiableItemStackHandler;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.ImageCycleButtonWidget;
import gregtech.api.gui.widgets.SlotWidget;
import gregtech.api.gui.widgets.ToggleButtonWidget;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.client.renderer.texture.cube.SimpleOverlayRenderer;
import gregtech.common.ConfigHolder;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityMultiblockNotifiablePart;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.items.IItemHandlerModifiable;

/*
 * This class can't use MetaTileEntityHolder's as IGridProxyable
 * Because of MTEH not implementing ICraftingProvider logic
 */
public abstract class MetaTileEntityAbstractAssemblyLineHatch extends MetaTileEntityMultiblockNotifiablePart
		implements IMultiblockAbilityPart<IItemHandlerModifiable>, IGridProxyable, IActionHost, ICraftingProvider {

	private static final int ME_UPDATE_INTERVAL = ConfigHolder.compat.ae2.updateIntervals;
	protected int updateTick = 0;
	private boolean isOnline = false;
	private final AENetworkProxy proxy = new AENetworkProxy(this, "proxy", getStackForm(), true);
	private final IActionSource source = new MachineSource(this);
	private Int2ObjectOpenHashMap<ItemStack> waitingToSend;
	private Int2ObjectOpenHashMap<FluidStack> fluidWaitingToSend;
	private int priority = 0;
	private BlockingMode blockingMode = BlockingMode.NO_BLOCKING;
	private boolean useFluids = true;
	protected final SubstitutionStorage substitutionStorage = new SubstitutionStorage();

	/* ###########################
	###     MTE METHODS     ###
	########################### */

	public MetaTileEntityAbstractAssemblyLineHatch(ResourceLocation metaTileEntityId, int tier) {
		super(metaTileEntityId, tier, false);
	}

	@Override
	public void update() {
		if (getWorld().isRemote) return;

		updateTick++;
		if (!shouldSyncME()) return;

		if (!updateMEStatus()) return;

		if (hasItemsToSend()) pushItemsOut();

		if (hasFluidsToSend()) pushFluidsOut();
	}

	@Override
	protected IItemHandlerModifiable createImportItemHandler() {
		return new NotifiableItemStackHandler(1, getController(), false);
	}

	@Override
	public void setFrontFacing(EnumFacing newFrontFacing) {
		super.setFrontFacing(newFrontFacing);
		getProxy().setValidSides(EnumSet.of(newFrontFacing));
	}

	@Override
	public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
		tooltip.add(I18n.format("gregtechenergistics.machine.me_assembly_line_hatch.info0"));
		tooltip.add(I18n.format("gregtechenergistics.machine.me_assembly_line_hatch.info1"));
	}

	// GUI helpers

	protected abstract Widget createPatternListWidget(int x, int y);

	protected abstract String usedPatternsInfo();

	@Override
	protected ModularUI createUI(EntityPlayer entityPlayer) {
		return ModularUI.builder(GuiTextures.BACKGROUND, 176, 18 + 18 * 4 + 94)
				.label(10, 5, getMetaFullName())
				.dynamicLabel(
						10,
						15,
						() -> isOnline
								? I18n.format("gregtechenergistics.gui.me_network.online")
								: I18n.format("gregtechenergistics.gui.me_network.offline"),
						0xFFFFFFFF)
				.dynamicLabel(
						10, 25, () -> I18n.format("gregtechenergistics.gui.pattern_list", usedPatternsInfo()), 0x404040)
				.widget(createPatternListWidget(10, 35))
				.widget(new SlotWidget(importItems, 0, 140, 14)
						.setBackgroundTexture(GuiTextures.SLOT)
						.setTooltipText(I18n.format("gregtechenergistics.gui.item_slot")))
				.widget(new ImageCycleButtonWidget(
								-18,
								6,
								16,
								16,
								GTEGuiTextures.BLOCKING_MODE,
								BlockingMode.values().length,
								() -> blockingMode.ordinal(),
								index -> blockingMode = BlockingMode.values()[index])
						.setTooltipHoverString(
								index -> I18n.format("gregtechenergistics.machine.me_assembly_line_hatch.gui."
										+ BlockingMode.values()[index]
												.toString()
												.toLowerCase())))
				.widget(new ToggleButtonWidget(
								-18, 24, 16, 16, GTEGuiTextures.FLUID_MODE, this::getUsingFluids, this::setUsingFluids)
						.setTooltipText("gregtechenergistics.gui.fluid_mode"))
				.bindPlayerInventory(entityPlayer.inventory, GuiTextures.SLOT, 7, 18 + 18 * 4 + 12)
				.build(getHolder(), entityPlayer);
	}

	@Override
	public boolean onRightClick(
			EntityPlayer player, EnumHand hand, EnumFacing facing, CuboidRayTraceResult traceResult) {
		ItemStack heldItem = player.getHeldItem(hand);

		if (AEApi.instance().definitions().items().memoryCard().isSameAs(heldItem)) {
			if (player.isSneaking()) {
				var nbt = new NBTTagCompound();
				heldItem.writeToNBT(nbt);

				nbt.setTag("substitutionData", substitutionStorage.serializeNBT());

				heldItem.setTagCompound(nbt);
				player.sendStatusMessage(new TextComponentString("Settings Saved"), true);
			} else {
				var tag = heldItem.getSubCompound("substitutionData");
				if (tag != null) {
					substitutionStorage.deserializeNBT(tag);

					notifyPatternChange();
					player.sendStatusMessage(new TextComponentString("Settings Copied"), true);
				}
			}
			return true;
		}
		return super.onRightClick(player, hand, facing, traceResult);
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound data) {
		super.writeToNBT(data);
		data.setString("blockingMode", blockingMode.toString());
		data.setInteger("priority", priority);
		data.setBoolean("useFluids", useFluids);
		data.setTag("substitutionStorage", substitutionStorage.serializeNBT());
		return data;
	}

	@Override
	public void readFromNBT(NBTTagCompound data) {
		super.readFromNBT(data);
		blockingMode = BlockingMode.valueOf(data.getString("blockingMode"));
		priority = data.getInteger("priority");
		useFluids = data.getBoolean("useFluids");
		substitutionStorage.deserializeNBT(data.getCompoundTag("substitutionStorage"));
	}

	@Override
	public void writeInitialSyncData(PacketBuffer buf) {
		super.writeInitialSyncData(buf);
		buf.writeEnumValue(blockingMode);
		buf.writeInt(priority);
		buf.writeBoolean(useFluids);
		buf.writeInt(updateTick);
		buf.writeBoolean(isOnline);
		NBTTagCompound proxyNBT = new NBTTagCompound();
		proxy.writeToNBT(proxyNBT);
		buf.writeCompoundTag(proxyNBT);
		buf.writeCompoundTag(substitutionStorage.serializeNBT());
	}

	@Override
	public void receiveInitialSyncData(PacketBuffer buf) {
		super.receiveInitialSyncData(buf);
		blockingMode = buf.readEnumValue(BlockingMode.class);
		priority = buf.readInt();
		useFluids = buf.readBoolean();
		updateTick = buf.readInt();
		isOnline = buf.readBoolean();
		try {
			proxy.readFromNBT(buf.readCompoundTag());
			substitutionStorage.deserializeNBT(buf.readCompoundTag());
		} catch (IOException ignored) {
			// :#
		}
	}

	@Override
	public void receiveCustomData(int dataId, PacketBuffer buf) {
		super.receiveCustomData(dataId, buf);
		if (dataId == ONLINE_STATUS_UPDATE) {
			this.isOnline = buf.readBoolean();
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

	protected SimpleOverlayRenderer getOverlay() {
		if (isOnline) {
			return GTETextures.ME_AL_HATCH_CONNECTOR_ACTIVE;
		}
		return GTETextures.ME_AL_HATCH_CONNECTOR_INACTIVE;
	}

	@Override
	public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
		super.renderMetaTileEntity(renderState, translation, pipeline);
		getOverlay().renderSided(getFrontFacing(), renderState, translation, pipeline);
	}

	/* ##############################
	###    AE2 GRID METHODS    ###
	############################## */

	@Nonnull
	@Override
	public AENetworkProxy getProxy() {
		if (!proxy.isReady() && getWorld() != null) {
			proxy.onReady();
			proxy.setFlags(GridFlags.REQUIRE_CHANNEL);
			proxy.setValidSides(EnumSet.of(frontFacing));
			proxy.setIdlePowerUsage(ConfigHolder.compat.ae2.meHatchEnergyUsage);
		}
		return proxy;
	}

	protected boolean updateMEStatus() {
		isOnline = getProxy().isActive() && getProxy().isPowered();
		writeCustomData(ONLINE_STATUS_UPDATE, buf -> buf.writeBoolean(isOnline));
		return isOnline;
	}

	protected boolean shouldSyncME() {
		return updateTick % ME_UPDATE_INTERVAL == 0;
	}

	// provide crafting patterns to the network
	@Override
	public void provideCrafting(ICraftingProviderHelper craftingHelper) {
		if (isAttachedToMultiBlock()) {
			for (ICraftingPatternDetails details : getPatterns()) {
				if (details instanceof AbstractPatternHelper helper) {
					helper.injectSubstitutions(substitutionStorage);
					helper.providePattern(this, craftingHelper);
					continue;
				}

				if (details != null) {
					details.setPriority(priority);

					craftingHelper.addCraftingOption(this, details);
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
				|| !getProxy().isActive()
				|| !getPatterns().contains(pattern)) {
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
		IFluidStorageChannel fluidChannel = AEApi.instance().storage().getStorageChannel(IFluidStorageChannel.class);
		for (IAEFluidStack fluidStack : fluids) {
			try {
				IAEFluidStack find = getProxy()
						.getStorage()
						.getInventory(fluidChannel)
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

		if (!containsFluids(inputs)) {
			return false;
		}

		IFluidStorageChannel storageChannel = AEApi.instance().storage().getStorageChannel(IFluidStorageChannel.class);
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
						.getInventory(storageChannel)
						.extractItems(fluidStack, Actionable.SIMULATE, source);
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
		IFluidStorageChannel fluidChannel = AEApi.instance().storage().getStorageChannel(IFluidStorageChannel.class);
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
							.getInventory(fluidChannel)
							.extractItems(fluidChannel.createStack(stack), Actionable.MODULATE, source);
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

	@Nonnull
	@Override
	public AECableType getCableConnectionType(@Nonnull AEPartLocation part) {
		if (part.getFacing() != getFrontFacing()) {
			return AECableType.NONE;
		}
		return AECableType.SMART;
	}

	@Nullable @Override
	public IGridNode getGridNode(@Nonnull AEPartLocation partLocation) {
		return getProxy().getNode();
	}

	@Nonnull
	@Override
	public IGridNode getActionableNode() {
		return getProxy().getNode();
	}

	@Override
	public DimensionalCoord getLocation() {
		return new DimensionalCoord(getWorld(), getPos());
	}

	@Override
	public void securityBreak() {
		getWorld().destroyBlock(getPos(), shouldDropWhenDestroyed());
	}

	// notify grid network when patterns should be recalculated
	public void notifyPatternChange() {
		if (Platform.isServer()) {
			try {
				getProxy()
						.getGrid()
						.postEvent(new MENetworkCraftingPatternChange(
								this, getProxy().getNode()));
			} catch (GridAccessException ignored) {
			}
		}
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
}
