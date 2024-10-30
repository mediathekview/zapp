package de.christinecoenen.code.zapp.app.mediathek.ui.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import de.christinecoenen.code.zapp.R
import de.christinecoenen.code.zapp.databinding.MediathekListBaseFragmentBinding

class MediathekListBaseFragment: Fragment() {
	private var _binding: MediathekListBaseFragmentBinding? = null
	private val binding get() = _binding!!

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		_binding = MediathekListBaseFragmentBinding.inflate(layoutInflater)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		binding.mediathekPager.adapter = MediathekListAdapter(this)
		val mediator = TabLayoutMediator(binding.tabLayout, binding.mediathekPager) { tab, position ->
			tab.text = when (position) {
				0 -> getString(R.string.fragment_mediathek_recent)
				1 -> getString(R.string.fragment_mediathek_subscriptions)
				else -> throw IllegalStateException("Only two tabs implemented.")
			}
		}
		mediator.attach()
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}
}

class MediathekListAdapter(fragment: Fragment): FragmentStateAdapter(fragment) {
	override fun getItemCount(): Int = 2

	override fun createFragment(position: Int): Fragment {
		val subscriptionsOnly = position == 1

		return MediathekListFragment().apply {
			arguments = bundleOf(MediathekListFragment.EXTRA_SUBSCRIPTIONS_ONLY to subscriptionsOnly)
		}
	}
}
