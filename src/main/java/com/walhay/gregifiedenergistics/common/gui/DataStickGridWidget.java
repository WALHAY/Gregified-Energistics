package com.walhay.gregifiedenergistics.common.gui;

import com.walhay.gregifiedenergistics.api.capability.AbstractPatternItemHandler;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.widgets.SlotWidget;
import gregtech.api.gui.widgets.WidgetGroup;

public class DataStickGridWidget extends WidgetGroup {

	private AbstractPatternItemHandler handler;
	private final int slotsPerLine;

	public DataStickGridWidget(int x, int y, int slotsPerLine, AbstractPatternItemHandler handler) {
		super(x, y, 18 * slotsPerLine, handler.getSlots() / slotsPerLine * 18);
		this.slotsPerLine = slotsPerLine;
		this.handler = handler;
	}

	public DataStickGridWidget(int slotsPerLine, AbstractPatternItemHandler handler) {
		this(0, 0, slotsPerLine, handler);
	}

	public DataStickGridWidget(AbstractPatternItemHandler handler) {
		this(9, handler);
	}

	@Override
	public void initWidget() {
		super.initWidget();

		for (int i = 0; i < handler.getSlots(); ++i) {
			int row = i / slotsPerLine;
			int col = i % slotsPerLine;

			SlotWidget slot = new SlotWidget(handler, i, row * 18, col * 18).setBackgroundTexture(GuiTextures.SLOT);
			addWidget(i, slot);
		}
	}
}
