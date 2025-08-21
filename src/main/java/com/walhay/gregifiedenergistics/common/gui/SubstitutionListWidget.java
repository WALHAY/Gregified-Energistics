package com.walhay.gregifiedenergistics.common.gui;

import com.walhay.gregifiedenergistics.api.patterns.ISubstitutionStorage;
import com.walhay.gregifiedenergistics.common.metatileentities.multiblockparts.MTEAbstractAssemblyLineBus;
import gregtech.api.gui.widgets.ScrollableListWidget;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.stack.MaterialStack;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.Collection;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;

public class SubstitutionListWidget extends ScrollableListWidget {

	private final MTEAbstractAssemblyLineBus mte;
	private final ISubstitutionStorage<String> storage;
	private int previousSize = 0;

	public SubstitutionListWidget(int x, int y, ISubstitutionStorage<String> storage, MTEAbstractAssemblyLineBus mte) {
		super(x, y, 18 * 9, 18 * 4);
		this.storage = storage;
		this.mte = mte;
	}

	protected void recalculateList() {
		Object2ObjectOpenHashMap<Material, Collection<String>> tierMap = new Object2ObjectOpenHashMap<>();
		for (String ore : storage.getOptions()) {
			ItemStack itemstack = OreDictUnifier.get(ore);

			MaterialStack materialStack = OreDictUnifier.getMaterial(itemstack);

			if (materialStack == null || materialStack.material == null) continue;

			Material material = materialStack.material;

			tierMap.putIfAbsent(material, new ArrayList<>());
			tierMap.get(material).add(ore);
		}

		clearAllWidgets();
		for (var entry : tierMap.entrySet()) {
			addGridWidget(entry.getKey().getName(), entry.getValue());
		}
	}

	protected void addGridWidget(String label, Collection<String> subInputs) {
		SubstitutionGridWidget grid = new SubstitutionGridWidget(0, 0, label, subInputs, storage, mte);
		addWidget(grid);
	}

	@Override
	public void detectAndSendChanges() {
		super.detectAndSendChanges();
		if (storage == null || storage.getOptions() == null) return;

		int currentSize = storage.getOptions().size();
		if (previousSize != currentSize) {
			previousSize = currentSize;
			writeUpdateInfo(10, buf -> buf.writeInt(currentSize));
			recalculateList();
		}
	}

	@Override
	public void readUpdateInfo(int descriptor, PacketBuffer buf) {
		super.readUpdateInfo(descriptor, buf);
		if (descriptor == 10) {
			this.previousSize = buf.readInt();
			recalculateList();
		}
	}
}
