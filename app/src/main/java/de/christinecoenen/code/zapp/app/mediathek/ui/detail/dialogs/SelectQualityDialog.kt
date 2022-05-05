package de.christinecoenen.code.zapp.app.mediathek.ui.detail.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.setFragmentResult
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.christinecoenen.code.zapp.R
import de.christinecoenen.code.zapp.models.shows.MediathekShow
import de.christinecoenen.code.zapp.models.shows.Quality

class SelectQualityDialog : AppCompatDialogFragment() {

	companion object {

		const val REQUEST_KEY_SELECT_QUALITY = "REQUEST_KEY_SELECT_QUALITY"
		private const val REQUEST_KEY_SELECT_QUALITY_KEY_QUALITY =
			"REQUEST_KEY_SELECT_QUALITY_KEY_QUALITY"
		private const val ARGUMENT_MEDIATHEK_SHOW = "ARGUMENT_MEDIATHEK_SHOW"
		private const val ARGUMENT_MODE = "ARGUMENT_MODE"

		@JvmStatic
		fun newInstance(mediathekShow: MediathekShow, mode: Mode): SelectQualityDialog {
			return SelectQualityDialog().apply {
				arguments = Bundle().apply {
					putSerializable(ARGUMENT_MEDIATHEK_SHOW, mediathekShow)
					putSerializable(ARGUMENT_MODE, mode)
				}
			}
		}

		@JvmStatic
		fun getSelectedQuality(bundle: Bundle): Quality {
			return bundle.getSerializable(REQUEST_KEY_SELECT_QUALITY_KEY_QUALITY) as Quality
		}
	}

	private lateinit var mode: Mode
	private lateinit var qualities: List<Quality>
	private lateinit var qualityLabels: List<String>

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		mode = requireArguments().getSerializable(ARGUMENT_MODE) as Mode

		val show = requireArguments().getSerializable(ARGUMENT_MEDIATHEK_SHOW) as MediathekShow

		qualities = when (mode) {
			Mode.DOWNLOAD -> show.supportedDownloadQualities
			Mode.SHARE -> show.supportedStreamingQualities
		}

		qualityLabels = qualities.map { getString(it.labelResId) }
	}

	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
		return MaterialAlertDialogBuilder(requireActivity())
			.setTitle(R.string.fragment_mediathek_qualities_title)
			.setItems(qualityLabels.toTypedArray()) { _, i ->
				onItemSelected(qualities[i])
			}
			.setNegativeButton(android.R.string.cancel, null)
			.create()
	}

	private fun onItemSelected(quality: Quality) {
		val bundle = Bundle().apply {
			putSerializable(REQUEST_KEY_SELECT_QUALITY_KEY_QUALITY, quality)
		}

		setFragmentResult(REQUEST_KEY_SELECT_QUALITY, bundle)
	}

	enum class Mode {
		DOWNLOAD, SHARE
	}
}
