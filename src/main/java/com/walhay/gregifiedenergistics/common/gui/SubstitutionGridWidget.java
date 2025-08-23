package com.walhay.gregifiedenergistics.common.gui;

import com.walhay.gregifiedenergistics.api.patterns.ISubstitutionStorage;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.widgets.AbstractWidgetGroup;
import gregtech.api.gui.widgets.DrawableWidget;
import gregtech.api.gui.widgets.LabelWidget;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.util.Position;
import java.util.Collection;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;

public class SubstitutionGridWidget extends AbstractWidgetGroup {

	private final ISubstitutionStorage storage;
	private final String label;
	private Collection<String> options;
	private final int slotsPerLine;

	public SubstitutionGridWidget(
			int x, int y, int slotsPerLine, String label, Collection<String> options, ISubstitutionStorage storage) {
		super(new Position(x, y));
		this.storage = storage;
		this.label = label;
		this.options = options;
		this.slotsPerLine = slotsPerLine;
	}

	public SubstitutionGridWidget(
			int slotsPerLine, String label, Collection<String> options, ISubstitutionStorage storage) {
		this(0, 0, slotsPerLine, label, options, storage);
	}

	public SubstitutionGridWidget(String label, Collection<String> options, ISubstitutionStorage storage) {
		this(0, 0, 9, label, options, storage);
	}

	@Override
	public void initWidget() {
		super.initWidget();

		int i = 0;
		int textWidth = Minecraft.getMinecraft().fontRenderer.getStringWidth(label);
		addWidget(new LabelWidget(18 / 2 * slotsPerLine - textWidth / 2, 2, label));

		for (String oreName : options) {
			List<ItemStack> items = OreDictUnifier.getAllWithOreDictionaryName(oreName);

			int x = (i % slotsPerLine) * 18;
			int y = (i++ / slotsPerLine) * 18 + 10;

			SubstitutionSlotWidget slot = new SubstitutionSlotWidget(
					x, y, storage.getOption(oreName), items, option -> storage.setOption(oreName, option));

			addWidget(slot);
		}

		int y = (i / slotsPerLine) * 18 + 10;
		for (int j = i % slotsPerLine; j < slotsPerLine; ++j) {
			int x = j * 18;
			addWidget(new DrawableWidget(x, y, 18, 18).setBackgroundDrawer((a, b, c, d, e) -> {
				Position position = e.getPosition();
				GuiTextures.SLOT_DARK.draw(position.x, position.y, 18, 18);
			}));
		}
	}
}
