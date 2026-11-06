package de.christinecoenen.code.zapp.app.mediathek.api.request

enum class MediathekChannel(val apiId: String, val label: String = apiId) {

	Ard("ARD"),
	ArdAlpha("ARD-alpha"),
	Zdf("ZDF"),
	Swr("SWR"),
	Ndr("NDR"),
	Br("BR"),
	Arte("ARTE.DE", "ARTE"),
	Dw("DW"),
	DreiSat("3Sat"),
	Sr("SR"),
	Srf("SRF"),
	Mdr("MDR"),
	KiKa("KiKA"),
	Hr("HR"),
	Rbb("RBB"),
	Orf("ORF"),
	Wdr("WDR"),
	Phoenix("PHOENIX"),
	Funk("Funk.net", "Funk"),
	RbTv("rbtv"),
	ZdfTivi("ZDF-tivi", "ZDFtivi"),
	One("One"),
	Tagesschau24("tagesschau24"),
}
