package com.walhay.gregifiedenergistics.api.patterns.substitutions;

import com.walhay.gregifiedenergistics.api.patterns.ISubstitutionStorage;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants.NBT;

public class SubstitutionStorage implements ISubstitutionStorage<String> {

	public static final String STORAGE_TAG = "SubstitutionStorage";

	private Map<String, Integer> subMap = new HashMap<>();

	@Override
	public int getOption(String name) {
		if (!subMap.containsKey(name)) {
			subMap.put(name, 0);
			return 0;
		}

		return subMap.get(name);
	}

	@Override
	public void setOption(String name, int option) {
		subMap.put(name, option);
	}

	@Override
	public NBTTagCompound serializeNBT() {
		NBTTagCompound compound = new NBTTagCompound();
		NBTTagList list = new NBTTagList();
		for (Map.Entry<String, Integer> entry : subMap.entrySet()) {
			NBTTagCompound tag = new NBTTagCompound();

			tag.setString("ingredient", entry.getKey());
			tag.setInteger("option", entry.getValue());

			list.appendTag(tag);
		}

		compound.setTag("Data", list);

		return compound;
	}

	@Override
	public void deserializeNBT(NBTTagCompound compound) {
		NBTTagList nbt = compound.getTagList("Data", NBT.TAG_COMPOUND);
		for (NBTBase base : nbt) {
			if (base instanceof NBTTagCompound tag) {
				String key = tag.getString("ingredient");
				int option = tag.getInteger("option");

				subMap.put(key, option);
			}
		}
	}

	@Override
	public Collection<String> getOptions() {
		return subMap.keySet();
	}
}
