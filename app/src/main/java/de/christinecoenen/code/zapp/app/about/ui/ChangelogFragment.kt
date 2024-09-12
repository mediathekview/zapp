package de.christinecoenen.code.zapp.app.about.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import de.christinecoenen.code.zapp.R
import de.christinecoenen.code.zapp.databinding.ChangelogFragmentBinding
import de.christinecoenen.code.zapp.utils.io.IoUtils.readAllText
import de.christinecoenen.code.zapp.utils.system.SystemUiHelper.applyBottomInsetAsPadding
import io.noties.markwon.Markwon
import org.koin.android.ext.android.inject

class ChangelogFragment : Fragment() {

	private val markwon: Markwon by inject()

	private var _binding: ChangelogFragmentBinding? = null
	private val binding: ChangelogFragmentBinding get() = _binding!!

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {

		_binding = ChangelogFragmentBinding.inflate(inflater, container, false)

		val markdown = resources.readAllText(R.raw.changelog)
		markwon.setMarkdown(binding.txtChangelog, markdown)

		binding.txtChangelog.applyBottomInsetAsPadding()

		return binding.root
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}
}
