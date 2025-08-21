package com.walhay.gregifiedenergistics.common.gui;

import com.walhay.gregifiedenergistics.api.patterns.ISubstitutionStorage;
import com.walhay.gregifiedenergistics.common.metatileentities.multiblockparts.MTEAbstractAssemblyLineBus;
import gregtech.api.gui.widgets.LabelWidget;
import gregtech.api.gui.widgets.WidgetGroup;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.util.Size;
import java.util.Collection;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;

public class SubstitutionGridWidget extends WidgetGroup {

	private final ISubstitutionStorage<String> storage;
	private final MTEAbstractAssemblyLineBus mte;
	private final String label;
	private Collection<String> options;
	private final int slotsPerLine;

	public SubstitutionGridWidget(
			int x,
			int y,
			int slotsPerLine,
			String label,
			Collection<String> options,
			ISubstitutionStorage<String> storage,
			MTEAbstractAssemblyLineBus mte) {
		this.storage = storage;
		this.mte = mte;
		this.label = label;
		this.options = options;
		this.slotsPerLine = slotsPerLine;
	}

	public SubstitutionGridWidget(
			int slotsPerLine,
			String label,
			Collection<String> options,
			ISubstitutionStorage<String> storage,
			MTEAbstractAssemblyLineBus mte) {
		this(0, 0, slotsPerLine, label, options, storage, mte);
	}

	public SubstitutionGridWidget(
			String label,
			Collection<String> options,
			ISubstitutionStorage<String> storage,
			MTEAbstractAssemblyLineBus mte) {
		this(0, 0, 9, label, options, storage, mte);
	}

	@Override
	public void initWidget() {
		super.initWidget();

		int i = 0;
		int textWidth = Minecraft.getMinecraft().fontRenderer.getStringWidth(label);
		addWidget(new LabelWidget(18 / 2 * slotsPerLine - textWidth / 2, 0, label));

		for (String option : options) {
			List<ItemStack> items = OreDictUnifier.getAllWithOreDictionaryName(option);

			int x = (i % slotsPerLine) * 18;
			int y = (i++ / slotsPerLine) * 18 + 10;

			// FIXME: change so that storage will accept notifiable and will notify mte on change instead of that cringe
			SubstitutionSlotWidget slot = new SubstitutionSlotWidget(x, y, storage.getOption(option), items, o -> {
				storage.setOption(option, o);
				mte.notifyPatternChange();
			});

			addWidget(slot);
		}

		setSize(new Size(slotsPerLine * 18, 10 + 18 * (int) Math.ceil((double) i / slotsPerLine)));
	}
}
