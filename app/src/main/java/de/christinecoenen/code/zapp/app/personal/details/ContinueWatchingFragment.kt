package de.christinecoenen.code.zapp.app.personal.details

import de.christinecoenen.code.zapp.R
import org.koin.androidx.viewmodel.ext.android.viewModel

class ContinueWatchingFragment : DetailsBaseFragment() {

	override val viewModel: ContinueWatchingViewModel by viewModel()

	override val noShowsStringResId = R.string.fragment_personal_no_results_continue_watching
	override val noShowsIconResId = R.drawable.ic_outline_play_circle_24
	override val searchQueryHintResId = R.string.search_query_hint_continue_watching

}
