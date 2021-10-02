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
	private val programInfoViewModel: ProgramInfoViewModel
) : BottomSheetDialogFragment() {

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

		binding.root.viewTreeObserver.addOnGlobalLayoutListener(::updatePeekHeight)

		return binding.root
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}

	private fun onTitleChanged(title: String) {
		binding.textShowTitle.text = title
	}

	private fun onSubtitleChanged(subtitle: String?) {
		binding.textShowSubtitle.isVisible = !subtitle.isNullOrEmpty()
		binding.textShowSubtitle.text = subtitle
	}

	private fun onDescriptionChanged(description: String?) {
		if (description.isNullOrEmpty()) {
			binding.textShowDescription.isVisible = false
			return
		}

		val htmldescription = HtmlCompat.fromHtml(description, HtmlCompat.FROM_HTML_MODE_LEGACY)
		binding.textShowDescription.text = htmldescription
		binding.textShowDescription.isVisible = true
	}

	private fun onTimeChanged(time: String?) {
		binding.textShowTime.isVisible = !time.isNullOrEmpty()
		binding.textShowTime.text = time
	}

	private fun onProgressPercentChanged(progressPercent: Float?) {
		if (progressPercent == null) {
			binding.progressbarShowProgress.visibility = View.INVISIBLE
			return
		}

		binding.progressbarShowProgress.progress =
			(progressPercent * binding.progressbarShowProgress.max).roundToInt()
		binding.progressbarShowProgress.visibility = View.VISIBLE
	}

	/**
	 * Sheet should reveal most important info only on initial show.
	 */
	private fun updatePeekHeight() {
		val behavior = (dialog as BottomSheetDialog).behavior
		behavior.peekHeight = binding.progressbarShowProgress.bottom + 20
	}

	companion object {
		const val TAG = "ProgramInfoSheetDialogFragment"
	}
}
