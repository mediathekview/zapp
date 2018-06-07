package de.christinecoenen.code.zapp.app.livestream.ui.list


import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.view.ViewCompat
import android.view.*
import android.widget.PopupMenu
import de.christinecoenen.code.zapp.R
import de.christinecoenen.code.zapp.app.livestream.ui.detail.ChannelDetailActivity
import de.christinecoenen.code.zapp.model.ChannelModel
import de.christinecoenen.code.zapp.model.json.SortableJsonChannelList
import de.christinecoenen.code.zapp.utils.system.MultiWindowHelper
import de.christinecoenen.code.zapp.utils.view.GridAutofitLayoutManager
import kotlinx.android.synthetic.main.fragment_channel_list.view.*


class ChannelListFragment : Fragment(), ChannelListAdapter.Listener {

	private val channelList by lazy { SortableJsonChannelList(context!!) }
	private val gridAdapter by lazy { ChannelListAdapter(context!!, channelList, this) }

	private var longClickChannel: ChannelModel? = null


	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		val view = inflater.inflate(R.layout.fragment_channel_list, container, false)

		ViewCompat.setNestedScrollingEnabled(view.gridview_channels, true)
		view.gridview_channels.layoutManager = GridAutofitLayoutManager(context, 320)
		view.gridview_channels.adapter = gridAdapter

		return view
	}

	override fun onStart() {
		super.onStart()
		if (MultiWindowHelper.isInsideMultiWindow(activity)) {
			resumeActivity()
		}
	}

	override fun onResume() {
		super.onResume()
		channelList.reloadChannelOrder()

		if (!MultiWindowHelper.isInsideMultiWindow(activity)) {
			resumeActivity()
		}
	}

	override fun onPause() {
		super.onPause()
		if (!MultiWindowHelper.isInsideMultiWindow(activity)) {
			pauseActivity()
		}
	}

	override fun onStop() {
		super.onStop()
		if (MultiWindowHelper.isInsideMultiWindow(activity)) {
			pauseActivity()
		}
	}

	override fun onItemClick(channel: ChannelModel) {
		val intent = ChannelDetailActivity.getStartIntent(context!!, channel.id)
		startActivity(intent)
	}

	override fun onItemLongClick(channel: ChannelModel, view: View) {
		this.longClickChannel = channel

		val menu = PopupMenu(context, view, Gravity.TOP or Gravity.END)
		menu.inflate(R.menu.activity_channel_list_context)
		menu.show()
		menu.setOnMenuItemClickListener(this::onContextMenuItemClicked)
	}

	private fun onContextMenuItemClicked(menuItem: MenuItem): Boolean {
		return when (menuItem.itemId) {
			R.id.menu_share -> {
				startActivity(longClickChannel!!.videoShareIntent)
				true
			}
			else -> false
		}
	}

	private fun pauseActivity() {
		gridAdapter.pause()
	}

	private fun resumeActivity() {
		gridAdapter.resume()
	}

	companion object {
		@JvmStatic
		val instance: ChannelListFragment
			get() = ChannelListFragment()
	}
}
