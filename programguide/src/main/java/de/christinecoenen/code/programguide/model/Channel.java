package de.christinecoenen.code.programguide.model;


public enum Channel {
	DAS_ERSTE("das_erste"),
	BR_NORD("br_nord"),
	BR_SUED("br_sued"),
	HR("hr"),
	MDR_SACHSEN("mdr_sachsen"),
	MDR_SACHSEN_ANHALT("mdr_sachsen_anhalt"),
	MDR_THUERINGEN("mdr_thueringen"),
	NDR_HAMBURG("ndr_hh"),
	NDR_MECKLENBURG_VORPOMMERN("ndr_mv"),
	NDR_NIEDERSACHSEN("ndr_nds"),
	NDR_SCHLESWIG_HOLSTEIN("ndr_sh"),
	RBB_BERLIN("rbb_berlin"),
	RBB_BRANDENBURG("rbb_brandenburg"),
	SR("sr"),
	SWR_BADEN_WUERTTEMBERG("swr_bw"),
	SWR_RHEINLAND_PFALZ("swr_rp"),
	WDR("wdr"),
	ARD_ALPHA("ard_alpha"),
	TAGESSCHAU24("tagesschau24"),
	ONE("one"),

	ARTE("arte"),

	ZDF("zdf"),
	DREISAT("dreisat"),
	KIKA("kika"),
	PHOENIX("phoenix"),
	ZDF_KULTUR("zdf_kultur"),
	ZDF_INFO("zdf_info"),
	ZDF_NEO("zdf_neo");

	public static Channel getById(String id) {
		for (Channel channel : Channel.values()) {
			if (channel.id.equals(id)) {
				return channel;
			}
		}
		throw new IllegalArgumentException();
	}

	private final String id;

	Channel(String id) {
		this.id = id;
	}
}
