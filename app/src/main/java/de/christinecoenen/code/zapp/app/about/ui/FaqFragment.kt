package de.christinecoenen.code.zapp.app.about.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import de.christinecoenen.code.zapp.R
import de.christinecoenen.code.zapp.databinding.FaqFragmentBinding
import de.christinecoenen.code.zapp.utils.io.IoUtils.readAllText
import io.noties.markwon.Markwon
import org.koin.android.ext.android.inject

class FaqFragment : Fragment() {

	private val markwon: Markwon by inject()

	private var _binding: FaqFragmentBinding? = null
	private val binding: FaqFragmentBinding get() = _binding!!

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {

		_binding = FaqFragmentBinding.inflate(inflater, container, false)

		val markdown = resources.readAllText(R.raw.faq)
		markwon.setMarkdown(binding.txtFaq, markdown)

		return binding.root
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}
}
