package com.walhay.gregifiedenergistics.api.patterns;

import java.util.Collection;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;

public interface ISubstitutionStorage extends INBTSerializable<NBTTagCompound> {

	void setOption(String input, int option);

	int getOption(String input);

	Collection<String> getOptions();
}
