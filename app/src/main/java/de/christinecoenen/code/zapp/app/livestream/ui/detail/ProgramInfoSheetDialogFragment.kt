package de.christinecoenen.code.zapp.app.livestream.ui.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.HtmlCompat
import androidx.core.view.isVisible
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import de.christinecoenen.code.zapp.app.livestream.model.LiveShow
import de.christinecoenen.code.zapp.app.livestream.ui.ProgramInfoViewModel
import de.christinecoenen.code.zapp.databinding.ProgramInfoSheetDialogFragmentBinding
import de.christinecoenen.code.zapp.utils.system.LifecycleOwnerHelper.launchOnCreated
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.math.roundToInt

class ProgramInfoSheetDialogFragment() : BottomSheetDialogFragment() {

	enum class Size {
		Small, Large
	}

	private var _binding: ProgramInfoSheetDialogFragmentBinding? = null
	private val binding: ProgramInfoSheetDialogFragmentBinding get() = _binding!!

	private val programInfoViewModel: ProgramInfoViewModel by viewModel()

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		_binding = ProgramInfoSheetDialogFragmentBinding.inflate(inflater, container, false)

		val size = requireArguments().getSerializable(ARGUMENT_SIZE) as Size
		val channelId = requireArguments().getString(ARGUMENT_CHANNEL_ID)!!


		viewLifecycleOwner.launchOnCreated {
			programInfoViewModel.liveShow
				.observe(viewLifecycleOwner, ::onLiveShowChanged)
			programInfoViewModel.progressPercent
				.observe(viewLifecycleOwner, ::onProgressPercentChanged)

			programInfoViewModel.setChannelId(channelId)
		}

		if (size == Size.Small) {
			binding.root.viewTreeObserver.addOnGlobalLayoutListener(::setMinimalPeekHeight)
		}

		return binding.root
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}

	private fun onLiveShowChanged(liveShow: LiveShow) {
		// title
		binding.title.text =
			HtmlCompat.fromHtml(liveShow.title, HtmlCompat.FROM_HTML_MODE_LEGACY)

		// subtitle
		val subtitle = liveShow.subtitle
		binding.subtitle.isVisible = !subtitle.isNullOrEmpty()

		if (!subtitle.isNullOrEmpty()) {
			binding.subtitle.text = HtmlCompat.fromHtml(subtitle, HtmlCompat.FROM_HTML_MODE_LEGACY)
		}

		// description
		val description = liveShow.description
		binding.description.isVisible = !description.isNullOrEmpty()

		if (!description.isNullOrEmpty()) {
			binding.description.text =
				HtmlCompat.fromHtml(description, HtmlCompat.FROM_HTML_MODE_LEGACY)
		}

		// time
		val formattedTime = liveShow.formattedDuration(requireContext())
		binding.time.isVisible = formattedTime != null
		binding.time.text = formattedTime
	}

	private fun onProgressPercentChanged(progressPercent: Float?) {
		if (progressPercent == null) {
			binding.showProgress.visibility = View.INVISIBLE
			return
		}

		binding.showProgress.progress =
			(progressPercent * binding.showProgress.max).roundToInt()
		binding.showProgress.visibility = View.VISIBLE
	}

	/**
	 * Sheet should reveal most important info only on initial show.
	 */
	private fun setMinimalPeekHeight() {
		val behavior = (dialog as BottomSheetDialog).behavior
		behavior.peekHeight = binding.showProgress.bottom + 20
	}

	companion object {
		const val TAG = "ProgramInfoSheetDialogFragment"

		private const val ARGUMENT_CHANNEL_ID = "ARGUMENT_CHANNEL_ID"
		private const val ARGUMENT_SIZE = "ARGUMENT_SIZE"

		fun newInstance(channelId: String, size: Size): ProgramInfoSheetDialogFragment {
			return ProgramInfoSheetDialogFragment().apply {
				arguments = Bundle().apply {
					putSerializable(ARGUMENT_CHANNEL_ID, channelId)
					putSerializable(ARGUMENT_SIZE, size)
				}
			}
		}
	}
}
