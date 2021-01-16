package de.christinecoenen.code.zapp.app.settings.ui

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import de.christinecoenen.code.zapp.R
import de.christinecoenen.code.zapp.databinding.ActivityChannelSelectionBinding
import de.christinecoenen.code.zapp.models.channels.ISortableChannelList
import de.christinecoenen.code.zapp.models.channels.json.SortableJsonChannelList
import de.christinecoenen.code.zapp.utils.view.GridAutofitLayoutManager
import de.christinecoenen.code.zapp.utils.view.SimpleDragListListener

class ChannelSelectionActivity : AppCompatActivity() {

	private lateinit var channelList: ISortableChannelList

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		val binding = ActivityChannelSelectionBinding.inflate(layoutInflater)
		setContentView(binding.root)

		// adapter
		channelList = SortableJsonChannelList(this)

		val listAdapter = ChannelSelectionAdapter(this)
		listAdapter.itemList = channelList.list

		// view
		val layoutManager = GridAutofitLayoutManager(this, 120)

		binding.draglistChannelSelection.apply {

			setLayoutManager(layoutManager)
			setAdapter(listAdapter, true)
			recyclerView.isVerticalScrollBarEnabled = true

			setDragListListener(object : SimpleDragListListener() {
				override fun onItemDragEnded(fromPosition: Int, toPosition: Int) {
					if (fromPosition != toPosition) {
						channelList.persistChannelOrder()
					}
				}
			})
		}
	}

	override fun onPause() {
		super.onPause()

		channelList.persistChannelOrder()
	}

	override fun onCreateOptionsMenu(menu: Menu): Boolean {
		menuInflater.inflate(R.menu.activity_channel_selection, menu)
		return super.onCreateOptionsMenu(menu)
	}

	override fun onOptionsItemSelected(item: MenuItem): Boolean {
		return when (item.itemId) {
			R.id.menu_help -> {
				openHelpDialog()
				true
			}
			else -> {
				super.onOptionsItemSelected(item)
			}
		}
	}

	private fun openHelpDialog() {
		ChannelSelectionHelpDialog().apply {
			show(supportFragmentManager, "help")
		}
	}
}
