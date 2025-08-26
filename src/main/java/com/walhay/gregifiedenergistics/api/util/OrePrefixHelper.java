package com.walhay.gregifiedenergistics.api.util;

import gregtech.api.unification.ore.OrePrefix;

public class OrePrefixHelper {

	public static void exposeOrePrefixes(String[] prefixes) {
		if (prefixes == null) return;

		for (String ore : prefixes) {
			OrePrefix prefix = OrePrefix.getPrefix(ore);

			if (prefix == null) continue;

			prefix.setMarkerPrefix(false);
		}
	}
}
