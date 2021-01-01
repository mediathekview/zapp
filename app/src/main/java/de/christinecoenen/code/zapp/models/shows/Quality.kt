package de.christinecoenen.code.zapp.models.shows

import androidx.annotation.StringRes
import de.christinecoenen.code.zapp.R

enum class Quality(@StringRes val labelResId: Int) {

	Low(R.string.fragment_mediathek_qualities_low),
	Medium(R.string.fragment_mediathek_qualities_medium),
	High(R.string.fragment_mediathek_qualities_high);

}
