package de.christinecoenen.code.zapp.app.personal.details

import de.christinecoenen.code.zapp.R
import org.koin.androidx.viewmodel.ext.android.viewModel

class DownloadsFragment : DetailsBaseFragment() {

	override val viewModel: DownloadsViewModel by viewModel()

	override val noShowsStringResId = R.string.fragment_personal_no_results_downloads
	override val noShowsIconResId = R.drawable.ic_baseline_save_alt_24

}
