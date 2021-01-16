package de.christinecoenen.code.zapp.app.main

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import de.christinecoenen.code.zapp.app.downloads.ui.list.DownloadsFragment
import de.christinecoenen.code.zapp.app.livestream.ui.list.ChannelListFragment
import de.christinecoenen.code.zapp.app.main.PageType.*
import de.christinecoenen.code.zapp.app.mediathek.ui.list.MediathekListFragment

internal class MainPageAdapter(
	fragmentActivity: FragmentActivity,
	private val mainViewModel: MainViewModel
) : FragmentStateAdapter(fragmentActivity) {

	override fun createFragment(position: Int): Fragment =
		when (mainViewModel.getPageTypeAt(position)) {
			PAGE_CHANNEL_LIST -> ChannelListFragment.newInstance()
			PAGE_MEDIATHEK_LIST -> MediathekListFragment.instance
			PAGE_DOWNLOADS -> DownloadsFragment.newInstance()
		}

	override fun getItemCount(): Int = mainViewModel.pageCount
}
