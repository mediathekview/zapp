package de.christinecoenen.code.zapp.app.search.suggestions.chips

import de.christinecoenen.code.zapp.R
import de.christinecoenen.code.zapp.app.mediathek.api.request.MediathekChannel

data class ChannelChipContent(
	val channel: MediathekChannel,
) : ChipContent {
	override val content = channel
	override val label = channel.apiId
	override val icon = R.drawable.ic_ondemand_video_white_24dp
}
