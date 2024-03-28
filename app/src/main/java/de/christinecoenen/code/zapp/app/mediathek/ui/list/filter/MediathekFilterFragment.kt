package de.christinecoenen.code.zapp.app.mediathek.ui.list.filter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import com.google.android.material.chip.Chip
import de.christinecoenen.code.zapp.R
import de.christinecoenen.code.zapp.app.mediathek.api.request.MediathekChannel
import de.christinecoenen.code.zapp.app.mediathek.ui.list.ShowLengthLabelFormatter
import de.christinecoenen.code.zapp.databinding.MediathekFilterFragmentBinding
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import java.text.DateFormat
import java.text.NumberFormat
import java.util.Date
import java.util.Locale

class MediathekFilterFragment : Fragment() {

	private var _binding: MediathekFilterFragmentBinding? = null
	private val binding: MediathekFilterFragmentBinding get() = _binding!!

	private val viewModel: MediathekFilterViewModel by activityViewModel()

	private val numberFormat = NumberFormat.getInstance(Locale.getDefault())
	private val queryInfoDateFormatter = DateFormat.getDateTimeInstance(
		DateFormat.SHORT,
		DateFormat.SHORT
	)

	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		_binding = MediathekFilterFragmentBinding.inflate(inflater, container, false)

		binding.search.addTextChangedListener { editable ->
			viewModel.setSearchQueryFilter(editable.toString())
		}

		setUpLengthFilter()
		createChannelFilterView(inflater)

		// display query results if present
		viewModel.queryInfoResult
			.asLiveData(lifecycleScope.coroutineContext)
			.observe(viewLifecycleOwner) { queryInfoResult ->
				if (queryInfoResult == null) {
					binding.queryInfo.visibility = View.INVISIBLE
					return@observe
				}

				val date = Date(queryInfoResult.filmlisteTimestamp * 1000)
				val queryInfoMessage = getString(
					R.string.fragment_mediathek_query_info,
					numberFormat.format(queryInfoResult.totalResults),
					queryInfoDateFormatter.format(date)
				)

				binding.queryInfo.text = queryInfoMessage
				binding.queryInfo.visibility = View.VISIBLE
			}

		return binding.root
	}

	private fun setUpLengthFilter() {
		val showLengthLabelFormatter =
			ShowLengthLabelFormatter(binding.showLengthSlider.valueTo)

		updateLengthFilterLabels(showLengthLabelFormatter)
		binding.showLengthSlider.setLabelFormatter(showLengthLabelFormatter)

		// from ui to viewmodel
		binding.showLengthSlider.addOnChangeListener { rangeSlider, _, fromUser ->

			updateLengthFilterLabels(showLengthLabelFormatter)

			if (fromUser) {
				val min = rangeSlider.values[0] * 60
				val max =
					if (rangeSlider.values[1] == rangeSlider.valueTo) null else rangeSlider.values[1] * 60
				viewModel.setLengthFilter(min, max)
			}
		}

		// from viewmodel to ui
		viewModel.lengthFilter
			.asLiveData(lifecycleScope.coroutineContext)
			.observe(viewLifecycleOwner) { lengthFilter ->
				val min = lengthFilter.minDurationMinutes
				val max = lengthFilter.maxDurationMinutes ?: binding.showLengthSlider.valueTo
				binding.showLengthSlider.setValues(min, max)
			}
	}

	private fun createChannelFilterView(inflater: LayoutInflater) {
		val chipMap = mutableMapOf<MediathekChannel, Chip>()

		for (channel in MediathekChannel.values()) {
			// create view
			val chip = inflater.inflate(
				R.layout.view_mediathek_filter_chip,
				binding.channels,
				false
			) as Chip

			// view properties
			chip.text = channel.apiId

			// ui listeners
			chip.setOnCheckedChangeListener { _, isChecked ->
				onChannelFilterCheckChanged(channel, isChecked)
			}
			chip.setOnLongClickListener {
				onChannelFilterLongClick(channel)
				true
			}

			// add to hierarchy
			binding.channels.addView(chip)

			// cache for listeners
			chipMap[channel] = chip
		}

		// viewmodel listener
		viewModel.channelFilter
			.asLiveData(lifecycleScope.coroutineContext)
			.observe(viewLifecycleOwner) { channelFilter ->
				for (filterItem in channelFilter) {
					val chip = chipMap[filterItem.key]!!
					if (chip.isChecked != filterItem.value) {
						chip.isChecked = filterItem.value
					}
				}
			}
	}

	private fun onChannelFilterCheckChanged(channel: MediathekChannel, isChecked: Boolean) {
		viewModel.setChannelFilter(channel, isChecked)
	}

	private fun onChannelFilterLongClick(clickedChannel: MediathekChannel) {
		for (channel in MediathekChannel.values()) {
			val isChecked = clickedChannel == channel
			viewModel.setChannelFilter(channel, isChecked)
		}
	}

	private fun updateLengthFilterLabels(formatter: ShowLengthLabelFormatter) {
		binding.showLengthLabelMin.text =
			formatter.getFormattedValue(binding.showLengthSlider.values[0])
		binding.showLengthLabelMax.text =
			formatter.getFormattedValue(binding.showLengthSlider.values[1])
	}
}
