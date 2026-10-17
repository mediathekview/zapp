package de.christinecoenen.code.zapp.app.personal.series

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.christinecoenen.code.zapp.R
import de.christinecoenen.code.zapp.databinding.DialogEditSeriesBinding
import de.christinecoenen.code.zapp.models.collections.ShowCollection

/**
 * Dialog to create a new or edit an existing [ShowCollection].
 */
class EditSeriesDialog : AppCompatDialogFragment() {

	companion object {

		const val REQUEST_KEY_SAVE = "REQUEST_KEY_SERIES_SAVE"
		const val RESULT_ID = "RESULT_SERIES_ID"
		const val RESULT_NAME = "RESULT_SERIES_NAME"
		const val RESULT_QUERY = "RESULT_SERIES_QUERY"
		const val RESULT_EXCLUDE_TERMS = "RESULT_SERIES_EXCLUDE_TERMS"
		const val RESULT_DETECT_EPISODES = "RESULT_SERIES_DETECT_EPISODES"
		const val RESULT_SORT_BY_EPISODES = "RESULT_SERIES_SORT_BY_EPISODES"
		const val RESULT_SORT_EPISODES_ASCENDING = "RESULT_SERIES_SORT_EPISODES_ASCENDING"

		private const val ARG_ID = "ARG_SERIES_ID"
		private const val ARG_NAME = "ARG_SERIES_NAME"
		private const val ARG_QUERY = "ARG_SERIES_QUERY"
		private const val ARG_EXCLUDE_TERMS = "ARG_SERIES_EXCLUDE_TERMS"
		private const val ARG_DETECT_EPISODES = "ARG_SERIES_DETECT_EPISODES"
		private const val ARG_SORT_BY_EPISODES = "ARG_SERIES_SORT_BY_EPISODES"
		private const val ARG_SORT_EPISODES_ASCENDING = "ARG_SERIES_SORT_EPISODES_ASCENDING"

		/**
		 * @param collection Collection to edit or null to create a new one.
		 */
		@JvmStatic
		fun newInstance(collection: ShowCollection?): EditSeriesDialog {
			return EditSeriesDialog().apply {
				arguments = bundleOf(
					ARG_ID to (collection?.id ?: 0),
					ARG_NAME to (collection?.name.orEmpty()),
					ARG_QUERY to (collection?.searchQuery.orEmpty()),
					ARG_EXCLUDE_TERMS to (collection?.excludeTerms.orEmpty()),
					ARG_DETECT_EPISODES to (collection?.detectEpisodes ?: false),
					ARG_SORT_BY_EPISODES to (collection?.sortByEpisodes ?: false),
					ARG_SORT_EPISODES_ASCENDING to (collection?.sortEpisodesAscending ?: true)
				)
			}
		}
	}

	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
		val binding = DialogEditSeriesBinding.inflate(layoutInflater)
		val args = requireArguments()
		val collectionId = args.getInt(ARG_ID)

		binding.name.setText(args.getString(ARG_NAME))
		binding.query.setText(args.getString(ARG_QUERY))
		binding.excludeTerms.setText(args.getString(ARG_EXCLUDE_TERMS))
		binding.detectEpisodes.isChecked = args.getBoolean(ARG_DETECT_EPISODES)
		binding.sortByEpisodes.isChecked = args.getBoolean(ARG_SORT_BY_EPISODES)
		binding.sortDirection.check(
			if (args.getBoolean(ARG_SORT_EPISODES_ASCENDING, true)) R.id.sort_ascending
			else R.id.sort_descending
		)

		updateSortByEpisodesEnabled(binding)

		binding.detectEpisodes.setOnCheckedChangeListener { _, _ ->
			updateSortByEpisodesEnabled(binding)
		}

		binding.sortByEpisodes.setOnCheckedChangeListener { _, _ ->
			updateSortByEpisodesEnabled(binding)
		}

		val dialog = MaterialAlertDialogBuilder(requireActivity())
			.setTitle(
				if (collectionId == 0) R.string.dialog_series_create_title
				else R.string.dialog_series_edit_title
			)
			.setView(binding.root)
			.setNegativeButton(android.R.string.cancel, null)
			.setPositiveButton(android.R.string.ok, null)
			.create()

		dialog.setOnShowListener {
			dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
				val name = binding.name.text?.toString().orEmpty().trim()
				val searchQuery = binding.query.text?.toString().orEmpty().trim()
				val excludeTerms = binding.excludeTerms.text?.toString().orEmpty().trim()
				val detectEpisodes = binding.detectEpisodes.isChecked
				val sortByEpisodes = detectEpisodes && binding.sortByEpisodes.isChecked
				val sortEpisodesAscending = binding.sortDirection.checkedButtonId != R.id.sort_descending

				if (name.isEmpty()) {
					binding.nameLayout.error = getString(R.string.error_series_missing_name)
					return@setOnClickListener
				}
				binding.nameLayout.error = null

				if (searchQuery.isEmpty()) {
					binding.queryLayout.error = getString(R.string.error_series_missing_query)
					return@setOnClickListener
				}
				binding.queryLayout.error = null

				setFragmentResult(
					REQUEST_KEY_SAVE,
					bundleOf(
						RESULT_ID to collectionId,
						RESULT_NAME to name,
						RESULT_QUERY to searchQuery,
						RESULT_EXCLUDE_TERMS to excludeTerms,
						RESULT_DETECT_EPISODES to detectEpisodes,
						RESULT_SORT_BY_EPISODES to sortByEpisodes,
						RESULT_SORT_EPISODES_ASCENDING to sortEpisodesAscending
					)
				)
				dismiss()
			}
		}

		return dialog
	}

	/**
	 * Sorting by episode only works if episodes are detected.
	 */
	private fun updateSortByEpisodesEnabled(binding: DialogEditSeriesBinding) {
		val sortEnabled = binding.detectEpisodes.isChecked && binding.sortByEpisodes.isChecked

		binding.sortByEpisodes.isEnabled = binding.detectEpisodes.isChecked
		binding.sortAscending.isEnabled = sortEnabled
		binding.sortDescending.isEnabled = sortEnabled
	}
}
