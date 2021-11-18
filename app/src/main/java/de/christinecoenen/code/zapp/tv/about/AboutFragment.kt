package de.christinecoenen.code.zapp.tv.about

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import de.christinecoenen.code.zapp.databinding.TvFragmentAboutBinding
import de.christinecoenen.code.zapp.tv.changelog.ChangelogActivity


class AboutFragment : Fragment(), AboutItemListener {

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		val binding = TvFragmentAboutBinding.inflate(inflater, container, false)

		binding.grid.adapter = AboutListAdapter(this)
		binding.grid.layoutManager = GridLayoutManager(requireContext(), 2)

		return binding.root
	}

	override fun onclick(item: AboutItem) {
		val intent = ChangelogActivity.getStartIntent(requireContext())
		startActivity(intent)
	}
}
