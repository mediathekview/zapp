package de.christinecoenen.code.zapp.app.mediathek.ui.detail.dialogs

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import de.christinecoenen.code.zapp.R
import de.christinecoenen.code.zapp.models.shows.MediathekShow
import de.christinecoenen.code.zapp.models.shows.Quality

class SelectQualityDialog : AppCompatDialogFragment() {

	companion object {

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

	}

	private lateinit var mode: Mode
	private lateinit var qualities: List<Quality>
	private lateinit var qualityLabels: List<String>
	private lateinit var listener: Listener

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

	override fun onAttach(context: Context) {
		super.onAttach(context)
		listener = if (targetFragment is Listener) {
			targetFragment as Listener
		} else {
			throw IllegalArgumentException("Parent fragment must implement ConfirmFileDeletionDialog.Listener interface.")
		}
	}

	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
		return AlertDialog.Builder(requireActivity())
			.setTitle(R.string.fragment_mediathek_qualities_title)
			.setItems(qualityLabels.toTypedArray(), ::onItemSelected)
			.setNegativeButton(android.R.string.cancel, null)
			.create()
	}

	private fun onItemSelected(dialogInterface: DialogInterface, i: Int) {
		val quality = qualities[i]
		when (mode) {
			Mode.DOWNLOAD -> listener.onDownloadQualitySelected(quality)
			Mode.SHARE -> listener.onShareQualitySelected(quality)
		}
	}

	enum class Mode {
		DOWNLOAD, SHARE
	}

	internal interface Listener {
		fun onDownloadQualitySelected(quality: Quality?)
		fun onShareQualitySelected(quality: Quality?)
	}
}
