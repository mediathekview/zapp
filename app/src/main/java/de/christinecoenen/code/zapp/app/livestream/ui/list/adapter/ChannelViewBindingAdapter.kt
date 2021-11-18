package de.christinecoenen.code.zapp.app.livestream.ui.list.adapter

import de.christinecoenen.code.zapp.databinding.FragmentChannelListItemBinding

class ChannelViewBindingAdapter(
	binding: FragmentChannelListItemBinding
) : IChannelViewBindingAdapter {

	override val rootView = binding.root

	override val logo = binding.logo
	override val subtitle = binding.subtitle
	override val showTitle = binding.textShowTitle
	override val showSubtitle = binding.textShowSubtitle
	override val showTime = binding.textShowTime
	override val showProgress = binding.progressbarShowProgress

}
