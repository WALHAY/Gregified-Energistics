package com.walhay.gregifiedenergistics.common.gui;

import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.Widget;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;

public class SubstitutionSlotWidget extends Widget {

	private final List<ItemStack> inputs;
	private int selected = 0;
	private final Consumer<Integer> changer;

	public SubstitutionSlotWidget(int x, int y, int selected, List<ItemStack> inputs, Consumer<Integer> changer) {
		super(new Position(x, y), new Size(18, 18));
		this.inputs = inputs;
		this.selected = selected;
		this.changer = changer;
	}

	@Override
	public void drawInBackground(int mouseX, int mouseY, float partialTicks, IRenderContext context) {
		Position pos = getPosition();
		GuiTextures.SLOT.draw(pos.x, pos.y, 18, 18);
	}

	@Override
	public void drawInForeground(int mouseX, int mouseY) {
		super.drawInForeground(mouseX, mouseY);
		Position pos = getPosition();
		ItemStack stack = inputs.get(selected);
		drawItemStack(stack, pos.x + 1, pos.y + 1, "");
	}

	@Override
	public boolean mouseClicked(int mouseX, int mouseY, int button) {
		if (isMouseOverElement(mouseX, mouseY)) {
			int options = inputs.size();
			if (button == 0) {
				selected = (selected + 1) % options;
			} else {
				selected = (selected - 1 < 0) ? options - 1 : selected - 1;
			}
			writeClientAction(6, buffer -> buffer.writeVarInt(selected));
			changer.accept(selected);
			return true;
		}
		return false;
	}

	@Override
	public void handleClientAction(int id, PacketBuffer buffer) {
		super.handleClientAction(id, buffer);
		if (id == 6) {
			selected = buffer.readVarInt();
			changer.accept(selected);
		}
	}
}
