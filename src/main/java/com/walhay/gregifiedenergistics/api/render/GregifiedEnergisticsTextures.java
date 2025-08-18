package com.walhay.gregifiedenergistics.api.render;

import gregtech.client.renderer.texture.cube.SimpleOverlayRenderer;

public class GregifiedEnergisticsTextures {

	public static SimpleOverlayRenderer ME_AL_HATCH_CONNECTOR_ACTIVE;
	public static SimpleOverlayRenderer ME_AL_HATCH_CONNECTOR_WAITING;
	public static SimpleOverlayRenderer ME_AL_HATCH_CONNECTOR_INACTIVE;

	public static void init() {
		ME_AL_HATCH_CONNECTOR_ACTIVE = new SimpleOverlayRenderer("me_assembly_line_hatch/me_connector_active");
		ME_AL_HATCH_CONNECTOR_WAITING = new SimpleOverlayRenderer("me_assembly_line_hatch/me_connector_waiting");
		ME_AL_HATCH_CONNECTOR_INACTIVE = new SimpleOverlayRenderer("me_assembly_line_hatch/me_connector_inactive");
	}
}
