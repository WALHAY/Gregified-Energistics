package com.walhay.gregifiedenergistics.api.util;

import gregtech.api.unification.ore.OrePrefix;

public class OrePrefixHelper {

	public static void exposeOrePrefixes(String[] prefixes) {
		if (prefixes == null || prefixes.length == 0) return;

		for (String ore : prefixes) {
			OrePrefix prefix = OrePrefix.getPrefix(ore);

			if (prefix == null) continue;

			prefix.setMarkerPrefix(false);
		}
	}
}
