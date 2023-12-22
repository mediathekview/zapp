package de.christinecoenen.code.zapp.app.search.suggestions.chips

import de.christinecoenen.code.zapp.app.mediathek.api.request.MediathekChannel

data class ChannelChipContent(
	val channel: MediathekChannel,
) : ChipContent {
	override val content = channel
	override val label = channel.apiId
}
