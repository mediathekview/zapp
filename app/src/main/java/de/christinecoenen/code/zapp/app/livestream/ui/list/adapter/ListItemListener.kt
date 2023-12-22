package de.christinecoenen.code.zapp.app.livestream.ui.list.adapter

import android.view.View
import de.christinecoenen.code.zapp.app.livestream.ui.ProgramInfoViewModel
import de.christinecoenen.code.zapp.models.channels.ChannelModel

interface ListItemListener {

	fun onItemClick(channel: ChannelModel)

	fun onItemLongClick(channel: ChannelModel, programInfoViewModel: ProgramInfoViewModel, view: View)

}
