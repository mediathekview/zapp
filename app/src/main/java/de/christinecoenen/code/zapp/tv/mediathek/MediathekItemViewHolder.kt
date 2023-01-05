package de.christinecoenen.code.zapp.tv.mediathek

import androidx.recyclerview.widget.RecyclerView
import de.christinecoenen.code.zapp.databinding.TvFragmentMediathekListItemBinding
import de.christinecoenen.code.zapp.models.shows.MediathekShow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent

class MediathekItemViewHolder(
	private val binding: TvFragmentMediathekListItemBinding
) : RecyclerView.ViewHolder(binding.root), KoinComponent {

	suspend fun setShow(show: MediathekShow) = withContext(Dispatchers.Main) {
		binding.topic.text = show.topic
		// fix layout_constraintWidth_max not be applied correctly
		binding.topic.requestLayout()

		binding.title.text = show.title
		binding.duration.text = show.formattedDuration
		binding.time.text = show.formattedTimestamp
		binding.channel.text = show.channel
	}
}
