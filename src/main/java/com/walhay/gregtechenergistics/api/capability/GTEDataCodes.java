package com.walhay.gregtechenergistics.api.capability;

public class GTEDataCodes {

	public static final int ONLINE_STATUS_UPDATE = autoId();
	public static final int PATTERN_DATA_UPDATE = autoId();
	public static final int PATTERNS_CHANGE = autoId();
	public static final int CHANGE_OPTICAL_SIDE = autoId();

	private static int i = 1234;

	private static int autoId() {
		return i++;
	}
}
