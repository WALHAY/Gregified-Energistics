package com.walhay.gregifiedenergistics.api.capability;

import gregtech.api.capability.GregtechDataCodes;

public class GEDataCodes {

	public static final int ONLINE_STATUS_UPDATE = autoId();
	public static final int PATTERNS_CHANGE = autoId();
	public static final int CHANGE_OPTICAL_SIDE = autoId();

	private static int autoId() {
		return GregtechDataCodes.assignId();
	}
}
