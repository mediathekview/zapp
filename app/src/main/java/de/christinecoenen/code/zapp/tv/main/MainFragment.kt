package de.christinecoenen.code.zapp.tv.main

import android.os.Bundle
import androidx.leanback.app.VerticalGridSupportFragment
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.VerticalGridPresenter
import de.christinecoenen.code.zapp.R
import de.christinecoenen.code.zapp.models.channels.ChannelModel
import de.christinecoenen.code.zapp.models.channels.ISortableChannelList
import de.christinecoenen.code.zapp.models.channels.json.SortableVisibleJsonChannelList
import timber.log.Timber


class MainFragment : VerticalGridSupportFragment() {

	private lateinit var channelList: ISortableChannelList

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		title = getString(R.string.app_name)

		channelList = SortableVisibleJsonChannelList(requireContext())

		val adapter = ArrayObjectAdapter(ChannelCardPresenter())
		setAdapter(adapter)

		for (channel in channelList) {
			adapter.add(channel)
		}

		val gridPresenter = VerticalGridPresenter()
		gridPresenter.numberOfColumns = 5
		gridPresenter.setOnItemViewClickedListener { _, item, _, _ ->
			val channel = item as ChannelModel
			Timber.d("channel clicked: %s", channel.name)
			// TODO: start player
		}
		setGridPresenter(gridPresenter)
	}
}
