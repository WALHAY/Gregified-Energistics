package com.walhay.gregtechenergistics.common.gui;

import java.util.function.Consumer;

import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.Widget;
import gregtech.api.recipes.ingredients.GTRecipeInput;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;

public class GhostSlot extends Widget {

	private final GTRecipeInput input;
	private int selected = 0;
	private final Consumer<Integer> changer;

	public GhostSlot(int x, int y, int selected, GTRecipeInput input, Consumer<Integer> changer) {
		super(new Position(x, y), new Size(18, 18));
		this.input = input;
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
		ItemStack stack = input.getInputStacks()[selected];
		drawItemStack(stack, pos.x + 1, pos.y + 1, "");
	}

	@Override
	public boolean mouseClicked(int mouseX, int mouseY, int button) {
		if (isMouseOverElement(mouseX, mouseY)) {
			int options = input.getInputStacks().length;
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
