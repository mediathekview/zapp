package de.christinecoenen.code.zapp.tv.main

import android.content.Context
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import de.christinecoenen.code.zapp.R
import de.christinecoenen.code.zapp.tv.channels.ChannelListFragment
import kotlin.reflect.KClass

class MainNavPagerAdapter(
	private val context: Context,
	fragmentManger: FragmentManager
) : FragmentPagerAdapter(fragmentManger) {

	private val navItems = listOf(
		MainNavItem(R.string.activity_main_tab_live, ChannelListFragment::class),
		MainNavItem(R.string.menu_about_short, ChannelListFragment::class),
	)

	override fun getCount(): Int = navItems.size

	override fun getItem(position: Int): Fragment {
		return navItems[position].createFragment()
	}

	override fun getPageTitle(position: Int): CharSequence {
		return navItems[position].getTitle(context)
	}

	private data class MainNavItem<T : Fragment>(
		@StringRes val titleResId: Int,
		val fragmentClass: KClass<T>
	) {
		fun createFragment(): T = fragmentClass.java.newInstance()
		fun getTitle(context: Context) = context.getString(titleResId)
	}
}
