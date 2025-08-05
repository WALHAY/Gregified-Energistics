package com.walhay.gregtechenergistics.api.capability;

import appeng.api.AEApi;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.storage.channels.IFluidStorageChannel;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.ingredients.GTRecipeInput;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;

public abstract class AbstractPatternHelper implements ICraftingPatternDetails, ISubstitutionHandler {

	protected static final IItemStorageChannel itemChannel =
			AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class);
	protected static final IFluidStorageChannel fluidChannel =
			AEApi.instance().storage().getStorageChannel(IFluidStorageChannel.class);
	protected IAEItemStack[] inputs;
	protected IAEItemStack[] outputs;
	protected IAEFluidStack[] fluidInputs;
	private int priority = 0;
	private final Map<Integer, GTRecipeInput> subMap = new HashMap<>();

	protected void parseRecipe(Recipe recipe) {
		inputs = recipe.getInputs().stream()
				.map(input -> input.getInputStacks()[0])
				.map(ItemStack::copy)
				.map(itemChannel::createStack)
				.toArray(IAEItemStack[]::new);

		outputs = recipe.getOutputs().stream()
				.map(ItemStack::copy)
				.map(itemChannel::createStack)
				.toArray(IAEItemStack[]::new);

		fluidInputs = recipe.getFluidInputs().stream()
				.map(GTRecipeInput::getInputFluidStack)
				.map(FluidStack::copy)
				.map(fluidChannel::createStack)
				.toArray(IAEFluidStack[]::new);

		for (int i = 0; i < recipe.getInputs().size(); ++i) {
			GTRecipeInput input = recipe.getInputs().get(i);
			if (input != null && input.getInputStacks().length > 1) {
				subMap.put(i, input);
			}
		}
	}

	@Override
	public Collection<GTRecipeInput> getSubstitutions() {
		return subMap.values();
	}

	@Override
	public void injectSubstitutions(ISubstitutionStorage storage) {
		for (Map.Entry<Integer, GTRecipeInput> entry : subMap.entrySet()) {
			GTRecipeInput input = entry.getValue();
			ItemStack stack = input.getInputStacks()[storage.getOption(input.toString())];

			inputs[entry.getKey()] = itemChannel.createStack(stack);
		}
	}

	@Override
	public boolean isValidItemForSlot(int i, ItemStack itemStack, World world) {
		return false;
	}

	@Override
	public boolean isCraftable() {
		return false;
	}

	public IAEFluidStack[] getFluidInputs() {
		return fluidInputs;
	}

	@Override
	public IAEItemStack[] getInputs() {
		return inputs;
	}

	@Override
	public IAEItemStack[] getCondensedInputs() {
		return inputs;
	}

	@Override
	public IAEItemStack[] getCondensedOutputs() {
		return outputs;
	}

	@Override
	public IAEItemStack[] getOutputs() {
		return outputs;
	}

	@Override
	public boolean canSubstitute() {
		return false;
	}

	@Override
	public ItemStack getOutput(InventoryCrafting inventoryCrafting, World world) {
		return null;
	}

	@Override
	public int getPriority() {
		return priority;
	}

	@Override
	public void setPriority(int i) {
		priority = i;
	}
}
