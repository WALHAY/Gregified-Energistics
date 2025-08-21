package com.walhay.gregifiedenergistics.api.gui;

import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.WidgetGroup;
import gregtech.api.util.Position;
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

	protected void generateGrid(Collection<K> items) {
		generateGrid(0, 0, items);
	}

	protected void generateGrid(int x, int y, Collection<K> items) {
		int i = 0;
		int width = 0;
		int height = 0;
		int currentWidth = 0;
		for (K item : items) {
			Widget widget = createWidget(item, i % columns, i / columns);

			if (widget == null) continue;
			Position position = widget.getPosition();
			widget.setSelfPosition(new Position(x + position.x, y + position.y));
			addWidget(i++, widget);

			Size size = widget.getSize();
			currentWidth += size.width;

			if (i % columns == 0) {
				width = Math.max(currentWidth, width);

				currentWidth = 0;
				height += size.height;
			}
		}

		int size = items.size();
		setSize(new Size(
				Math.min(size, columns) * 18 + x, (int) Math.min(rows, Math.ceil((double) size / columns)) * 18 + y));
	}

	public abstract Widget createWidget(K item, int row, int column);
}
