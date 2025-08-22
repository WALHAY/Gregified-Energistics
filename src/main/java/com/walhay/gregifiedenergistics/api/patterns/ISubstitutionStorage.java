package com.walhay.gregifiedenergistics.api.patterns;

import java.util.Collection;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;

public interface ISubstitutionStorage<T> extends INBTSerializable<NBTTagCompound> {

	void setOption(T input, int option);

	int getOption(T input);

	Collection<T> getOptions();
}
