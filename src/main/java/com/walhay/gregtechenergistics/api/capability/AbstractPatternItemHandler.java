package com.walhay.gregtechenergistics.api.capability;

import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.items.misc.ItemEncodedPattern;
import java.util.Arrays;
import java.util.Collection;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

public abstract class AbstractPatternItemHandler extends ItemStackHandler {

	private final ICraftingPatternDetails[] patterns;

	public AbstractPatternItemHandler(int size) {
		super(size);
		this.patterns = new ICraftingPatternDetails[size];
	}

	@Override
	protected void onContentsChanged(int slot) {
		ItemStack stack = getStackInSlot(slot);
		if (stack.isEmpty()) {
			patterns[slot] = null;
			return;
		}

		ICraftingPatternDetails pattern = getPatternDetails(slot);
		if (pattern == null || !ItemStack.areItemStacksEqual(pattern.getPattern(), stack)) {
			patterns[slot] = getPatternFromItemStack(stack);
		}
	}

	@Nullable public ICraftingPatternDetails getPatternDetails(int slot) {
		validateSlotIndex(slot);
		return patterns[slot];
	}

	public Collection<ICraftingPatternDetails> getPatterns() {
		return Arrays.asList(patterns);
	}

	@Override
	public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
		return stack.getItem() instanceof ItemEncodedPattern;
	}

	@Override
	protected int getStackLimit(int slot, @Nonnull ItemStack stack) {
		return 1;
	}

	protected abstract ICraftingPatternDetails getPatternFromItemStack(ItemStack stack);

	@Override
	protected void onLoad() {
		super.onLoad();
		for (int slot = 0; slot < getSlots(); ++slot) {
			ICraftingPatternDetails pattern = getPatternFromItemStack(getStackInSlot(slot));
			if (pattern != null) {
				patterns[slot] = pattern;
			}
		}
	}
}
