package com.walhay.gregtechenergistics.api.capability;

public interface ISubstitutionStorage {

	void registerNewOption(String input);

	void setOption(String input, int option);

	int getOption(String input);
}
