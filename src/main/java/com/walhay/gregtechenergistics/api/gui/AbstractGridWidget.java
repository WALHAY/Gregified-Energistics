package com.walhay.gregtechenergistics.api.gui;

import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.WidgetGroup;
import gregtech.api.util.Size;
import java.util.Collection;

public abstract class AbstractGridWidget<K> extends WidgetGroup {

	protected final int rows;
	protected final int columns;

	public AbstractGridWidget(int x, int y, int rows, int columns) {
		super(x, y, 0, 0);
		this.rows = rows;
		this.columns = columns;
	}

	public void initGrid(Collection<K> items) {
		int i = 0;
		int width = 0;
		int height = 0;
		int current_width = 0;
		for (K item : items) {
			Widget widget = createWidget(item, i % columns, i / columns);
			if (i % columns == 0) height += widget.getSize().height;

			current_width += widget.getSize().width;

			addWidget(i++, widget);
		}

		if (current_width > width) width = current_width;

		setSize(new Size(width, height));
	}

	public abstract Widget createWidget(K item, int x, int y);
}
