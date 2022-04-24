package de.christinecoenen.code.zapp.app.about.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import de.christinecoenen.code.zapp.R
import de.christinecoenen.code.zapp.databinding.ChangelogFragmentBinding
import de.christinecoenen.code.zapp.utils.io.IoUtils.readAllText
import ru.noties.markwon.Markwon

class ChangelogFragment : Fragment() {

	private var _binding: ChangelogFragmentBinding? = null
	private val binding: ChangelogFragmentBinding get() = _binding!!

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {

		_binding = ChangelogFragmentBinding.inflate(inflater, container, false)

		val markdown = resources.readAllText(R.raw.changelog)
		Markwon.setMarkdown(binding.txtChangelog, markdown)

		return binding.root
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}
}
