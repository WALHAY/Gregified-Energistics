package com.walhay.gregifiedenergistics.api.util;

public enum BlockingMode {
	NO_BLOCKING,
	BLOCKING_MODE,
	CRAFTING_BLOCKING_MODE;

	public static final String BLOCKING_MODE_TAG = "BlockingMode";

	public boolean isBlockingEnabled() {
		return this == BLOCKING_MODE || this == CRAFTING_BLOCKING_MODE;
	}
}
