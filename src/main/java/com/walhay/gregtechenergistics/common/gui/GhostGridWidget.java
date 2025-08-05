package com.walhay.gregtechenergistics.common.gui;

import com.walhay.gregtechenergistics.api.capability.ISubstitutionStorage;
import com.walhay.gregtechenergistics.api.gui.AbstractGridWidget;
import com.walhay.gregtechenergistics.common.metatileentities.multiblockparts.MetaTileEntityAbstractAssemblyLineHatch;
import gregtech.api.gui.Widget;
import gregtech.api.recipes.ingredients.GTRecipeInput;

public class GhostGridWidget extends AbstractGridWidget<GTRecipeInput> {

	private final ISubstitutionStorage storage;
	private final MetaTileEntityAbstractAssemblyLineHatch mte;

	public GhostGridWidget(int x, int y, ISubstitutionStorage storage, MetaTileEntityAbstractAssemblyLineHatch mte) {
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
