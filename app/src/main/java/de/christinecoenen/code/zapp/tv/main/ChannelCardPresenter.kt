package de.christinecoenen.code.zapp.tv.main

import android.app.Application
import android.view.ViewGroup
import androidx.leanback.widget.Presenter
import androidx.lifecycle.LifecycleOwner
import de.christinecoenen.code.zapp.app.livestream.ui.ProgramInfoViewModel
import de.christinecoenen.code.zapp.models.channels.ChannelModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

class ChannelCardPresenter(
	private val lifecycleOwner: LifecycleOwner
) : Presenter(), KoinComponent {

	override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
		return ChannelCardViewHolder(ChannelCardView(parent.context), lifecycleOwner)
	}

	override fun onBindViewHolder(viewHolder: ViewHolder, item: Any) {
		val testViewHolder = viewHolder as ChannelCardViewHolder

		val programInfoViewModel =
			ProgramInfoViewModel(viewHolder.view.context.applicationContext as Application, get())

		val channel = item as ChannelModel
		testViewHolder.setChannel(programInfoViewModel, channel)
	}

	override fun onUnbindViewHolder(viewHolder: ViewHolder) {
		(viewHolder as ChannelCardViewHolder).recycle()
	}
}
