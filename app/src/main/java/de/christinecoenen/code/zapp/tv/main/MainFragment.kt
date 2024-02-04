package de.christinecoenen.code.zapp.tv.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import de.christinecoenen.code.zapp.R
import de.christinecoenen.code.zapp.app.settings.repository.SettingsRepository
import de.christinecoenen.code.zapp.databinding.TvFragmentMainBinding
import org.koin.android.ext.android.inject


class MainFragment : Fragment() {

	private var _binding: TvFragmentMainBinding? = null
	private val binding: TvFragmentMainBinding get() = _binding!!

	private val settingsRepository: SettingsRepository by inject()

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		_binding = TvFragmentMainBinding.inflate(inflater, container, false)

		binding.viewpager.adapter = MainNavPagerAdapter(requireContext(), parentFragmentManager)
		binding.tabs.setupWithViewPager(binding.viewpager)

		val selectedTabIndex = when (settingsRepository.startFragment) {
			R.id.mediathekListFragment -> 1
			else -> 0
		}
		val selectedTab = binding.tabs.getTabAt(selectedTabIndex)
		binding.tabs.selectTab(selectedTab)
		selectedTab?.view?.requestFocus()

		return binding.root
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}

	fun onBackPressed(): Boolean {
		return if (binding.tabs.hasFocus()) {
			// tabs have already focus - let parent handle back press
			false
		} else {
			// focus tabs before doing any other back press action
			binding.tabs.requestFocus()
			true
		}
	}
}
