package de.christinecoenen.code.zapp.app.mediathek.model;

import de.christinecoenen.code.zapp.R;

public enum Quality {
	Low(R.string.fragment_mediathek_qualities_low),
	Medium(R.string.fragment_mediathek_qualities_medium),
	High(R.string.fragment_mediathek_qualities_high);

	private final int labelResId;

	Quality(int labelResId) {
		this.labelResId = labelResId;
	}

	public int getLabelResId() {
		return labelResId;
	}
}
