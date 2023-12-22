package de.christinecoenen.code.zapp.app.livestream.ui.detail

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
import kotlin.math.roundToInt

class ProgramInfoSheetDialogFragment(
	private val programInfoViewModel: ProgramInfoViewModel,
	private val size: Size,
) : BottomSheetDialogFragment() {

	enum class Size {
		Small, Large
	}

	private var _binding: ProgramInfoSheetDialogFragmentBinding? = null
	private val binding: ProgramInfoSheetDialogFragmentBinding get() = _binding!!

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		_binding = ProgramInfoSheetDialogFragmentBinding.inflate(inflater, container, false)

		programInfoViewModel.title.observe(this, ::onTitleChanged)
		programInfoViewModel.subtitle.observe(this, ::onSubtitleChanged)
		programInfoViewModel.description.observe(this, ::onDescriptionChanged)
		programInfoViewModel.time.observe(this, ::onTimeChanged)
		programInfoViewModel.progressPercent.observe(this, ::onProgressPercentChanged)

		if (size == Size.Small) {
			binding.root.viewTreeObserver.addOnGlobalLayoutListener(::setMinimalPeekHeight)
		}

		return binding.root
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}

	private fun onTitleChanged(title: String) {
		binding.title.text = title
	}

	private fun onSubtitleChanged(subtitle: String?) {
		binding.subtitle.isVisible = !subtitle.isNullOrEmpty()
		binding.subtitle.text = subtitle
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
	}
}
