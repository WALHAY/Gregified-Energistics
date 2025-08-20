package com.walhay.gregifiedenergistics.common.gui;

import com.walhay.gregifiedenergistics.api.gui.AbstractGridWidget;
import com.walhay.gregifiedenergistics.api.patterns.ISubstitutionStorage;
import com.walhay.gregifiedenergistics.common.metatileentities.multiblockparts.MTEAbstractAssemblyLineBus;
import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.LabelWidget;
import gregtech.api.unification.OreDictUnifier;
import java.util.Collection;
import java.util.List;
import net.minecraft.item.ItemStack;

public class SubstitutionGridWidget extends AbstractGridWidget<String> {

	private final ISubstitutionStorage<String> storage;
	private final MTEAbstractAssemblyLineBus mte;
	private final String name;

	public SubstitutionGridWidget(
			int x, int y, String label, ISubstitutionStorage<String> storage, MTEAbstractAssemblyLineBus mte) {
		super(x, y, 4, 9);
		this.storage = storage;
		this.mte = mte;
		this.name = label;

		generateGrid(storage.getOptions());
	}

	@Override
	protected void generateGrid(int x, int y, Collection<String> items) {
		addWidget(new LabelWidget(x, y, name));
		super.generateGrid(x + 10, y + 10, items);
	}

	@Override
	public Widget createWidget(String name, int x, int y) {
		List<ItemStack> substitutions = OreDictUnifier.getAllWithOreDictionaryName(name);

		if (substitutions == null || substitutions.isEmpty()) return null;

		return new SubstitutionSlotWidget(x * 18, y * 18, storage.getOption(name), substitutions, opt -> {
			storage.setOption(name, opt);
			mte.notifyPatternChange();
		});
	}
}
