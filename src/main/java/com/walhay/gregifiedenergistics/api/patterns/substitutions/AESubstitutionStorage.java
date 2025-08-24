package com.walhay.gregifiedenergistics.api.patterns.substitutions;

import com.walhay.gregifiedenergistics.api.patterns.ISubstitutionStorage;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.Collection;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants.NBT;

public class AESubstitutionStorage implements ISubstitutionStorage {

	private Object2IntOpenHashMap<String> substitutionMap = new Object2IntOpenHashMap<>();

	public AESubstitutionStorage(NBTTagCompound tag) {
		deserializeNBT(tag);
	}

	@Override
	public int getOption(String name) {
		return substitutionMap.getOrDefault(name, 0);
	}

	@Override
	public void setOption(String name, int option) {
		substitutionMap.put(name, option);
	}

	@Override
	public Collection<String> getOptions() {
		return substitutionMap.keySet();
	}

	@Override
	public NBTTagCompound serializeNBT() {
		return new NBTTagCompound();
	}

	@Override
	public void deserializeNBT(NBTTagCompound compound) {
		NBTTagList nbt = compound.getTagList("Data", NBT.TAG_COMPOUND);
		for (NBTBase base : nbt) {
			if (base instanceof NBTTagCompound tag) {
				String key = tag.getString("Ingredient");
				int option = tag.getInteger("Option");

				substitutionMap.put(key, option);
			}
		}
	}
}
