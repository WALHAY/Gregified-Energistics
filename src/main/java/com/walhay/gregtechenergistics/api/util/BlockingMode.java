package com.walhay.gregtechenergistics.api.util;

public enum BlockingMode {
	NO_BLOCKING,
	BLOCKING_MODE,
	CRAFTING_BLOCKING_MODE;

	public boolean isBlockingEnabled() {
		return this == BLOCKING_MODE || this == CRAFTING_BLOCKING_MODE;
	}
}
