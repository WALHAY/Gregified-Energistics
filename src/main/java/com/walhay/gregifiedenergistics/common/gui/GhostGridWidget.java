package com.walhay.gregifiedenergistics.common.gui;

import com.walhay.gregifiedenergistics.api.gui.AbstractGridWidget;
import com.walhay.gregifiedenergistics.api.patterns.ISubstitutionStorage;
import com.walhay.gregifiedenergistics.common.metatileentities.multiblockparts.MTEAbstractAssemblyLineBus;
import gregtech.api.gui.Widget;
import gregtech.api.recipes.ingredients.GTRecipeInput;

public class GhostGridWidget extends AbstractGridWidget<GTRecipeInput> {

	private final ISubstitutionStorage storage;
	private final MTEAbstractAssemblyLineBus mte;

	public GhostGridWidget(int x, int y, ISubstitutionStorage storage, MTEAbstractAssemblyLineBus mte) {
		super(x, y, 3, 3);
		this.storage = storage;
		this.mte = mte;
	}

	@Override
	public Widget createWidget(GTRecipeInput item, int x, int y) {
		String name = item.toString();
		return new GhostSlot(x * 18, y * 18, storage.getOption(name), item, opt -> {
			storage.setOption(name, opt);
			mte.notifyPatternChange();
		});
	}
}
