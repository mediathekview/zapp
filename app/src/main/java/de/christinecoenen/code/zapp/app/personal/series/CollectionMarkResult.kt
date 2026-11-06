package de.christinecoenen.code.zapp.app.personal.series

import android.widget.Toast
import androidx.fragment.app.Fragment
import de.christinecoenen.code.zapp.R

/**
 * Result of marking all shows of a collection as watched or unwatched.
 */
data class CollectionMarkResult(
	val action: Action,
	val count: Int,
	val failed: Boolean = false
) {
	enum class Action { WATCHED, UNWATCHED }
}

/**
 * Shows a toast describing the given result.
 */
fun Fragment.showCollectionMarkResult(result: CollectionMarkResult) {
	if (result.failed) {
		Toast.makeText(
			requireContext(),
			R.string.fragment_series_mark_all_failed,
			Toast.LENGTH_LONG
		).show()
		return
	}

	val messageResId = when (result.action) {
		CollectionMarkResult.Action.WATCHED ->
			if (result.count > 0) R.string.fragment_series_mark_all_watched_success
			else R.string.fragment_series_mark_all_watched_none

		CollectionMarkResult.Action.UNWATCHED ->
			if (result.count > 0) R.string.fragment_series_mark_all_unwatched_success
			else R.string.fragment_series_mark_all_unwatched_none
	}

	Toast.makeText(
		requireContext(),
		getString(messageResId, result.count),
		Toast.LENGTH_SHORT
	).show()
}
