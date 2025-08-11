package com.walhay.gregifiedenergistics.api.patterns;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;

public interface ISubstitutionStorage extends INBTSerializable<NBTTagCompound> {

	void registerNewOption(String input);

	void setOption(String input, int option);

	int getOption(String input);
}
