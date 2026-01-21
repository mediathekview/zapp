package de.christinecoenen.code.zapp.app.livestream.ui.detail

import android.app.Application
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.HtmlCompat
import androidx.core.view.isVisible
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import de.christinecoenen.code.zapp.app.livestream.ui.ProgramInfoViewModel
import de.christinecoenen.code.zapp.databinding.ProgramInfoSheetDialogFragmentBinding
import de.christinecoenen.code.zapp.utils.system.LifecycleOwnerHelper.launchOnCreated
import org.koin.android.ext.android.get
import kotlin.math.roundToInt

class ProgramInfoSheetDialogFragment() : BottomSheetDialogFragment() {

	enum class Size {
		Small, Large
	}

	private var _binding: ProgramInfoSheetDialogFragmentBinding? = null
	private val binding: ProgramInfoSheetDialogFragmentBinding get() = _binding!!

	private var programInfoViewModel: ProgramInfoViewModel? = null

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		_binding = ProgramInfoSheetDialogFragmentBinding.inflate(inflater, container, false)

		val size = requireArguments().getSerializable(ARGUMENT_SIZE) as Size
		val channelId = requireArguments().getString(ARGUMENT_CHANNEL_ID)!!

		programInfoViewModel =
			ProgramInfoViewModel(requireContext().applicationContext as Application, get())
				.apply {
					viewLifecycleOwner.launchOnCreated {
						setChannelId(channelId)
						title.observe(viewLifecycleOwner, ::onTitleChanged)
						subtitle.observe(viewLifecycleOwner, ::onSubtitleChanged)
						description.observe(viewLifecycleOwner, ::onDescriptionChanged)
						time.observe(viewLifecycleOwner, ::onTimeChanged)
						progressPercent.observe(viewLifecycleOwner, ::onProgressPercentChanged)
					}
				}

		if (size == Size.Small) {
			binding.root.viewTreeObserver.addOnGlobalLayoutListener(::setMinimalPeekHeight)
		}

		return binding.root
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
		programInfoViewModel = null
	}

	private fun onTitleChanged(title: String) {
		binding.title.text = HtmlCompat.fromHtml(title, HtmlCompat.FROM_HTML_MODE_LEGACY)
	}

	private fun onSubtitleChanged(subtitle: String?) {
		binding.subtitle.isVisible = !subtitle.isNullOrEmpty()

		if (!subtitle.isNullOrEmpty()) {
			binding.subtitle.text = HtmlCompat.fromHtml(subtitle, HtmlCompat.FROM_HTML_MODE_LEGACY)
		}
	}

	private fun onDescriptionChanged(description: String?) {
		if (description.isNullOrEmpty()) {
			binding.description.isVisible = false
			return
		}

		val htmldescription = HtmlCompat.fromHtml(description, HtmlCompat.FROM_HTML_MODE_LEGACY)
		binding.description.text = htmldescription
		binding.description.isVisible = true
	}

	private fun onTimeChanged(time: String?) {
		binding.time.isVisible = !time.isNullOrEmpty()
		binding.time.text = time
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
