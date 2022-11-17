package de.christinecoenen.code.zapp.app.personal.details

import de.christinecoenen.code.zapp.R
import org.koin.androidx.viewmodel.ext.android.viewModel

class BookmarksFragment : DetailsBaseFragment() {

	override val viewModel: BookmarksViewModel by viewModel()

	override val noShowsStringResId = R.string.fragment_personal_no_results_bookmarks
	override val noShowsIconResId = R.drawable.ic_baseline_bookmark_border_24

}
